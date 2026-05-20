package com.bidnova.controllers.common;

import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

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
        loadData(); // Gọi loadData thay vì loadMockData
    }

    /* Lấy dữ liệu từ database */
    private void loadData() {
        // Xóa nội dung cũ trước khi tải mới
        if (bannerContainer != null) bannerContainer.getChildren().clear();
        if (dangDienRaContainer != null) dangDienRaContainer.getChildren().clear();
        if (sapDienRaContainer != null) sapDienRaContainer.getChildren().clear();

        new Thread(() -> {
            try {
                Request request = new Request("GET_ALL_AUCTIONS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    if (response.getData() == null || response.getData().toString().equals("[]")) {
                        Platform.runLater(() -> {
                            Label emptyLabel = new Label("Hiện chưa có phiên đấu giá nào.");
                            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
                            dangDienRaContainer.getChildren().add(emptyLabel);
                        });
                        return;
                    }

                    // Parse JSON response vào List các đối tượng Auction
                    java.lang.reflect.Type listType = new TypeToken<List<Auction>>(){}.getType();
                    List<Auction> allAuctions = gson.fromJson(response.getData().toString(), listType);

                    Platform.runLater(() -> {
                        List<Auction> ongoingAuctions = new ArrayList<>();
                        List<Auction> upcomingAuctions = new ArrayList<>();
                        LocalDateTime now = LocalDateTime.now();

                        for (Auction auction : allAuctions) {
                            // Đảm bảo startTime và endTime không null trước khi so sánh
                            if (auction.getStartTime() != null && auction.getEndTime() != null) {
                                if (auction.getStartTime().isAfter(now)) {
                                    upcomingAuctions.add(auction);
                                } else if (auction.getEndTime().isAfter(now)) {
                                    ongoingAuctions.add(auction);
                                }
                                // Các phiên đã kết thúc sẽ không hiển thị ở đây
                            }
                        }

                        // Đổ dữ liệu vào banner (vài phiên đang diễn ra đầu tiên)
                        int bannerCount = Math.min(ongoingAuctions.size(), 2); // Hiển thị tối đa 2 banner
                        for (int i = 0; i < bannerCount; i++) {
                            bannerContainer.getChildren().add(createAuctionCard(ongoingAuctions.get(i), true));
                        }
                        // Xóa các mục đã hiển thị trên banner khỏi danh sách đang diễn ra để tránh trùng lặp
                        if (bannerCount > 0) {
                            ongoingAuctions.subList(0, bannerCount).clear();
                        }

                        // Đổ dữ liệu vào phần "Đang diễn ra"
                        if (ongoingAuctions.isEmpty()) {
                            Label emptyLabel = new Label("Hiện chưa có phiên đấu giá nào đang diễn ra.");
                            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
                            dangDienRaContainer.getChildren().add(emptyLabel);
                        } else {
                            for (Auction auction : ongoingAuctions) {
                                dangDienRaContainer.getChildren().add(createAuctionCard(auction, false));
                            }
                        }

                        // Đổ dữ liệu vào phần "Sắp diễn ra"
                        if (upcomingAuctions.isEmpty()) {
                            Label emptyLabel = new Label("Hiện chưa có phiên đấu giá nào sắp diễn ra.");
                            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 20px;");
                            sapDienRaContainer.getChildren().add(emptyLabel);
                        } else {
                            for (Auction auction : upcomingAuctions) {
                                sapDienRaContainer.getChildren().add(createAuctionCard(auction, false));
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Đã xảy ra lỗi khi tải dữ liệu: " + e.getMessage());
                    errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-padding: 20px;");
                    dangDienRaContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    // Cập nhật hàm createAuctionCard để nhận đối tượng Auction
    private Node createAuctionCard(Auction auction, boolean isBanner) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 15, 0, 0, 6);");
        
        if (isBanner) {
            card.setPrefWidth(580);
        } else {
            card.setPrefWidth(280);
        }

        // Container cho ảnh (sử dụng StackPane để có thể đặt ảnh hoặc placeholder)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(isBanner ? 180 : 160);
        imageContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #e1bee7, #ce93d8); "
            + "-fx-background-radius: 12 12 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(isBanner ? 160 : 140);
        imageView.setFitWidth(isBanner ? 540 : 240);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true); // Để ảnh hiển thị mượt mà hơn

        if (auction.getImageUrl() != null && !auction.getImageUrl().isEmpty()) {
            // Tải ảnh từ URL
            Image image = new Image(auction.getImageUrl(), true); // true để tải ảnh nền
            imageView.setImage(image);
            imageContainer.getChildren().add(imageView);
        } else {
            // Hiển thị chữ placeholder nếu không có ảnh
            Label imgPlaceholderLabel = new Label("Không có ảnh");
            imgPlaceholderLabel.setStyle("-fx-text-fill: #6a1b9a; -fx-font-weight: bold; -fx-font-size: 14px;");
            imageContainer.getChildren().add(imgPlaceholderLabel);
        }
        StackPane.setAlignment(imageView, Pos.CENTER); // Căn giữa ảnh trong StackPane
        StackPane.setAlignment(imageContainer.getChildren().get(0), Pos.CENTER); // Căn giữa label placeholder

        // Info section
        VBox infoBox = new VBox();
        infoBox.setSpacing(10);
        infoBox.setPadding(new Insets(18));

        // Title
        Label titleLabel = new Label(auction.getProductName());
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(isBanner ? 540 : 240);

        // Price
        double displayPrice = auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice();
        String priceStr = String.format("%,.0f VNĐ", displayPrice);
        Label priceLabel = new Label(priceStr);
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        // Status badge
        String statusText;
        String statusColor;
        LocalDateTime now = LocalDateTime.now();

        if (auction.getStartTime() != null && auction.getStartTime().isAfter(now)) {
            statusText = "Sắp diễn ra";
            statusColor = "#f39c12"; // Màu cam cho sắp diễn ra
        } else if (auction.getEndTime() != null && auction.getEndTime().isAfter(now)) {
            statusText = "Đang diễn ra";
            statusColor = "#27ae60"; // Màu xanh lá cho đang diễn ra
        } else {
            statusText = "Đã kết thúc";
            statusColor = "#95a5a6"; // Màu xám cho đã kết thúc
        }
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; "
            + "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Nút hành động
        Button actionBtn = new Button("Xem chi tiết"); // Đổi text thành "Xem chi tiết"
        actionBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; "
            + "-fx-font-size: 14px;");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setPrefHeight(40);

        actionBtn.setOnAction(event -> {
            try {
                System.out.println("Đang mở sản phẩm có ID: " + auction.getId());
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/bidder/ItemDetailView.fxml"));
                javafx.scene.Parent detailRoot = loader.load();
                com.bidnova.controllers.bidder.ItemDetailController controller = loader.getController();
                controller.setAuctionId(auction.getId()); // Truyền ID thực tế của phiên đấu giá
                javafx.stage.Stage stage = (javafx.stage.Stage) actionBtn.getScene().getWindow();
                stage.getScene().setRoot(detailRoot);
            } catch (Exception e) {
                System.out.println("Lỗi khi chuyển sang trang Chi Tiết: " + e.getMessage());
                e.printStackTrace();
            }
        });

        infoBox.getChildren().addAll(titleLabel, priceLabel, statusLabel, actionBtn);
        card.getChildren().addAll(imageContainer, infoBox); // Thêm imageContainer

        return card;
    }
}