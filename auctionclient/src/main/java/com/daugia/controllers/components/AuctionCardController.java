package com.daugia.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AuctionCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;

    public void setData(/* Item item */ String name, String price, String time, String imagePath) {
        lblName.setText(name);
        lblPrice.setText(price + " VNĐ");
        lblTime.setText("Thời gian: " + time);
        
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imgProduct.setImage(image);
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    @FXML
    private void handleBidClick() {
        System.out.println("Chuyển sang màn hình chi tiết của sản phẩm: " + lblName.getText());
    }
}