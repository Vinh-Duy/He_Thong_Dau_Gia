package com.bidnova.controllers.bidder;

import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.controllers.components.AuctionCardController;
import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.NotificationUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class CategoryController {
    @FXML private Label lblTitle;
    @FXML private FlowPane productContainer;

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public void setCategory(String categoryName) {
        // Xóa các card cũ trước khi load danh mục mới
        productContainer.getChildren().clear();
        lblTitle.setText(categoryName);
        
        // Gọi server để lấy dữ liệu
        loadProductsFromServer();

        // 🔴 Lắng nghe real-time updates từ server
        NetworkClient.getInstance().onMessageReceived(message -> {
            try {
                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                String action = data.has("action") ? data.get("action").getAsString() : null;
                if ("AUCTION_LIST_UPDATE".equals(action)) {
                    Platform.runLater(this::loadProductsFromServer);
                } else if ("AUCTION_FINISHED".equals(action)) {
                    JsonObject payload = JsonParser.parseString(data.get("payload").getAsString()).getAsJsonObject();
                    String winner = payload.has("highestBidder") ? payload.get("highestBidder").getAsString() : "Không có người thắng";
                    String finalPrice = payload.has("finalBid") ? payload.get("finalBid").getAsString() : "0";
                    Platform.runLater(() -> {
                        NotificationUtil.showTopBanner("Đấu giá kết thúc", "Người thắng: " + winner + " - Giá: " + finalPrice);
                        loadProductsFromServer();
                    });
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý broadcast message: " + e.getMessage());
            }
        });
    }

    @FXML
    public void initialize() {
        // Khởi tạo các thông số UI nếu cần
    }

    private void loadProductsFromServer() {
        new Thread(() -> {
            try {
                Request request = new Request("GET_AUCTIONS_BY_CATEGORY", lblTitle.getText());
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    List<Auction> auctions = gson.fromJson(response.getData().toString(),
                            new TypeToken<List<Auction>>() {}.getType());

                    Platform.runLater(() -> {
                        for (Auction auction : auctions) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/auction-card.fxml"));
                                Parent card = loader.load();
                                
                                AuctionCardController controller = loader.getController();
                                controller.setData(auction);
                                
                                productContainer.getChildren().add(card);
                            } catch (Exception e) {
                                System.err.println("Lỗi khi load AuctionCard: " + e.getMessage());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Hàm tiện ích để hiển thị thông báo Popup cho nhanh
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}