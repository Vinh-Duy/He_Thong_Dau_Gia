package com.bidnova.controllers.components;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.bidnova.controllers.bidder.AuctionDetailController;
import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AuctionCardController {
    @FXML private StackPane imgContainer;
    @FXML private StackPane imgWrapper;
    @FXML private Label lblName;
    @FXML private Label lblPrice;
    @FXML private Label startTime;
    @FXML private Label endTime;
    @FXML private Label lblTime;
    @FXML private Label lblStatus;
    @FXML private Button btn;

    private Auction auction;
    private Timeline countdownTimeline;
    private Consumer<String> realTimeListener;

    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    private ScaleTransition scaleIn;
    private ScaleTransition scaleOut;

    public void setData(Auction auction) {
        detachRealTimeListener();
        this.auction = auction;

        lblName.setText(auction.getProductName());
        double displayPrice = auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice();
        lblPrice.setText(String.format("%,.0f VNĐ", displayPrice));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        startTime.setText(auction.getStartTime() != null ? auction.getStartTime().format(formatter) : "N/A");
        endTime.setText(auction.getEndTime() != null ? auction.getEndTime().format(formatter) : "N/A");

        if (countdownTimeline != null) countdownTimeline.stop();
        countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> refreshUI()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();

        refreshUI();
        registerRealTimeListener();

        setupZoomEffect();

        String imagePath = auction.getImageUrl() != null ? auction.getImageUrl() : "/images/default_item.png";
        handleImageLoading(imagePath);
    }

    private void refreshUI() {
        if (auction == null) return;

        String status = determineStatus(auction);
        String timeLeft = calculateTimeLeft(auction);

        lblStatus.setText(status);
        lblTime.setText(timeLeft);

        if ("Sắp diễn ra".equals(status)) {
            lblStatus.setStyle("-fx-background-color: #CFBA1D");
            btn.setDisable(false);
            btn.setCursor(Cursor.HAND);
        } else if ("Đang diễn ra".equals(status)) {
            lblStatus.setStyle("-fx-background-color: #0E8716");
            btn.setDisable(false);
            btn.setCursor(Cursor.HAND);
        } else {
            lblStatus.setStyle("-fx-background-color: #b41712");
            btn.setDisable(true);
            btn.setCursor(Cursor.DEFAULT);
            if (countdownTimeline != null) countdownTimeline.stop();
        }
    }

    private void handleImageLoading(String imagePath) {
        imgContainer.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5 5 0 0;");

        try {
            if (imagePath != null) {
                Image image = imageCache.get(imagePath);

                if (image == null) {
                    if (imagePath.startsWith("http")) {
                        image = new Image(imagePath, 300, 200, true, true, true);
                    } else {
                        String internalPath = getClass().getResource(imagePath).toExternalForm();
                        image = new Image(internalPath, 300, 200, true, true, true);
                    }
                    imageCache.put(imagePath, image);
                }

                final Image finalImg = image;
                if (finalImg.isBackgroundLoading() && finalImg.getProgress() < 1.0) {
                    finalImg.progressProperty().addListener((obs, old, progress) -> {
                        if (progress.doubleValue() >= 1.0) {
                            Platform.runLater(() -> setPaneBackground(finalImg));
                        }
                    });
                } else {
                    setPaneBackground(finalImg);
                }
            }
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    private void setupZoomEffect() {
        // Clip trên wrapper để giới hạn ảnh không tràn ra ngoài
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imgWrapper.widthProperty());
        clip.heightProperty().bind(imgWrapper.heightProperty());
        clip.setStyle("-fx-border-radius: 5 5 0 0;");
        imgWrapper.setClip(clip);

        // Scale bên trong imgContainer (wrapper có clip không bị scale)
        scaleIn = new ScaleTransition(javafx.util.Duration.millis(200), imgContainer);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);
        scaleIn.setInterpolator(Interpolator.EASE_BOTH);

        scaleOut = new ScaleTransition(javafx.util.Duration.millis(200), imgContainer);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.setInterpolator(Interpolator.EASE_BOTH);
    }

    private void setupZoomListeners() {
        // Tìm VBox card từ imgWrapper
        Parent parent = imgWrapper;
        while (parent != null && !(parent instanceof VBox)) {
            parent = parent.getParent();
        }
        VBox card = (VBox) parent;

        if (card != null) {
            card.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                if (scaleIn != null) {
                    scaleOut.stop();
                    scaleIn.playFromStart();
                }
            });
            card.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                if (scaleOut != null) {
                    scaleIn.stop();
                    scaleOut.playFromStart();
                }
            });
        }
    }

    private void setPaneBackground(Image image) {
        if (image == null) return;
        String url = image.getUrl().replace(" ", "%20");
        imgContainer.setStyle("-fx-background-image: url('" + url + "'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-transition-origin: center;");
        // Gắn listener sau khi ảnh đã load xong
        setupZoomListeners();
    }

    private void registerRealTimeListener() {
        realTimeListener = message -> {
            if (auction == null) return;

            try {
                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                String action = data.has("action") ? data.get("action").getAsString() : null;
                if (action == null || !data.has("payload")) return;

                JsonObject payload = JsonParser.parseString(data.get("payload").getAsString()).getAsJsonObject();
                String auctionId = payload.has("auctionId") ? payload.get("auctionId").getAsString() : null;
                if (auctionId == null || !auctionId.equals(auction.getId())) return;

                Platform.runLater(() -> {
                    if (payload.has("newHighestBid")) {
                        double newHighestBid = payload.get("newHighestBid").getAsDouble();
                        auction.setCurrentHighestBid(newHighestBid);
                        lblPrice.setText(String.format("%,.0f VNĐ", newHighestBid));
                    }

                    if (payload.has("highestBidder")) {
                        auction.setHighestBidder(payload.get("highestBidder").getAsString());
                    }

                    if (payload.has("newEndTime")) {
                        try {
                            auction.setEndTime(LocalDateTime.parse(payload.get("newEndTime").getAsString()));
                        } catch (Exception ignored) {
                        }
                    }

                    if ("AUCTION_FINISHED".equals(action)) {
                        auction.setStatus("FINISHED");
                        refreshUI();
                        detachRealTimeListener();
                    } else if ("BID_UPDATE".equals(action)) {
                        refreshUI();
                    }
                });
            } catch (Exception ignored) {
            }
        };

        NetworkClient.getInstance().onMessageReceived(realTimeListener);
    }

    private void detachRealTimeListener() {
        if (realTimeListener != null) {
            NetworkClient.getInstance().getMessageListeners().remove(realTimeListener);
            realTimeListener = null;
        }
    }

    private String determineStatus(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (isAuctionFinished(auction)) return "Đã kết thúc";
        if (auction.getStartTime() == null || auction.getStartTime().isAfter(now)) return "Sắp diễn ra";
        if (auction.getEndTime() != null && auction.getEndTime().isAfter(now)) return "Đang diễn ra";
        return "Đã kết thúc";
    }

    private String calculateTimeLeft(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = auction.getStartTime();
        LocalDateTime end = auction.getEndTime();

        if (isAuctionFinished(auction)) return "Đã kết thúc";
        if (start == null || end == null) return "N/A";
        if (now.isAfter(end)) return "Đã kết thúc";

        boolean isUpcoming = now.isBefore(start);
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

    private boolean isAuctionFinished(Auction auction) {
        if (auction == null) return false;

        String status = auction.getStatus();
        if (status != null) {
            String normalizedStatus = status.trim().toUpperCase();
            if (!"OPEN".equals(normalizedStatus)) {
                return true;
            }
        }

        Double priceCeiling = auction.getPriceCeiling();
        return priceCeiling != null
                && priceCeiling > 0
                && auction.getCurrentHighestBid() >= priceCeiling;
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            detachRealTimeListener();
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
            detachRealTimeListener();
            Stage stage = (Stage) imgContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/auction-detail-view.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setAuction(this.auction);

            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển sang màn hình chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }
}