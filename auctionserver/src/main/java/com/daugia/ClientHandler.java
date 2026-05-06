package com.daugia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.daugia.dao.AuctionDAO;
import com.daugia.dao.UserDAO;
import com.daugia.handlers.ActionHandler;
import com.daugia.handlers.HandlerRegistry;
import com.daugia.models.Auction;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson = new Gson();

    private final HandlerRegistry registry = new HandlerRegistry();

    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            clientWriters.add(out);

            String inputLine;
            UserDAO userDAO = new UserDAO();
            AuctionDAO auctionDAO = new AuctionDAO();

            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = gson.fromJson(inputLine, Request.class);
                    Response response;

                    // Dispatch sang handler mới nếu action đã được đăng ký
                    ActionHandler handler = registry.get(request.getAction());
                    if (handler != null) {
                        Response handled = handler.handle(request);
                        out.println(gson.toJson(handled));
                        continue;
                    }

                    switch (request.getAction()) {
                        case "LOGIN": {
                            JsonObject loginData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                            String username = loginData.get("username").getAsString();
                            String password = loginData.get("password").getAsString();

                            User userDaLogin = userDAO.checkLogin(username, password);
                            if (userDaLogin != null) {
                                String userJsonPayload = gson.toJson(userDaLogin);
                                response = new Response("SUCCESS", "Đăng nhập thành công", userJsonPayload);
                            } else {
                                response = new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
                            }
                            out.println(gson.toJson(response));
                            break;
                        }

                        case "REGISTER": {
                            try {
                                JsonObject regData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                                String newUsername = regData.get("username").getAsString();
                                String newPassword = regData.get("password").getAsString();
                                String email = regData.has("email") ? regData.get("email").getAsString() : "";
                                String fullName = regData.has("fullName") ? regData.get("fullName").getAsString() : "";
                                String phone = regData.has("phone") ? regData.get("phone").getAsString() : "";
                                String gender = regData.has("gender") ? regData.get("gender").getAsString() : "";
                                String role = regData.get("role").getAsString();

                                boolean isRegistered = userDAO.registerUser(
                                    newUsername, newPassword, email, fullName, phone, gender, role
                                );

                                if (isRegistered) {
                                    response = new Response("SUCCESS", "Đăng ký tài khoản thành công!", null);
                                } else {
                                    response = new Response("ERROR", "Tên đăng nhập đã tồn tại hoặc lỗi hệ thống!", null);
                                }
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println(gson.toJson(new Response("ERROR", "Dữ liệu đăng ký không hợp lệ!", null)));
                            }
                            break;
                        }

                        case "GET_ALL_USERS": {
                            try {
                                List<User> danhSachUser = userDAO.getAllUsers();
                                String payloadUsers = gson.toJson(danhSachUser);
                                response = new Response("SUCCESS", "Lấy danh sách người dùng thành công", payloadUsers);
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println(gson.toJson(new Response("ERROR", "Lỗi khi lấy danh sách user", null)));
                            }
                            break;
                        }

                        case "GET_ALL_AUCTIONS": {
                            try {
                                List<Auction> danhSachHang =
                                    new java.util.ArrayList<>(AuctionManager.getInstance().getAllAuctions());

                                String payloadData = gson.toJson(danhSachHang);
                                response = new Response("SUCCESS", "Lấy danh sách thành công", payloadData);
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi nội bộ Server: " + e.getMessage(), null);
                                out.println(gson.toJson(response));
                            }
                            break;
                        }

                        case "GET_AUCTIONS_BY_CATEGORY": {
                            try {
                                String requestedCategory = request.getPayload();
                                List<Auction> allAuctions =
                                    new java.util.ArrayList<>(AuctionManager.getInstance().getAllAuctions());
                                List<Auction> filteredAuctions = new java.util.ArrayList<>();

                                for (Auction auc : allAuctions) {
                                    if (auc.getCategory() != null
                                        && auc.getCategory().equalsIgnoreCase(requestedCategory)) {
                                        filteredAuctions.add(auc);
                                    }
                                }

                                String payloadData = gson.toJson(filteredAuctions);
                                response = new Response("SUCCESS", "Lọc danh mục thành công", payloadData);
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi khi lọc danh mục", null);
                                out.println(gson.toJson(response));
                            }
                            break;
                        }

                        case "ADD_PRODUCT": {
                            try {
                                Auction newAuction = gson.fromJson(request.getPayload(), Auction.class);

                                String newId = "A" + System.currentTimeMillis();
                                newAuction.setId(newId);
                                newAuction.setStatus("OPEN");

                                boolean success = auctionDAO.addAuction(
                                    newAuction.getId(),
                                    newAuction.getName(),
                                    newAuction.getDescription(),
                                    newAuction.getStartingPrice(),
                                    newAuction.getEndTime(),
                                    newAuction.getSellerId(),
                                    newAuction.getStatus()
                                );

                                if (success) {
                                    AuctionManager.getInstance().addAuction(newAuction);
                                    response = new Response("SUCCESS", "Đăng sản phẩm thành công!", newAuction);
                                } else {
                                    response = new Response("ERROR", "Lỗi DB: Database từ chối lưu dữ liệu!", null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi hệ thống: " + e.getMessage(), null);
                            }
                            out.println(gson.toJson(response));
                            break;
                        }

                        case "DELETE_PRODUCT": {
                            try {
                                String productId = request.getPayload();

                                boolean dbDeleted = auctionDAO.deleteAuction(productId);
                                if (!dbDeleted) {
                                    out.println(gson.toJson(
                                        new Response("ERROR", "Không xóa được trong DB", null)
                                    ));
                                    break;
                                }

                                java.util.Collection<Auction> listRam =
                                    AuctionManager.getInstance().getAllAuctions();
                                listRam.removeIf(a -> a.getId().equals(productId));

                                response = new Response("SUCCESS", "Xóa thành công!", null);
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println(gson.toJson(
                                    new Response("ERROR", "Lỗi xóa sản phẩm: " + e.getMessage(), null)
                                ));
                            }
                            break;
                        }

                        case "PLACE_BID": {
                            try {
                                JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                                String auctionId = bidData.get("auctionId").getAsString();
                                double bidAmount = bidData.get("amount").getAsDouble();

                                Auction currentAuction = AuctionManager.getInstance().getAuction(auctionId);
                                if (currentAuction == null) {
                                    out.println(gson.toJson(
                                        new Response("ERROR", "Mã hàng không tồn tại!", null)
                                    ));
                                    break;
                                }

                                synchronized (currentAuction) {
                                    if (bidAmount <= currentAuction.getCurrentHighestBid()) {
                                        out.println(gson.toJson(new Response(
                                            "ERROR",
                                            "Chậm chân rồi! Đã có người trả giá cao hơn hoặc bằng giá bạn đặt ("
                                                + currentAuction.getCurrentHighestBid() + ")",
                                            null
                                        )));
                                        break;
                                    }

                                    currentAuction.setCurrentHighestBid(bidAmount);
                                    auctionDAO.updateHighestBid(auctionId, bidAmount);

                                    JsonObject successData = new JsonObject();
                                    successData.addProperty("auctionId", auctionId);
                                    successData.addProperty("newHighestBid", bidAmount);

                                    Response successRes =
                                        new Response("SUCCESS", "Đặt giá thành công", gson.toJson(successData));
                                    out.println(gson.toJson(successRes));

                                    JsonObject broadcastReq = new JsonObject();
                                    broadcastReq.addProperty("action", "BID_UPDATE");
                                    broadcastReq.addProperty("payload", gson.toJson(successData));
                                    broadcast(gson.toJson(broadcastReq));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println(gson.toJson(
                                    new Response("ERROR", "Dữ liệu đặt giá bị lỗi!", null)
                                ));
                            }
                            break;
                        }

                        default: {
                            response = new Response("ERROR", "Hành động không hợp lệ", null);
                            out.println(gson.toJson(response));
                            break;
                        }
                    }

                } catch (Exception reqEx) {
                    reqEx.printStackTrace();
                    Response errorResponse = new Response("ERROR", "Dữ liệu yêu cầu không hợp lệ", null);
                    out.println(gson.toJson(errorResponse));
                }
            }

        } catch (Exception e) {
            System.out.println("Client ngắt kết nối: " + e.getMessage());
        } finally {
            if (out != null) {
                clientWriters.remove(out);
            }
            try {
                socket.close();
            } catch (Exception ignore) {
                // ignore close exception
            }
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}