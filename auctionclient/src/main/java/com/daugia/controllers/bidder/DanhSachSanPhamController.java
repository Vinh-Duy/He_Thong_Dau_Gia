package com.daugia.controllers.bidder;

import java.util.List;

import com.daugia.models.Auction;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class DanhSachSanPhamController {

    @FXML private Label lblTitle;
    @FXML private FlowPane productContainer;
    private java.util.Map<String, Label> mapGiaSanPham = new java.util.HashMap<>();

    private String currentCategory;

    public void setCategory(String categoryName) {
        this.currentCategory = categoryName;
        lblTitle.setText("Danh mục: " + categoryName);
        
        loadProducts();
    }

    @FXML
    public void initialize() {
        // Vừa mở trang lên là tự động load dữ liệu
        loadAuctionsFromServer();

        // Bật công tắc lắng nghe Real-time
        NetworkClient.getInstance().setOnMessageReceived(message -> {
            Platform.runLater(() -> {
                try {
                    com.google.gson.JsonObject data = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
                    
                    // Nếu là tin nhắn Cập nhật giá
                    if (data.has("action") && "BID_UPDATE".equals(data.get("action").getAsString())) {
                        String payloadString = data.get("payload").getAsString();
                        com.google.gson.JsonObject payloadJson = com.google.gson.JsonParser.parseString(payloadString).getAsJsonObject();

                        String updatedAuctionId = payloadJson.get("auctionId").getAsString();
                        double newPrice = payloadJson.get("newHighestBid").getAsDouble();

                        // Tìm đúng cái nhãn giá của món hàng đó trong Bản đồ
                        Label lblCanSua = mapGiaSanPham.get(updatedAuctionId);
                        
                        if (lblCanSua != null) {
                            // Cập nhật số tiền mới
                            lblCanSua.setText("Giá hiện tại: " + String.format("%,.0f", newPrice) + " VNĐ");
                            
                            // (Hiệu ứng chớp nháy) Đổi màu chữ sang ĐỎ cho khách chú ý
                            lblCanSua.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            
                            // Sau 1.5 giây thì đổi lại màu đen bình thường
                            new java.util.Timer().schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> lblCanSua.setStyle("-fx-text-fill: black;"));
                                }
                            }, 1500);
                            
                            System.out.println("=> [REAL-TIME] Đã cập nhật giá mới cho SP " + updatedAuctionId);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi đọc tin realtime: " + e.getMessage());
                }
            });
        });
    }

    private void loadProducts() {
        // Xóa sạch màn hình trước khi load hàng mới
        productContainer.getChildren().clear();

        new Thread(() -> {
            try {
                // 1. GỬI LỆNH LỌC LÊN SERVER (Sửa lại đúng tên lệnh Server đang chờ)
                Request request = new Request("GET_AUCTIONS_BY_CATEGORY", currentCategory);
                Response res = NetworkClient.getInstance().sendRequest(request);

                // 2. XỬ LÝ KẾT QUẢ TRẢ VỀ
                if (res != null && "SUCCESS".equals(res.getStatus())) {
                    
                    // Ép kiểu JSON về danh sách List<Auction> giống hệt lúc lấy tất cả
                    Gson gson = new Gson();
                    java.lang.reflect.Type listType = new TypeToken<List<Auction>>(){}.getType();
                    List<Auction> dsSanPham = gson.fromJson(res.getData().toString(), listType);

                    Platform.runLater(() -> {
                        if (dsSanPham.isEmpty()) {
                            Label lblEmpty = new Label("Hiện chưa có sản phẩm nào trong danh mục này.");
                            lblEmpty.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
                            productContainer.getChildren().add(lblEmpty);
                        } else {
                            hienThiLenGiaoDien(dsSanPham);
                        }
                    });
                } else {
                    System.out.println("Lỗi từ server: " + (res != null ? res.getMessage() : "Null"));
                }
            } catch (Exception e) {
                System.out.println("Lỗi mạng khi lọc sản phẩm: " + e.getMessage());
            }
        }).start();
    }


    private void loadAuctionsFromServer() {
        new Thread(() -> {
            try {
                System.out.println("--- BƯỚC 2: Đang gửi yêu cầu GET_AUCTIONS lên Server... ---");
                Request req = new Request("GET_ALL_AUCTIONS", "");
                Response res = NetworkClient.getInstance().sendRequest(req);

                System.out.println("--- BƯỚC 3: Server đã trả lời! Trạng thái: " + (res != null ? res.getStatus() : "NULL") + " ---");
                System.out.println("--- BƯỚC 4: Dữ liệu Server gửi về: " + (res != null ? res.getData() : "NULL") + " ---");

                if (res != null && "SUCCESS".equals(res.getStatus())) {
                    Gson gson = new Gson();
                    java.lang.reflect.Type listType = new TypeToken<List<Auction>>(){}.getType();
                    List<Auction> dsSanPham = gson.fromJson(res.getData().toString(), listType);

                    System.out.println("--- BƯỚC 5: Chuyển JSON thành công! Số lượng sản phẩm kéo được: " + dsSanPham.size() + " ---");

                    Platform.runLater(() -> {
                        System.out.println("--- BƯỚC 6: Bắt đầu vẽ lên giao diện... ---");
                        hienThiLenGiaoDien(dsSanPham);
                    });
                }
            } catch (Exception e) {
                System.out.println("--- CÓ LỖI XẢY RA RỒI: ---");
                e.printStackTrace();
            }
        }).start();
    }

    private void hienThiLenGiaoDien(List<Auction> ds) {
        productContainer.getChildren().clear(); 
        
        for (Auction sp : ds) {
            // 1. Tạo một cái HBox (Hộp nằm ngang) để chứa thông tin cho gọn
            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(15); 
            hbox.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-width: 0 0 1 0; -fx-alignment: center-left;");
            
            // 2. Chữ hiển thị thông tin
            javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(5);
            javafx.scene.control.Label lblName = new javafx.scene.control.Label(sp.getName());
            lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            
            // Lấy giá cao nhất hiện tại (currentHighestBid), nếu nó bằng 0 thì lấy giá khởi điểm
            double giaHienTai = sp.getCurrentHighestBid() > 0 ? sp.getCurrentHighestBid() : sp.getStartingPrice();
            javafx.scene.control.Label lblPrice = new javafx.scene.control.Label("Giá hiện tại: " + String.format("%.0f", giaHienTai) + " VNĐ");
            infoBox.getChildren().addAll(lblName, lblPrice);
            
            mapGiaSanPham.put(String.valueOf(sp.getId()), lblPrice);
            // 3. Ô nhập tiền và Nút bấm
            javafx.scene.control.TextField txtBid = new javafx.scene.control.TextField();
            txtBid.setPromptText("Nhập giá cao hơn...");
            
            javafx.scene.control.Button btnBid = new javafx.scene.control.Button("Ra Giá");
            btnBid.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            
            // 4. Bắt sự kiện khi bấm nút "Ra Giá"
            btnBid.setOnAction(event -> {
                // CHỐT KIỂM TRA ĐĂNG NHẬP 
                String currentUser = com.daugia.utils.SessionManager.getUsername();
                if (currentUser == null || currentUser.isEmpty()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Cảnh báo");
                    alert.setHeaderText(null);
                    alert.setContentText("Bác chưa đăng nhập! Vui lòng đăng nhập tài khoản để tham gia đấu giá nhé.");
                    alert.showAndWait();
                    
                    return; // <--- Lệnh này sẽ đá văng người dùng ra, không cho chạy câu lệnh bên dưới
                }

                // Nếu đã qua được cửa bảo vệ (đã đăng nhập), thì mới cho phép gọi hàm xử lý đặt giá
                xuLyDatGia(sp.getId(), txtBid.getText());
            });
            
            // Nhét tất cả vào HBox, rồi nhét HBox vào Container chính
            hbox.getChildren().addAll(infoBox, txtBid, btnBid);
            productContainer.getChildren().add(hbox);
        }
    }

    private void xuLyDatGia(String auctionId, String tienNhapVao) {
        // 1. Kiểm tra xem user đã nhập số chưa
        if (tienNhapVao == null || tienNhapVao.trim().isEmpty()) {
            showAlert("Lỗi", "Bác chưa nhập số tiền kìa!");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(tienNhapVao);
            
            // 2. Lấy tên người dùng đang đăng nhập từ "Két sắt" SessionManager
            String username = com.daugia.utils.SessionManager.getUsername();
            if (username == null) {
                showAlert("Cảnh báo", "Bác phải đăng nhập mới được đấu giá nhé!");
                return;
            }

            // 3. Đóng gói Dữ liệu chuẩn bị gửi (Giống hệt cái Server đang chờ)
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("auctionId", auctionId);
            payload.addProperty("amount", bidAmount);
            payload.addProperty("username", username);

            // 4. Gửi lên Server qua luồng riêng
            new Thread(() -> {
                Request req = new Request("PLACE_BID", payload.toString());
                Response res = NetworkClient.getInstance().sendRequest(req);

                // 5. Cập nhật giao diện khi có kết quả
                Platform.runLater(() -> {
                    if (res != null) {
                        if ("SUCCESS".equals(res.getStatus())) {
                            showAlert("Tuyệt vời", "Đặt giá thành công!");
                            // Đặt giá xong thì tự động load lại danh sách để cập nhật giá mới
                        } else {
                            showAlert("Thất bại", res.getMessage());
                        }
                    } else {
                        showAlert("Lỗi mạng", "Không nhận được phản hồi từ Server.");
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Tiền tệ mà bác nhập chữ cái vào làm gì? Nhập số thôi!");
        }
    }

    // Hàm tiện ích để hiển thị thông báo Popup cho nhanh
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}