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
                                String itemName = auctionJson.has("name") ? auctionJson.get("name").getAsString() : "Chưa có tên";
                                String highestBid = auctionJson.has("currentHighestBid") ? auctionJson.get("currentHighestBid").getAsString() : "0";
                                
                                dangDienRaContainer.getChildren().add(
                                    createAuctionCard(id, itemName, highestBid + " VNĐ", "Đang mở", false)
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

    private Node createAuctionCard(String id, String title, String price, String time, boolean isBanner) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
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

        Button actionBtn = new Button("Đấu giá ngay");
        actionBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");
        actionBtn.setMaxWidth(Double.MAX_VALUE);

        actionBtn.setOnAction(event -> {
            try {
                System.out.println("Đang mở sản phẩm có ID: " + id);

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/bidder/ItemDetailView.fxml"));
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

        infoBox.getChildren().addAll(titleLabel, priceLabel, timeLabel, actionBtn);
        card.getChildren().addAll(imagePlaceholder, infoBox);

        return card;
    }
}