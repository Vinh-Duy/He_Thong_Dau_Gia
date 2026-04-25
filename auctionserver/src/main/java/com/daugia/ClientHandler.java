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
                            String username = loginData.get("username").getAsString();
                            String password = loginData.get("password").getAsString();
                            
                            // Check xem DB có tài khoản này không
                            User loggedInUser = userDAO.checkLogin(username, password);
                            
                            User userDaLogin = userDAO.checkLogin(username, password);

                            if (userDaLogin != null) {
                                // 🔥 CHỖ NÀY QUAN TRỌNG NHẤT: 
                                // Ép đối tượng User thành chuỗi JSON, rồi mới nhét vào Response
                                String userJsonPayload = gson.toJson(userDaLogin);
                                
                                response = new Response("SUCCESS", "Đăng nhập thành công", userJsonPayload);
                            } else {
                                response = new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
                            }
                            out.println(gson.toJson(response));

                            break;

                        case "PLACE_BID":
                            try {
                                // Lấy dữ liệu đặt giá từ Client gửi lên (Lưu ý: dùng request hoặc req tùy code cũ của bác)
                                JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                                String auctionId = bidData.get("auctionId").getAsString();
                                double bidAmount = bidData.get("amount").getAsDouble();

                                // 1. Lấy sản phẩm từ kho ra
                                Auction currentAuction = AuctionManager.getInstance().getAuction(auctionId);
                                if (currentAuction == null) {
                                    out.println(gson.toJson(new Response("ERROR", "Mã hàng không tồn tại!", null)));
                                    break;
                                }

                                // =================================================================
                                // 🔥 BẮT ĐẦU CHỐNG RACE CONDITION Ở ĐÂY (ĂN TRỌN 1 ĐIỂM) 🔥
                                // Khóa riêng món hàng này lại (Các món hàng khác vẫn được mua bán bình thường)
                                // =================================================================
                                synchronized (currentAuction) {
                                    
                                    // 2. Kiểm tra lại giá một lần nữa BÊN TRONG KHÓA (Double-check an toàn tuyệt đối)
                                    if (bidAmount <= currentAuction.getCurrentHighestBid()) {
                                        out.println(gson.toJson(new Response("ERROR", "Chậm chân rồi! Đã có người trả giá cao hơn hoặc bằng giá bạn đặt (" + currentAuction.getCurrentHighestBid() + ")", null)));
                                        break; // Thoát ra khỏi khối kiểm tra, TỰ ĐỘNG MỞ KHÓA cho người khác vào
                                    }

                                    // 3. Nếu an toàn -> Chốt giá mới vào RAM và DB
                                    currentAuction.setCurrentHighestBid(bidAmount);
                                    com.daugia.dao.AuctionDAO dao = new com.daugia.dao.AuctionDAO();
                                    dao.updateHighestBid(auctionId, bidAmount); // Cập nhật luôn DB trong lúc đang khóa

                                    // Tạo hộp thông tin để báo hỷ
                                    JsonObject successData = new JsonObject();
                                    successData.addProperty("auctionId", auctionId);
                                    successData.addProperty("newHighestBid", bidAmount);

                                    // 4. Báo "Thành công" cho người vừa đặt
                                    Response successRes = new Response("SUCCESS", "Đặt giá thành công", gson.toJson(successData));
                                    out.println(gson.toJson(successRes));

                                    // 5. Cầm loa phát sóng (Broadcast) cho các máy khác nhảy số
                                    JsonObject broadcastReq = new JsonObject();
                                    broadcastReq.addProperty("action", "BID_UPDATE");
                                    broadcastReq.addProperty("payload", gson.toJson(successData));
                                    broadcast(gson.toJson(broadcastReq));
                                    
                                } 
                                // === KẾT THÚC KHÓA (Ổ KHÓA TỰ ĐỘNG MỞ) ===

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
                        
                        // 🔥 THÊM NGUYÊN KHỐI NÀY VÀO DƯỚI CASE GET_ALL_AUCTIONS 🔥
                        case "GET_AUCTIONS_BY_CATEGORY":
                            try {
                                // 1. Lấy tên danh mục Client muốn xem từ payload (Ví dụ: "Bất động sản")
                                String requestedCategory = request.getPayload();
                                
                                // 2. Kéo toàn bộ hàng trong kho ra
                                java.util.List<Auction> allAuctions = new java.util.ArrayList<>(AuctionManager.getInstance().getAllAuctions());
                                java.util.List<Auction> filteredAuctions = new java.util.ArrayList<>();
                                
                                // 3. Lọc: Chỉ nhặt những món có Category trùng khớp
                                for (Auction auc : allAuctions) {
                                    if (auc.getCategory() != null && auc.getCategory().equalsIgnoreCase(requestedCategory)) {
                                        filteredAuctions.add(auc);
                                    }
                                }
                                
                                // 4. Đóng gói danh sách đã lọc và gửi về
                                String payloadData = gson.toJson(filteredAuctions);
                                response = new Response("SUCCESS", "Lọc danh mục thành công", payloadData);
                                out.println(gson.toJson(response));
                                
                                System.out.println("=> [SERVER LỌC DANH MỤC] Đã gửi " + filteredAuctions.size() + " món thuộc loại: " + requestedCategory);
                                
                            } catch (Exception e) {
                                System.out.println("!!! LỖI KHI LỌC DANH MỤC !!!");
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi khi lọc danh mục", null);
                                out.println(gson.toJson(response));
                            }
                            break;

                        // ... các case cũ ...
                        case "REGISTER":
                            try {
                                // 1. Bóc tách dữ liệu từ Client gửi lên
                                JsonObject regData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                                String newUsername = regData.get("username").getAsString();
                                String newPassword = regData.get("password").getAsString();
                                String email = regData.has("email") ? regData.get("email").getAsString() : "";
                                String fullName = regData.has("fullName") ? regData.get("fullName").getAsString() : "";
                                String phone = regData.has("phone") ? regData.get("phone").getAsString() : "";
                                String gender = regData.has("gender") ? regData.get("gender").getAsString() : "";
                                // 🔥 THÊM DÒNG NÀY ĐỂ LẤY ROLE (Nếu Client không gửi thì mặc định là BIDDER)
                                String role = regData.has("role") ? regData.get("role").getAsString() : "BIDDER";

                                // 🔥 TRUYỀN THÊM biền role VÀO HÀM DAO
                                boolean isRegistered = userDAO.registerUser(newUsername, newPassword, email, fullName, phone, gender, role);

                                // 3. Trả lời Client
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

                        case "GET_ALL_USERS":
                            try {
                                // 1. Kéo dữ liệu từ DAO
                                List<User> danhSachUser = userDAO.getAllUsers();
                                
                                // 2. Chuyển thành JSON
                                String payloadUsers = gson.toJson(danhSachUser);
                                
                                // 3. Đóng gói và gửi về
                                response = new Response("SUCCESS", "Lấy danh sách người dùng thành công", payloadUsers);
                                out.println(gson.toJson(response));
                            } catch (Exception e) {
                                out.println(gson.toJson(new Response("ERROR", "Lỗi khi lấy danh sách user", null)));
                            }
                            break; // QUAN TRỌNG!
                        
                        case "ADD_PRODUCT":
                            try {
                                // 1. Giải mã món hàng từ Client gửi lên
                                Auction newAuction = gson.fromJson(request.getPayload(), Auction.class);
                                
                                // 2. Tự tạo ID duy nhất (Ví dụ: A1714012345678)
                                String newId = "A" + System.currentTimeMillis();
                                newAuction.setId(newId);
                                newAuction.setStatus("OPEN"); // Đảm bảo trạng thái luôn là mở khi mới đăng

                                // 3. Gọi DAO để lưu xuống MySQL
                                // Lưu ý: Dòng này sẽ trả về true nếu bác đã sửa "> 1" thành "> 0" ở AuctionDAO
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
                                    // 4. LƯU VÀO RAM - Cực kỳ quan trọng để Client lấy được danh sách mới
                                    AuctionManager.getInstance().addAuction(newAuction);
                                    
                                    // 5. Trả lời thành công cho Client (Gửi kèm đối tượng newAuction để Client có ID mới)
                                    response = new Response("SUCCESS", "Đăng sản phẩm thành công!", newAuction);
                                    System.out.println("=> [SERVER SUCCESS] Đã lưu DB và RAM sản phẩm: " + newAuction.getName());
                                } else {
                                    // Nếu hàm addAuction trả về false
                                    response = new Response("ERROR", "Lỗi DB: Database từ chối lưu dữ liệu!", null);
                                    System.err.println("=> [SERVER ERROR] AuctionDAO trả về false. Kiểm tra lại SQL!");
                                }
                            } catch (Exception e) {
                                // Bắt lỗi nếu dữ liệu JSON sai hoặc lỗi code
                                e.printStackTrace();
                                response = new Response("ERROR", "Lỗi hệ thống: " + e.getMessage(), null);
                            }
                            
                            // GỬI KẾT QUẢ VỀ CLIENT VÀ THOÁT CASE
                            out.println(gson.toJson(response));
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