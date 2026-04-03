package com.daugia.controllers.bidder;

import java.io.IOException;

import com.daugia.controllers.components.AuctionCardController;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
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

        new Thread(() -> {
            try {
                Request request = new Request("GET_ITEMS_BY_CATEGORY", currentCategory);
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    if (response.getPayload() == null) {
                        System.out.println("Server trả về payload rỗng!");
                        return;
                    }

                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String payloadJson = gson.toJson(response.getPayload());
                    
                    JsonArray itemsArray = JsonParser.parseString(payloadJson).getAsJsonArray();

                    Platform.runLater(() -> {
                        for (int i = 0; i < itemsArray.size(); i++) {
                            JsonObject itemJson = itemsArray.get(i).getAsJsonObject();
                            
                            String tenSP = itemJson.has("name") ? itemJson.get("name").getAsString() : "Đang cập nhật";
                            String gia = itemJson.has("startingPrice") ? itemJson.get("startingPrice").getAsString() : "0";
                            
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/AuctionCard.fxml"));
                                Node cardView = loader.load();
                                AuctionCardController cardController = loader.getController();

                                cardController.setData(tenSP, gia + " VNĐ", "Đang mở", "/images/default_item.png");
                                productContainer.getChildren().add(cardView);
                            } catch (IOException e) {
                                System.out.println("Lỗi load card: " + e.getMessage());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("Lỗi mạng: " + e.getMessage());
            }
        }).start();
    }
}