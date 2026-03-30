package com.daugia.controllers.bidder;

import java.io.IOException;

import com.daugia.controllers.components.AuctionCardController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class DanhSachSanPhamController {

    @FXML private Label lblTitle;
    @FXML private FlowPane productContainer;

    private String currentCategory;

    public void setCategory(String categoryName) {
        this.currentCategory = categoryName;
        lblTitle.setText("Danh mục: " + categoryName);
        
        loadProducts();
    }

    private void loadProducts() {
        productContainer.getChildren().clear();

        
        for (int i = 1; i <= 10; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/AuctionCard.fxml"));
                Node cardView = loader.load();

                AuctionCardController cardController = loader.getController();

                cardController.setData(
                    currentCategory + " - SP " + i, 
                    "5,000,000", 
                    "02:00:00", 
                    "/images/UET-logo.png" 
                );

                productContainer.getChildren().add(cardView);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}