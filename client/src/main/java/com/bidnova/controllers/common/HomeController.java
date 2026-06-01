package com.bidnova.controllers.common;

import com.bidnova.controllers.components.AuctionCardController;
import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private HBox bannerContainer;
    @FXML private FlowPane dangDienRaContainer;
    @FXML private FlowPane sapDienRaContainer;

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @FXML
    public void initialize() {
        loadData();

        // 🔴 Lắng nghe real-time updates từ server
        NetworkClient.getInstance().onMessageReceived(message -> {
            try {
                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                if (data.has("action") && "AUCTION_LIST_UPDATE".equals(data.get("action").getAsString())) {
                    // Reload dữ liệu khi có product update
                    Platform.runLater(this::loadData);
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý broadcast message: " + e.getMessage());
            }
        });
    }

    /* Lấy dữ liệu từ database */
    private void loadData() {
        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_AUCTIONS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    processAuctions(response);
                }
            } catch (Exception e) {
                showErrorUI(e.getMessage());
            }
        }).start();
    }

    private void processAuctions(Response response) {
        List<Auction> allAuctions = new ArrayList<>();
        try {
            Object data = response.getData();
            if (data == null) return;
            
            // Kiểm tra: Nếu data đã là String (JSON từ server), dùng luôn. Nếu là Object, mới dùng toJson.
            // Việc này tránh lỗi double-encode dẫn đến "Expected BEGIN_ARRAY but was STRING"
            String jsonData = (data instanceof String) ? (String) data : gson.toJson(data);
            allAuctions = gson.fromJson(jsonData, new TypeToken<List<Auction>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Lỗi parse dữ liệu đấu giá: " + e.getMessage());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<Auction> ongoing = new ArrayList<>();
        List<Auction> upcoming = new ArrayList<>();

        for (Auction a : allAuctions) {
            if (a.getStartTime() == null || a.getEndTime() == null) continue;
            if (a.getStartTime().isAfter(now)) upcoming.add(a);
            else if (a.getEndTime().isAfter(now)) ongoing.add(a);
        }

        Platform.runLater(() -> {
            // Xóa nội dung cũ ngay trước khi đổ dữ liệu mới để tránh bị trống màn hình quá lâu
            if (bannerContainer != null) bannerContainer.getChildren().clear();
            if (dangDienRaContainer != null) dangDienRaContainer.getChildren().clear();
            if (sapDienRaContainer != null) sapDienRaContainer.getChildren().clear();
            
            populateUI(ongoing, upcoming);
        });
    }

    private void populateUI(List<Auction> ongoing, List<Auction> upcoming) {
        // Banner logic
        int bannerCount = Math.min(ongoing.size(), 2);
        for (int i = 0; i < bannerCount; i++) {
            bannerContainer.getChildren().add(createAuctionCard(ongoing.get(i), true));
        }

        // Ongoing list logic
        List<Auction> remainingOngoing = ongoing.size() > bannerCount 
            ? ongoing.subList(bannerCount, ongoing.size()) : new ArrayList<>();
        
        if (remainingOngoing.isEmpty() && bannerCount == 0) {
            dangDienRaContainer.getChildren().add(createEmptyLabel("Hiện chưa có phiên nào đang diễn ra."));
        } else {
            remainingOngoing.forEach(a -> dangDienRaContainer.getChildren().add(createAuctionCard(a, false)));
        }

        // Upcoming logic
        if (upcoming.isEmpty()) {
            sapDienRaContainer.getChildren().add(createEmptyLabel("Hiện chưa có phiên nào sắp diễn ra."));
        } else {
            upcoming.forEach(a -> sapDienRaContainer.getChildren().add(createAuctionCard(a, false)));
        }
    }

    private void showErrorUI(String msg) {
        Platform.runLater(() -> dangDienRaContainer.getChildren().add(createEmptyLabel("Lỗi: " + msg)));
    }

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
        return label;
    }

    private Node createAuctionCard(Auction auction, boolean isBanner) {
        try {
            // 1. Load FXML của component card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/auction-card.fxml"));
            VBox card = loader.load();

            // 2. Lấy controller của card đó
            AuctionCardController controller = loader.getController();

            // 3. Đổ dữ liệu vào card - Card sẽ tự lo phần countdown real-time
            controller.setData(auction);

            // Nếu là banner, ta có thể chỉnh lại kích thước thủ công nếu muốn
            if (isBanner) {
                card.setPrefWidth(580);
                card.setMinWidth(580);
            }

            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return new Label("Lỗi khi tải thông tin đấu giá.");
        }
    }

}