package com.bidnova.controllers.components;

import com.bidnova.models.Auction;
import com.bidnova.controllers.bidder.AuctionDetailController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Parent;

public class AuctionCardController {
    @FXML private StackPane imgContainer;
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label startTime;
    @FXML private Label endTime;
    @FXML private Label lblTime;
    @FXML private Label lblStatus;
    @FXML private Button btn;

    private Auction auction;
    private Timeline countdownTimeline;

    // Cache để lưu lại ảnh đã tải, tránh tải lại nhiều lần gây chậm
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void setData(Auction auction) {
        this.auction = auction;

        // 1. Set các thông tin cơ bản
        lblName.setText(auction.getProductName());
        double displayPrice = auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice();
        lblPrice.setText(String.format("%,.0f VNĐ", displayPrice));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        startTime.setText(auction.getStartTime() != null ? auction.getStartTime().format(formatter) : "N/A");
        endTime.setText(auction.getEndTime() != null ? auction.getEndTime().format(formatter) : "N/A");

        // 2. Khởi tạo bộ đếm ngược Real-time
        if (countdownTimeline != null) countdownTimeline.stop();
        countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> refreshUI()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();

        refreshUI(); // Cập nhật lần đầu

        // 3. Xử lý ảnh (Giữ nguyên logic cũ nhưng gọn hơn)
        String imagePath = auction.getImageUrl() != null ? auction.getImageUrl() : "/images/default_item.png";
        handleImageLoading(imagePath);
    }

    private void refreshUI() {
        if (auction == null) return;

        String status = determineStatus(auction);
        String timeLeft = calculateTimeLeft(auction);

        lblStatus.setText(status);
        lblTime.setText(timeLeft);

        if (status.equals("Sắp diễn ra")) {
            lblStatus.setStyle("-fx-background-color: #CFBA1D");
            btn.setDisable(false);
            btn.setCursor(Cursor.HAND);
        } else if (status.equals("Đang diễn ra")) {
            lblStatus.setStyle("-fx-background-color: #0E8716");
            btn.setDisable(false);
            btn.setCursor(Cursor.HAND);
        } else { // Đã kết thúc
            lblStatus.setStyle("-fx-background-color: #b41712");
            btn.setDisable(true); // Tự động làm mờ và chặn click
            btn.setCursor(Cursor.DEFAULT); // Đưa cursor về mặc định
            if (countdownTimeline != null) countdownTimeline.stop();
        }
    }

    private void handleImageLoading(String imagePath) {
        // Thiết lập placeholder trước để UI không bị trống
        imgContainer.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5 5 0 0;");

        try {
            if (imagePath != null) {
                Image image = imageCache.get(imagePath);
                
                if (image == null) {
                    // Load ảnh với kích thước cố định (300x200) để JavaFX xử lý nhanh hơn rất nhiều
                    // tham số: url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading
                    if (imagePath.startsWith("http")) {
                        image = new Image(imagePath, 300, 200, true, true, true);
                    } else {
                        String internalPath = getClass().getResource(imagePath).toExternalForm();
                        image = new Image(internalPath, 300, 200, true, true, true);
                    }
                    imageCache.put(imagePath, image);
                }

                // Khi ảnh load xong (hoặc lấy từ cache), set làm background cho Pane
                final Image finalImg = image;
                if (finalImg.isBackgroundLoading() && finalImg.getProgress() < 1.0) {
                    finalImg.progressProperty().addListener((obs, old, progress) -> {
                        if (progress.doubleValue() >= 1.0) Platform.runLater(() -> setPaneBackground(finalImg));
                    });
                } else {
                    setPaneBackground(finalImg);
                }
            }
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    private void setPaneBackground(Image image) {
        if (image == null) return;
        // Sử dụng CSS inline để set background-image. 
        // Cách này giúp đạt được hiệu ứng "Cover" (chiếm toàn bộ khung hình và center)
        String url = image.getUrl();
        imgContainer.setStyle("-fx-background-image: url('" + url + "'); " +
                             "-fx-background-size: cover; " +
                             "-fx-background-position: center;");
    }

    private String determineStatus(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (auction.getStartTime() == null || auction.getStartTime().isAfter(now)) return "Sắp diễn ra";
        if (auction.getEndTime() != null && auction.getEndTime().isAfter(now)) return "Đang diễn ra";
        return "Đã kết thúc";
    }

    private String calculateTimeLeft(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end = auction.getEndTime();

        if (start == null || end == null) return "N/A";
        if (now.isAfter(end)) return "Đã kết thúc";

        boolean isUpcoming = now.isBefore(start);
        // Sử dụng java.time.Duration
        Duration duration = Duration.between(now, isUpcoming ? start : end);
        String prefix = isUpcoming ? "Bắt đầu sau " : "";

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 0) return String.format("%s%d ngày %d giờ %d phút %d giây", prefix, days, hours, minutes, seconds);
        if (hours > 0) return String.format("%s%d giờ %d phút %d giây", prefix, hours, minutes, seconds);
        if (minutes > 0) return String.format("%s%d phút %d giây", prefix, minutes, seconds);
        return String.format("%s%d giây", prefix, seconds);
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) imgContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBidClick() {
        try {
            Stage stage = (Stage) imgContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/auction-detail-view.fxml"));
            Parent root = loader.load();
            
            // Lấy controller của màn hình chi tiết và truyền dữ liệu
            AuctionDetailController controller = loader.getController();
            controller.setAuction(this.auction);
            
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển sang màn hình chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }
}