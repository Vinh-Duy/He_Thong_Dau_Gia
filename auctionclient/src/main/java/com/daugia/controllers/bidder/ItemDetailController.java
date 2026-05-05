package com.daugia.controllers.bidder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ItemDetailController {

    @FXML private Button btnBack;
    @FXML private ImageView imgItem;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentBid;
    @FXML private TextField txtBidInput;
    @FXML private Button btnPlaceBid;
    @FXML private Label lblBidError;
    @FXML private TextArea txtDescription;

    private String currentAuctionId;
    private long currentPriceValue = 0;

    @FXML
    public void initialize() {
        System.out.println("ItemDetailController đã được khởi tạo!");
        
        txtBidInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
            lblBidError.setVisible(false);
        });
    }

    public void setAuctionId(String id) {
        this.currentAuctionId = id.equals("B1") ? "A001" : id; 
        System.out.println("Trang chi tiết đã nhận được ID thật là: " + currentAuctionId);
        
        if ("A001".equals(currentAuctionId)) {
            lblItemName.setText("Siêu xe Lamborghini Aventador SVJ - Phiên bản giới hạn");
            this.currentPriceValue = 5000000000L;
            txtDescription.setText("Chiếc Lamborghini Aventador SVJ màu xanh cốm cực hiếm. \n" +
                                   "Năm sản xuất: 2022. Tình trạng: Mới 99%.");
        } else {
            lblItemName.setText("Sản phẩm từ Server (ID: " + currentAuctionId + ")");
            this.currentPriceValue = 1500000L;
            txtDescription.setText("Đây là sản phẩm được lấy từ danh sách đang diễn ra trên Server.");
        }
        
        lblCurrentBid.setText(formatVietnameseCurrency(this.currentPriceValue));
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/common/HomeView.fxml"));
            javafx.scene.Parent homeRoot = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
            stage.getScene().setRoot(homeRoot);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay lại trang chủ!");
        }
    }

    @FXML
    private void handlePlaceBid() {
        String bidText = txtBidInput.getText();
        if (bidText.isEmpty()) {
            lblBidError.setText("Vui lòng nhập số tiền!");
            lblBidError.setVisible(true);
            return;
        }

        try {
            long placedBidValue = Long.parseLong(bidText);
            if (placedBidValue <= currentPriceValue) {
                lblBidError.setText("Giá đặt phải lớn hơn Giá hiện tại!");
                lblBidError.setVisible(true);
                return;
            }

            System.out.println("BẮT ĐẦU GỬI MẠNG CHO ID: " + currentAuctionId);
            
            // KẾT NỐI SERVER THẬT
            // 1. Tạo chuỗi JSON thủ công để gửi đi (giả sử user đang mượn tên là 'guest')
            String payload = String.format("{\\\"auctionId\\\":\\\"%s\\\", \\\"amount\\\":%d, \\\"username\\\":\\\"guest\\\"}", 
                                            currentAuctionId, placedBidValue);
            String jsonRequest = String.format("{\"action\":\"PLACE_BID\",\"payload\":\"%s\"}", payload);

            // 2. Mở kết nối Socket tới Server (localhost:8888)
            try (Socket socket = new Socket("localhost", 8888);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // true = auto flush
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                // Gửi lệnh sang Server (println BẮT BUỘC có để tạo dấu Enter \n)
                out.println(jsonRequest);
                System.out.println("Đã bắn tín hiệu: " + jsonRequest);

                // Chờ Server phản hồi
                String responseLine = in.readLine();
                System.out.println("Server báo về: " + responseLine);

                // Xử lý nếu Server báo thành công
                if (responseLine != null && responseLine.contains("\"SUCCESS\"")) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bạn đã đặt giá " + formatVietnameseCurrency(placedBidValue) + " thành công!");
                    currentPriceValue = placedBidValue;
                    lblCurrentBid.setText(formatVietnameseCurrency(currentPriceValue));
                    txtBidInput.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi từ server, giá chưa được cập nhật!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không kết nối được với Server! (Bật server chưa?)");
            }
            // ----------------------------------------------------

        } catch (NumberFormatException e) {
            lblBidError.setText("Số tiền không hợp lệ!");
            lblBidError.setVisible(true);
        }
    }

    private String formatVietnameseCurrency(long amount) {
        Locale localeVN = new Locale("vi", "VN");
        java.text.NumberFormat vnCurrencyFormat = java.text.NumberFormat.getCurrencyInstance(localeVN);
        return vnCurrencyFormat.format(amount);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}