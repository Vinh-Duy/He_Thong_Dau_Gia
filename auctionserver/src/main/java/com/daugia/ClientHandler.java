package com.daugia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.daugia.dao.AuctionDAO;
import com.daugia.dao.UserDAO;
import com.daugia.models.Auction;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Gson gson = new Gson();

    // 1. TẠO DANH SÁCH LƯU TẤT CẢ CLIENT ĐANG KẾT NỐI
    // Dùng ConcurrentHashMap.newKeySet() để không bị lỗi đụng độ (crash) khi có người thoát ra giữa lúc đang gửi tin
    private static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // 2. KHI CLIENT KẾT NỐI -> THÊM VÀO DANH SÁCH
            clientWriters.add(out);

            String inputLine;
            UserDAO userDAO = new UserDAO();
            AuctionDAO auctionDAO = new AuctionDAO();

            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = gson.fromJson(inputLine, Request.class);
                    Response response = null;

                    switch (request.getAction()) {
                        case "LOGIN":
                            JsonObject loginData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                            User loggedInUser = userDAO.checkLogin(
                                loginData.get("username").getAsString(), 
                                loginData.get("password").getAsString()
                            );
                            
                            if (loggedInUser != null) {
                                response = new Response("SUCCESS", "Đăng nhập thành công", gson.toJson(loggedInUser));
                            } else {
                                response = new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
                            }
                            // Trả lời riêng cho người đang login
                            out.println(gson.toJson(response)); 
                            break;

                        case "PLACE_BID":
                            try {
                                JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                                String auctionId = bidData.get("auctionId").getAsString();
                                double bidAmount = bidData.get("amount").getAsDouble();
                                String username = bidData.get("username").getAsString();

                                // 1. Lấy sản phẩm từ kho để kiểm tra
                                Auction currentAuction = AuctionManager.getInstance().getAuction(auctionId);
                                if (currentAuction == null) {
                                    out.println(gson.toJson(new Response("ERROR", "Mã hàng không tồn tại!", null)));
                                    break;
                                }

                                // 2. Kiểm tra xem tiền đặt có đủ lớn không
                                if (bidAmount <= currentAuction.getCurrentHighestBid()) {
                                    out.println(gson.toJson(new Response("ERROR", "Giá đặt phải CAO HƠN giá hiện tại (" + currentAuction.getCurrentHighestBid() + ")", null)));
                                    break;
                                }

                                // 3. Nếu OK -> Cập nhật giá mới vào RAM và DB
                                currentAuction.setCurrentHighestBid(bidAmount);
                                com.daugia.dao.AuctionDAO dao = new com.daugia.dao.AuctionDAO();
                                dao.updateHighestBid(auctionId, bidAmount);

                                // Tạo cục dữ liệu chuẩn bị gửi đi
                                JsonObject successData = new JsonObject();
                                successData.addProperty("auctionId", auctionId);
                                successData.addProperty("newHighestBid", bidAmount);

                                // 4. TRẢ LỜI CHO NGƯỜI VỪA BẤM NÚT (Báo Success)
                                Response successRes = new Response("SUCCESS", "Đặt giá thành công", gson.toJson(successData));
                                out.println(gson.toJson(successRes));

                                // 5. QUAN TRỌNG NHẤT: CẦM LOA HÉT CHO NHỮNG NGƯỜI KHÁC BIẾT (BROADCAST)
                                JsonObject broadcastReq = new JsonObject();
                                broadcastReq.addProperty("action", "BID_UPDATE");
                                broadcastReq.addProperty("payload", gson.toJson(successData));
                                
                                broadcast(gson.toJson(broadcastReq)); // Gửi lệnh đi cho tất cả các Client!

                            } catch (Exception e) {
                                System.out.println("Lỗi khi xử lý đặt giá: " + e.getMessage());
                                out.println(gson.toJson(new Response("ERROR", "Dữ liệu đặt giá bị lỗi!", null)));
                            }
                            break;

                        case "GET_ALL_AUCTIONS":
                            try {
                                // 1. Lấy dữ liệu từ kho (RAM) và ép ngay sang dạng ArrayList chuẩn để Gson không bị ngáo
                                java.util.List<Auction> danhSachHang = new java.util.ArrayList<>(AuctionManager.getInstance().getAllAuctions()); 
                                
                                // 2. Chuyển danh sách thành chuỗi JSON
                                String payloadData = gson.toJson(danhSachHang);
                                
                                // 3. Đóng gói vào hộp Response
                                response = new Response("SUCCESS", "Lấy danh sách thành công", payloadData);
                                
                                // 4. Đóng gói lần cuối và in ra màn hình Server (Camera giám sát)
                                String duLieuGuiDi = gson.toJson(response);
                                System.out.println("=> [SERVER GỬI DANH SÁCH]: " + duLieuGuiDi);
                                
                                // 5. Gửi sang Client
                                out.println(duLieuGuiDi);
                                
                            } catch (Exception e) {
                                // NẾU CÓ LỖI, NÓ SẼ BÁO ĐỎ CHÓT Ở SERVER VÀ GỬI LỖI VỀ CLIENT
                                System.out.println("!!! LỖI KHI LẤY DANH SÁCH HÀNG CỦA CLIENT !!!");
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi nội bộ Server: " + e.getMessage(), null);
                                out.println(gson.toJson(response));
                            }
                            break;
                        default:
                            response = new Response("ERROR", "Hành động không hợp lệ", null);
                            out.println(gson.toJson(response));
                    }
                    
                } catch (Exception reqEx) {
                    reqEx.printStackTrace(); // THÊM DÒNG NÀY VÀO ĐỂ HIỆN LỖI ĐỎ NẾU CÓ
                    Response errorResponse = new Response("ERROR", "Dữ liệu yêu cầu không hợp lệ", null);
                    out.println(gson.toJson(errorResponse));
                }
            }
        } catch (Exception e) {
            System.out.println("Client ngắt kết nối: " + e.getMessage());
        } finally {
            // 4. KHI CLIENT TẮT APP/MẤT MẠNG -> RÚT ỐNG THỞ CỦA HỌ KHỎI DANH SÁCH
            if (out != null) {
                clientWriters.remove(out);
            }
            try { socket.close(); } catch (Exception e) {}
        }
    }

    // 5. HÀM CẦM LOA HÉT CHO TẤT CẢ MỌI NGƯỜI
    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            try {
                // Gửi message cho từng client trong danh sách
                writer.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}