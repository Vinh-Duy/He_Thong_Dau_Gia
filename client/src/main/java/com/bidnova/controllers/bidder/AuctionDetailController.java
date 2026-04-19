package com.bidnova.controllers.bidder;

import com.bidnova.models.Product;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class AuctionDetailController {
    @FXML
    private Label lblProductName;

    @FXML
    private Pane imgProduct;

    public void setData(Product product) {
        this.lblProductName.setText(product.getName());

        imgProduct.setStyle("-fx-background-image: url('" + product.getImagePath()
                + "'); -fx-background-size: cover; -fx-background-position: center");
    }
}
