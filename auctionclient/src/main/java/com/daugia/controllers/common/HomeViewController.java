package com.daugia.controllers.common;

import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeViewController {

    @FXML private BorderPane mainRoot;
    @FXML private VBox welcomeImageHolder;
    @FXML private HBox bannerContainer;
    @FXML private FlowPane dangDienRaContainer;
    @FXML private FlowPane sapDienRaContainer;

    @FXML
    public void initialize() {
        loadMockData();
    }

    private void loadMockData() {
        if (bannerContainer != null) bannerContainer.getChildren().clear();
        if (dangDienRaContainer != null) dangDienRaContainer.getChildren().clear();
        if (sapDienRaContainer != null) sapDienRaContainer.getChildren().clear();

        if (bannerContainer != null) {
            bannerContainer.getChildren().add(createAuctionCard("B1", "Siêu xe Lamborghini Aventador", "5,000,000,000 VNĐ", "05:20:15", true));
            bannerContainer.getChildren().add(createAuctionCard("B2", "Biệt thự biển Đà Nẵng", "25,000,000,000 VNĐ", "12:45:00", true));
        }

        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_AUCTIONS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    if (response.getPayload() == null) return;

                    // Ép trực tiếp Payload thành chuỗi String, không dùng gson.toJson nữa
                    String payloadString = String.valueOf(response.getPayload());
                    JsonArray auctionsArray = JsonParser.parseString(payloadString).getAsJsonArray();

                    Platform.runLater(() -> {

                        if (dangDienRaContainer != null) {
                            for (int i = 0; i < auctionsArray.size(); i++) {
                                JsonObject auctionJson = auctionsArray.get(i).getAsJsonObject();
                                
                                String id = auctionJson.has("id") ? auctionJson.get("id").getAsString() : "ITEM_" + i;
                                String itemName = auctionJson.has("productName") ? auctionJson.get("productName").getAsString() : 
                                                    (auctionJson.has("name") ? auctionJson.get("name").getAsString() : "Chưa có tên");
                                
                                double currentHighestBid = 0;
                                if (auctionJson.has("currentHighestBid")) {
                                    currentHighestBid = auctionJson.get("currentHighestBid").getAsDouble();
                                }
                                
                                double startPrice = 0;
                                if (auctionJson.has("startPrice")) {
                                    startPrice = auctionJson.get("startPrice").getAsDouble();
                                } else if (auctionJson.has("startingPrice")) {
                                    startPrice = auctionJson.get("startingPrice").getAsDouble();
                                }
                                
                                double displayPrice = currentHighestBid > 0 ? currentHighestBid : startPrice;
                                String priceStr = String.format("%,.0f VNĐ", displayPrice);
                                
                                String status = auctionJson.has("status") ? auctionJson.get("status").getAsString() : "OPEN";
                                
                                dangDienRaContainer.getChildren().add(
                                    createAuctionCard(id, itemName, priceStr, status, false)
                                );
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Node createAuctionCard(String id, String title, String price, String status, boolean isBanner) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 15, 0, 0, 6);");
        
        if (isBanner) {
            card.setPrefWidth(580);
        } else {
            card.setPrefWidth(280);
        }

        // Image placeholder
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.setStyle("-fx-background-color: linear-gradient(to bottom right, #e1bee7, #ce93d8); "
            + "-fx-background-radius: 12 12 0 0;");
        imagePlaceholder.setPrefHeight(isBanner ? 180 : 160);
        Label imgLabel = new Label("Ảnh tài sản");
        imgLabel.setStyle("-fx-text-fill: #6a1b9a; -fx-font-weight: bold; -fx-font-size: 14px;");
        imagePlaceholder.getChildren().add(imgLabel);

        // Info section
        VBox infoBox = new VBox();
        infoBox.setSpacing(10);
        infoBox.setPadding(new Insets(18));

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(isBanner ? 540 : 240);

        // Price
        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        // Status badge
        String statusColor = "OPEN".equals(status) || "RUNNING".equals(status) 
            ? "#27ae60" : "#e74c3c";
        String statusText = "OPEN".equals(status) || "RUNNING".equals(status) 
            ? "Đang mở" : "Đã đóng";
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; "
            + "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Button
        Button actionBtn = new Button("Đấu giá ngay");
        actionBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; "
            + "-fx-font-size: 14px;");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setPrefHeight(40);

        actionBtn.setOnAction(event -> {
            try {
                System.out.println("Đang mở sản phẩm có ID: " + id);
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/bidder/ItemDetailView.fxml"));
                javafx.scene.Parent detailRoot = loader.load();
                com.daugia.controllers.bidder.ItemDetailController controller = loader.getController();
                controller.setAuctionId(id);
                javafx.stage.Stage stage = (javafx.stage.Stage) actionBtn.getScene().getWindow();
                stage.getScene().setRoot(detailRoot);
            } catch (Exception e) {
                System.out.println("Lỗi khi chuyển sang trang Chi Tiết: " + e.getMessage());
                e.printStackTrace();
            }
        });

        infoBox.getChildren().addAll(titleLabel, priceLabel, statusLabel, actionBtn);
        card.getChildren().addAll(imagePlaceholder, infoBox);

        return card;
    }
}