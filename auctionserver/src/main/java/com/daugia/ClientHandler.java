package com.daugia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.daugia.dao.AuctionDAO;
import com.daugia.dao.UserDAO;
import com.daugia.models.Auction;
import com.daugia.models.BidMessage;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Gson   gson = new Gson();

    // out là field (không phải biến local) vì joinRoom/leaveRoom cần dùng nó
    private PrintWriter out;

    // Phòng client đang xem, -1 = chưa vào phòng nào
    private int currentAuctionId = -1;

    public ClientHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (
            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // autoFlush=true: writer.println() gửi ngay, không cần flush() thủ công
            PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = out;
            String line;

            while ((line = in.readLine()) != null) {
                Request  request  = gson.fromJson(line, Request.class);
                Response response = null;

                switch (request.getAction()) {

                    // ── Đăng nhập ─────────────────────────────────────────
                    case "LOGIN": {
                        JsonObject d    = parse(request.getPayload());
                        UserDAO    dao  = new UserDAO();
                        User user = dao.checkLogin(
                            d.get("username").getAsString(),
                            d.get("password").getAsString()
                        );
                        response = user != null
                            ? new Response("SUCCESS", "Đăng nhập thành công", gson.toJson(user))
                            : new Response("ERROR",   "Sai tài khoản hoặc mật khẩu", null);
                        break;
                    }

                    // ── Đăng ký ───────────────────────────────────────────
                    case "REGISTER": {
                        JsonObject d   = parse(request.getPayload());
                        UserDAO    dao = new UserDAO();
                        boolean ok = dao.register(
                            d.get("username").getAsString(),
                            d.get("password").getAsString()
                        );
                        response = ok
                            ? new Response("SUCCESS", "Đăng ký thành công", null)
                            : new Response("ERROR",   "Tài khoản đã tồn tại!", null);
                        break;
                    }

                    // ── Lấy danh sách đấu giá — LẤY TỪ DB THẬT ──────────
                    case "GET_ALL_AUCTIONS": {
                        AuctionDAO dao  = new AuctionDAO();
                        String     data = gson.toJson(dao.getAllAuctionDTOs());
                        response = new Response("SUCCESS", "Lấy danh sách thành công", data);
                        break;
                    }

                    // ── Lấy theo danh mục — LẤY TỪ DB THẬT ──────────────
                    case "GET_ITEMS_BY_CATEGORY": {
                        JsonObject d        = parse(request.getPayload());
                        String     category = d.get("category").getAsString();
                        AuctionDAO dao      = new AuctionDAO();
                        // Lọc DTO theo category
                        java.util.List<AuctionDAO.AuctionDTO> all = dao.getAllAuctionDTOs();
                        java.util.List<AuctionDAO.AuctionDTO> filtered = new java.util.ArrayList<>();
                        for (AuctionDAO.AuctionDTO dto : all) {
                            if (dto.category.equals(category)) filtered.add(dto);
                        }
                        response = new Response("SUCCESS", "Lấy dữ liệu thành công", gson.toJson(filtered));
                        break;
                    }

                    // ── Client vào xem một phiên đấu giá ─────────────────
                    // ── REALTIME STEP 1 (SERVER SIDE) ─────────────────────
                    case "JOIN_AUCTION": {
                        JsonObject d         = parse(request.getPayload());
                        int        auctionId = d.get("auctionId").getAsInt();
                        AuctionManager manager = AuctionManager.getInstance();
                        Auction auction = manager.getAuction(auctionId);

                        if (auction != null) {
                            currentAuctionId = auctionId;

                            // Đăng ký output stream của client này vào "phòng"
                            // → từ giờ broadcastToRoom() sẽ gửi tin tới client này
                            manager.joinRoom(auctionId, out);

                            // Gửi toàn bộ lịch sử bid để client vẽ lại chart từ đầu
                            // (phục vụ client vào giữa chừng khi đã có bid rồi)
                            String historyJson = gson.toJson(auction.getBidHistory());
                            response = new Response("BID_HISTORY", "Lịch sử đấu giá", historyJson);
                        } else {
                            response = new Response("ERROR", "Không tìm thấy phiên đấu giá!", null);
                        }
                        break;
                    }

                    // ── Đặt giá ───────────────────────────────────────────
                    // ── REALTIME STEP 2 (SERVER SIDE) ─────────────────────
                    case "PLACE_BID": {
                        JsonObject d         = parse(request.getPayload());
                        int        auctionId = d.get("auctionId").getAsInt();
                        double     amount    = d.get("amount").getAsDouble();
                        String     username  = d.get("username").getAsString();

                        AuctionManager manager = AuctionManager.getInstance();
                        Auction auction = manager.getAuction(auctionId);

                        if (auction == null) {
                            response = new Response("ERROR", "Không tìm thấy phiên đấu giá!", null);
                            break;
                        }

                        boolean success = auction.placeBid(username, amount);

                        if (success) {
                            // ── ĐÂY LÀ ĐIỂM REALTIME THỰC SỰ ──────────────────
                            // placeBid() đã ghi BidMessage vào bidHistory trong Auction.
                            // Lấy BidMessage vừa tạo và broadcast tới TẤT CẢ client trong phòng.
                            // Mỗi client đang chạy listenFromServer() sẽ nhận được ngay.
                            BidMessage latest = auction.getLatestBid();
                            manager.broadcastToRoom(auctionId, gson.toJson(latest));
                            // ────────────────────────────────────────────────────

                            response = new Response("SUCCESS", "Đặt giá thành công!",
                                String.valueOf(auction.getCurrentHighestBid()));
                        } else {
                            response = new Response("ERROR",
                                "Giá không hợp lệ hoặc phiên đấu giá đã kết thúc!", null);
                        }
                        break;
                    }

                    default:
                        response = new Response("ERROR", "Không tìm thấy Action", null);
                }

                out.println(gson.toJson(response));
            }

        } catch (Exception e) {
            System.out.println("Client ngắt kết nối.");
        } finally {
            // Khi client ngắt kết nối → xóa khỏi phòng, tránh gửi vào socket chết
            if (currentAuctionId != -1 && out != null) {
                AuctionManager.getInstance().leaveRoom(currentAuctionId, out);
            }
        }
    }

    private JsonObject parse(String payload) {
        return JsonParser.parseString(payload).getAsJsonObject();
    }
}