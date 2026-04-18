package com.bidnova.controllers.components;

import com.bidnova.models.Product;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// nhận vào một đối tượng kiểu Product và lấy các thông số của nó để set cho this: ảnh, tên, giá tiền
public class ProductCardController {
    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblName;

    @FXML
    private Label lblPrice;

    public void setData(Product product) {
        lblName.setText(product.getName());
        lblPrice.setText("Giá khởi điểm: " + String.format("%,d VNĐ", product.getStartingPrice()));

        Image image = new Image(getClass().getResourceAsStream(product.getImagePath()));
        imgProduct.setImage(image);
    }
}
