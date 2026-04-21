package com.daugia.controllers.bidder;

import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

import org.w3c.dom.Node;

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
        this.currentAuctionId = id;
        System.out.println("Trang chi tiết đã nhận được ID: " + id);
        
        if ("B1".equals(id)) {
            lblItemName.setText("Siêu xe Lamborghini Aventador SVJ - Phiên bản giới hạn");
            this.currentPriceValue = 5000000000L;
            txtDescription.setText("Chiếc Lamborghini Aventador SVJ màu xanh cốm cực hiếm. \n" +
                                   "Năm sản xuất: 2022. Tình trạng: Mới 99%.");
        } 
        else if ("B2".equals(id)) {
            lblItemName.setText("Biệt thự biển Đà Nẵng siêu xịn xò");
            this.currentPriceValue = 25000000000L;
            txtDescription.setText("Biệt thự lô góc view thẳng ra biển Mỹ Khê. \n" +
                                   "Diện tích: 500m2. Sổ đỏ chính chủ, sang tên trong ngày.");
        } 
        else {
            lblItemName.setText("Sản phẩm từ Server (ID: " + id + ")");
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

            System.out.println("Đang gửi lệnh đặt giá " + placedBidValue + " cho ID " + currentAuctionId);
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                     "Bạn đã đặt giá " + formatVietnameseCurrency(placedBidValue) + " thành công!");
            
            currentPriceValue = placedBidValue;
            lblCurrentBid.setText(formatVietnameseCurrency(currentPriceValue));
            txtBidInput.clear();

        } catch (NumberFormatException e) {
            lblBidError.setText("Số tiền không hợp lệ!");
            lblBidError.setVisible(true);
        }
    }

    private String formatVietnameseCurrency(long amount) {
        Locale localeVN = new Locale("vi", "VN");
        Currency currencyVN = Currency.getInstance(localeVN);
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