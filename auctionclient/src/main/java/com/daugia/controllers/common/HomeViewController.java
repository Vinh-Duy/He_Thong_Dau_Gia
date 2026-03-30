package com.daugia.controllers.common;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeViewController {

    @FXML
    private BorderPane mainRoot;

    @FXML private VBox welcomeImageHolder;

    @FXML private HBox bannerContainer;
    @FXML private FlowPane dangDienRaContainer;
    @FXML private FlowPane sapDienRaContainer;

    @FXML
    public void initialize() {
        loadMockData();
    }

    private void loadMockData() {
        bannerContainer.getChildren().clear();
        dangDienRaContainer.getChildren().clear();

        bannerContainer.getChildren().add(createAuctionCard("Siêu xe Lamborghini Aventador", "5,000,000,000 VNĐ", "05:20:15", true));
        bannerContainer.getChildren().add(createAuctionCard("Biệt thự biển Đà Nẵng", "25,000,000,000 VNĐ", "12:45:00", true));

        dangDienRaContainer.getChildren().add(createAuctionCard("Đồng hồ Rolex Datejust", "320,000,000 VNĐ", "00:45:10", false));
        dangDienRaContainer.getChildren().add(createAuctionCard("Tranh sơn dầu Phố Cổ", "45,000,000 VNĐ", "02:15:30", false));
        dangDienRaContainer.getChildren().add(createAuctionCard("Xe Honda SH 350i", "150,000,000 VNĐ", "01:10:05", false));
        dangDienRaContainer.getChildren().add(createAuctionCard("Sim số đẹp 09xxx88888", "500,000,000 VNĐ", "00:12:40", false));
    }

    private VBox createAuctionCard(String title, String price, String time, boolean isBanner) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.getStyleClass().add("auction-card");
        
        if (isBanner) {
            card.setPrefWidth(580);
        } else {
            card.setPrefWidth(280);
        }

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.setStyle("-fx-background-color: #e1bee7; -fx-background-radius: 10 10 0 0;");
        imagePlaceholder.setPrefHeight(isBanner ? 180 : 160);
        Label imgLabel = new Label("Ảnh tài sản");
        imgLabel.setStyle("-fx-text-fill: #6a1b9a; -fx-font-weight: bold;");
        imagePlaceholder.getChildren().add(imgLabel);

        VBox infoBox = new VBox();
        infoBox.setSpacing(8);
        infoBox.setPadding(new Insets(15));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        titleLabel.setWrapText(true);

        Label priceLabel = new Label("Giá k.điểm: " + price);
        priceLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");

        Button actionBtn = new Button(isBanner ? "XEM CHI TIẾT" : "THAM GIA");
        actionBtn.getStyleClass().add(isBanner ? "btn-galaxy-filled" : "btn-galaxy-transparent");
        actionBtn.setMaxWidth(Double.MAX_VALUE);

        infoBox.getChildren().addAll(titleLabel, priceLabel, timeLabel, actionBtn);
        card.getChildren().addAll(imagePlaceholder, infoBox);

        return card;
    }
}