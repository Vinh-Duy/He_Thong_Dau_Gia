package com.daugia.controllers.components;

import com.daugia.controllers.bidder.ItemDetailController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;    
import java.io.IOException;

public class AuctionCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private Button actionBtn;

    private String currentItemId;

    public void setData(String id, String title, String price, String time) {
        this.currentItemId = id;
        if (titleLabel != null) titleLabel.setText(title);
        if (priceLabel != null) priceLabel.setText(price);
        if (timeLabel != null) timeLabel.setText(time);
        
        String imagePath = "/images/Product.jpg"; 
        
        try {
            if (imgProduct != null) {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imgProduct.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    @FXML
    private void initialize() {
        if (actionBtn != null) {
            actionBtn.setOnAction(event -> handleBidClick());
        }
    }

    private void handleBidClick() {
        try {
        
            String fxmlPath = "/views/bidder/ItemDetailView.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent detailRoot = loader.load();

            ItemDetailController detailController = loader.getController();
            detailController.setAuctionId(currentItemId);

            Stage stage = (Stage) actionBtn.getScene().getWindow();

            stage.getScene().setRoot(detailRoot);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không thể tìm thấy hoặc tải file ItemDetailView.fxml");
        }
    }
    
}