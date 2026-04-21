package com.daugia.controllers.common;

import javafx.scene.layout.Region;

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
import javafx.fxml.FXMLLoader;
import com.daugia.controllers.components.AuctionCardController;

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
            bannerContainer.getChildren().add(createAuctionCard("B1", "Tung Tung Tung Sahur", "5,000,000,000", "05:20:15", true));
            bannerContainer.getChildren().add(createAuctionCard("B2", "Tung Tung Tung Sahur", "5,000,000,000", "12:45:00", true));
        }

        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_AUCTIONS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    if (response.getPayload() == null) return;

                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String payloadJson = gson.toJson(response.getPayload());
                    JsonArray auctionsArray = JsonParser.parseString(payloadJson).getAsJsonArray();

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
        try {
           
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/AuctionCard.fxml"));
            Node card = loader.load();
            AuctionCardController controller = loader.getController();
            controller.setData(id, title, price, time);


            if (card instanceof Region) {
                ((Region) card).setPrefWidth(isBanner ? 580 : 280);
            }
            return card;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi load thẻ sản phẩm!");
            return new javafx.scene.control.Label("Lỗi tải thẻ"); 
        }
    }
    
}