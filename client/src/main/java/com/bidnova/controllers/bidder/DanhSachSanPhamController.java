package com.bidnova.controllers.bidder;

import java.util.List;

import java.time.LocalDateTime;

import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DanhSachSanPhamController {

    @FXML private Label lblTitle;
    @FXML private VBox productContainer;
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
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .create();
                    java.lang.reflect.Type listType = new TypeToken<List<Auction>>(){}.getType();
                    List<Auction> dsSanPham = gson.fromJson(res.getData().toString(), listType);

                    // 3. VẼ LÊN GIAO DIỆN BẰNG HÀM CŨ ĐANG CHẠY RẤT NGON
                    Platform.runLater(() -> {
                        if (dsSanPham.isEmpty()) {
                            // Nếu danh mục này chưa có hàng, báo 1 câu cho đẹp
                            Label lblEmpty = new Label("Hiện chưa có sản phẩm nào trong danh mục này.");
                            lblEmpty.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
                            productContainer.getChildren().add(lblEmpty);
                        } else {
                            hienThiLuoiCard(dsSanPham);
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
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .create();
                    java.lang.reflect.Type listType = new TypeToken<List<Auction>>(){}.getType();
                    List<Auction> dsSanPham = gson.fromJson(res.getData().toString(), listType);

                    System.out.println("--- BƯỚC 5: Chuyển JSON thành công! Số lượng sản phẩm kéo được: " + dsSanPham.size() + " ---");

                    Platform.runLater(() -> {
                        System.out.println("--- BƯỚC 6: Bắt đầu vẽ lên giao diện... ---");
                        hienThiTheoNhom(dsSanPham);
                    });
                }
            } catch (Exception e) {
                System.out.println("--- CÓ LỖI XẢY RA RỒI: ---");
                e.printStackTrace();
            }
        }).start();
    }

    // ============ HIỂN THỊ THEO NHÓM CATEGORY (khi xem tất cả) ============
    private void hienThiTheoNhom(List<Auction> ds) {
        productContainer.getChildren().clear();
        
        java.util.Map<String, java.util.List<Auction>> groups = new java.util.LinkedHashMap<>();
        for (Auction sp : ds) {
            String cat = sp.getCategory();
            if (cat == null || cat.isEmpty()) cat = "Chưa phân loại";
            groups.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(sp);
        }
        
        for (java.util.Map.Entry<String, java.util.List<Auction>> entry : groups.entrySet()) {
            Label lblCat = new Label("📂 " + entry.getKey());
            lblCat.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            FlowPane flow = new FlowPane();
            flow.setHgap(20);
            flow.setVgap(20);
            flow.setPadding(new Insets(0, 0, 20, 0));
            
            for (Auction sp : entry.getValue()) {
                flow.getChildren().add(taoCardSanPham(sp));
            }
            
            productContainer.getChildren().addAll(lblCat, flow);
        }
    }

    // ============ HIỂN THỊ DẠNG LƯỚI CARD (khi lọc theo 1 category) ============
    private void hienThiLuoiCard(List<Auction> ds) {
        productContainer.getChildren().clear();
        
        FlowPane flow = new FlowPane();
        flow.setHgap(20);
        flow.setVgap(20);
        flow.setPadding(new Insets(10, 0, 20, 0));
        
        for (Auction sp : ds) {
            flow.getChildren().add(taoCardSanPham(sp));
        }
        productContainer.getChildren().add(flow);
    }

    // ============ TẠO CARD CHO 1 SẢN PHẨM ============
    private VBox taoCardSanPham(Auction sp) {
        // Card container
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 4);"
        );
        
        // Tên sản phẩm
        Label lblName = new Label(sp.getProductName());
        lblName.setWrapText(true);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        lblName.setPrefWidth(230);
        
        // Mô tả (giới hạn)
        String desc = sp.getDescription();
        if (desc == null) desc = "";
        if (desc.length() > 60) desc = desc.substring(0, 60) + "...";
        Label lblDesc = new Label(desc);
        lblDesc.setWrapText(true);
        lblDesc.setPrefWidth(230);
        lblDesc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        // Trạng thái badge
        Label lblStatus = new Label(sp.getStatus() != null ? sp.getStatus() : "Đang mở");
        String statusColor = "#27ae60";
        if ("ENDED".equalsIgnoreCase(sp.getStatus()) || "KẾT THÚC".equals(sp.getStatus())) {
            statusColor = "#95a5a6";
        }
        lblStatus.setStyle(
            "-fx-background-color: " + statusColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 3 10;" +
            "-fx-background-radius: 10;" +
            "-fx-font-size: 11px;"
        );
        
        // Giá hiện tại
        double giaHienTai = sp.getCurrentHighestBid() > 0 ? sp.getCurrentHighestBid() : sp.getStartPrice();
        Label lblPrice = new Label(String.format("%,.0f", giaHienTai) + " VNĐ");
        lblPrice.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
        mapGiaSanPham.put(String.valueOf(sp.getId()), lblPrice);
        
        // Input + Button đặt giá
        TextField txtBid = new TextField();
        txtBid.setPromptText("Nhập giá...");
        txtBid.setStyle("-fx-background-radius: 6;");
        txtBid.setPrefWidth(150);
        
        Button btnBid = new Button("Ra Giá");
        btnBid.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        btnBid.setOnAction(event -> {
            String currentUser = com.bidnova.utils.SessionManager.getUsername();
            if (currentUser == null || currentUser.isEmpty()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Bác chưa đăng nhập! Vui lòng đăng nhập tài khoản để tham gia đấu giá nhé.");
                alert.showAndWait();
                return;
            }
            xuLyDatGia(sp.getId(), txtBid.getText());
        });
        
        HBox bidBox = new HBox(10, txtBid, btnBid);
        bidBox.setAlignment(Pos.CENTER_LEFT);
        
        // Thêm tất cả vào card
        card.getChildren().addAll(lblName, lblDesc, lblStatus, lblPrice, bidBox);
        
        return card;
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
            String username = com.bidnova.utils.SessionManager.getUsername();
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
                            showAlert("Thất bại", res.getMessage()); // Hiển thị câu chửi của Server (ví dụ: Chậm chân rồi...)
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