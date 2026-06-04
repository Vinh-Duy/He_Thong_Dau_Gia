package com.bidnova.controllers.bidder;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import com.bidnova.controllers.components.BidChartController;
import com.bidnova.controllers.components.BidHistoryController;
import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuctionDetailController implements Initializable {
    @FXML private Button btnBack;
    @FXML private ImageView imgItem;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblTimeLeft;
    @FXML private Label lblMinBidIncrement;
    @FXML private Label lblPriceCeiling;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtBidInput;
    @FXML private Button btnPlaceBid;
    @FXML private Label lblBidError;
    
    // Auto-bid components
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;
    @FXML private Button btnToggleAutoBid;
    @FXML private Label lblAutoBidStatus;
    
    // Inject controller của file bid-history-table.fxml (fx:id + "Controller")
    @FXML private BidHistoryController bidHistoryTableController;
    @FXML private BidChartController bidChartController;
    
    private Auction currentAuction;
    private boolean autoBidEnabled = false;
    private double currentPriceValue = 0;
    private Timeline countdownTimeline;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("AuctionDetailController đã được khởi tạo!");
        
        // Setup input validation
        txtBidInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
            lblBidError.setVisible(false);
        });
        
        // Setup button actions
        btnToggleAutoBid.setOnAction(e -> toggleAutoBid());

        // Đăng ký lắng nghe thông điệp real-time từ server
        NetworkClient.getInstance().onMessageReceived(this::handleRealTimeUpdate);
    }
    
    public void setAuction(Auction auction) {
        this.currentAuction = auction;
        if (auction != null) {
            this.currentPriceValue = auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice();
            updateUI();
            
            // Cập nhật lịch sử đặt giá nếu controller con đã sẵn sàng
            if (bidHistoryTableController != null) {
                bidHistoryTableController.loadHistory(auction.getId());
            }
            
            // Cập nhật biểu đồ giá đấu khi controller chart đã sẵn sàng
            if (bidChartController != null) {
                bidChartController.loadChartData(auction.getId());
            }
            
            // Kiểm tra trạng thái Auto-Bid trên server (nếu user đã bật trước đó)
            checkAutoBidState();
            
            // Bắt đầu đếm ngược thời gian đấu giá
            startCountdown();
        }
    }
    
    public void setAuctionId(String id) {
        // For backward compatibility
        System.out.println("ItemDetailController nhận auction ID: " + id);
        // This method can be used to load auction from server
        loadAuctionFromServer(id);
    }
    
    private void loadAuctionFromServer(String auctionId) {
        new Thread(() -> {
            try {
                Request request = new Request("GET_AUCTION_BY_ID", auctionId);
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .create();
                    Auction auction = gson.fromJson((String) response.getData(), Auction.class);
                    
                    Platform.runLater(() -> {
                        setAuction(auction);
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Lỗi", "Không thể tải thông tin sản phẩm!");
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading auction: " + e.getMessage());
            }
        }).start();
    }
    
    private void updateUI() {
        if (currentAuction != null) {
            lblItemName.setText(currentAuction.getProductName());
            lblCurrentBid.setText(formatVietnameseCurrency(currentPriceValue));
            txtDescription.setText(currentAuction.getDescription() != null ? currentAuction.getDescription() : "Không có mô tả.");
            
            // Display min bid increment
            double minIncrement = currentAuction.getMinBidIncrement();
            lblMinBidIncrement.setText(formatVietnameseCurrency(minIncrement));
            
            // Display price ceiling (if set)
            if (currentAuction.getPriceCeiling() != null && currentAuction.getPriceCeiling() > 0) {
                lblPriceCeiling.setText(formatVietnameseCurrency(currentAuction.getPriceCeiling()));
            } else {
                lblPriceCeiling.setText("Vô giới hạn");
            }
            
            // NEW: Disable bidding if auction is already finished
            if ("FINISHED".equalsIgnoreCase(currentAuction.getStatus()) || "CLOSED".equalsIgnoreCase(currentAuction.getStatus())) {
                disableAllBidding();
                lblTimeLeft.setText("Đã kết thúc");
                lblTimeLeft.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }
            }
            
            loadImage(currentAuction.getImageUrl());
        }
    }

    private void disableAllBidding() {
        btnPlaceBid.setDisable(true);
        txtBidInput.setDisable(true);
        txtMaxBid.setDisable(true);
        txtIncrement.setDisable(true);
        btnToggleAutoBid.setDisable(true);
    }

    private void loadImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image;
                if (imageUrl.startsWith("http")) {
                    image = new Image(imageUrl, true);
                } else {
                    image = new Image(getClass().getResource(imageUrl).toExternalForm(), true);
                }
                
                image.progressProperty().addListener((obs, old, progress) -> {
                    if (progress.doubleValue() >= 1.0) {
                        Platform.runLater(() -> imgItem.setImage(image));
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + e.getMessage());
            imgItem.setImage(new Image(getClass().getResource("/images/abstract-background.jpg").toExternalForm()));
        }
    }

    private void handleRealTimeUpdate(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String action = json.has("action") ? json.get("action").getAsString() : null;
            
            if ("BID_UPDATE".equals(action)) {
                // Server gửi payload là một chuỗi JSON string bên trong
                JsonObject payload = JsonParser.parseString(json.get("payload").getAsString()).getAsJsonObject();
                String auctionId = payload.get("auctionId").getAsString();
                
                // Chỉ cập nhật nếu đúng là sản phẩm đang xem
                if (currentAuction != null && auctionId.equals(currentAuction.getId())) {
                    double newBid = payload.get("newHighestBid").getAsDouble();
                    
                    Platform.runLater(() -> {
                        currentPriceValue = newBid;
                        lblCurrentBid.setText(formatVietnameseCurrency(newBid));
                        
                        if (payload.has("newEndTime")) {
                            try {
                                String newEndTimeStr = payload.get("newEndTime").getAsString();
                                LocalDateTime newEndTime = LocalDateTime.parse(newEndTimeStr);
                                currentAuction.setEndTime(newEndTime);
                                // Cập nhật lại đồng hồ đếm ngược với thời gian mới
                                startCountdown();

                                // Hiển thị thông báo gia hạn nếu có flag isExtended
                                if (payload.has("isExtended") && payload.get("isExtended").getAsBoolean()) {
                                    showAlert("Gia hạn đấu giá", 
                                        "Có lượt đặt giá mới trong 5 phút cuối!\nPhiên đấu giá được gia hạn thêm 5 phút.");
                                }
                            } catch (Exception ex) {
                                System.err.println("Lỗi parse newEndTime: " + ex.getMessage());
                            }
                        }
                        // Cập nhật lại bảng lịch sử đấu giá
                        if (bidHistoryTableController != null) {
                            bidHistoryTableController.loadHistory(currentAuction.getId());
                        }
                        
                        // Cập nhật biểu đồ
                        if (bidChartController != null) {
                            bidChartController.loadChartData(currentAuction.getId());
                        }
                    });
                }
            } else if ("AUCTION_FINISHED".equals(action)) {
                // NEW: Handle AUCTION_FINISHED event (ceiling reached)
                JsonObject payload = JsonParser.parseString(json.get("payload").getAsString()).getAsJsonObject();
                String auctionId = payload.get("auctionId").getAsString();
                
                if (currentAuction != null && auctionId.equals(currentAuction.getId())) {
                    Platform.runLater(() -> {
                        // Update the final bid
                        if (payload.has("newHighestBid")) {
                            double finalBid = payload.get("newHighestBid").getAsDouble();
                            currentPriceValue = finalBid;
                            lblCurrentBid.setText(formatVietnameseCurrency(finalBid));
                        }
                        
                        // Disable all bidding
                        disableAllBidding();
                        
                        // Stop countdown
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                        
                        // Show notification
                        String highestBidder = payload.has("highestBidder") ? payload.get("highestBidder").getAsString() : "Không có người thắng";
                        String finalPrice = payload.has("newHighestBid") ? formatVietnameseCurrency(payload.get("newHighestBid").getAsDouble()) : "0 ₫";
                        showAlert("Phiên kết thúc", 
                            "Phiên đấu giá đã kết thúc. Người thắng: " + highestBidder + "\nGiá cuối cùng: " + finalPrice);
                        
                        // Update UI
                        currentAuction.setStatus("FINISHED");
                        lblTimeLeft.setText("Đã kết thúc");
                        lblTimeLeft.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        
                        // Reload history
                        if (bidHistoryTableController != null) {
                            bidHistoryTableController.loadHistory(currentAuction.getId());
                        }
                        
                        // Reload chart
                        if (bidChartController != null) {
                            bidChartController.loadChartData(currentAuction.getId());
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Bỏ qua nếu tin nhắn không đúng định dạng JSON
        }
    }

    @FXML
    private void goTo(String fxmlPath) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handlePlaceBid() {
        String username = SessionManager.getUsername();
        if (username == null || username.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng đăng nhập để tham gia đấu giá!");
            goTo("/views/auth/signin-view.fxml");
            return;
        }
        
        String bidText = txtBidInput.getText().trim();
        if (bidText.isEmpty()) {
            lblBidError.setText("Vui lòng nhập giá đấu!");
            lblBidError.setVisible(true);
            return;
        }
        
        try {
            double bidAmount = Double.parseDouble(bidText);
            
            if (bidAmount <= currentPriceValue) {
                lblBidError.setText("Giá đấu phải cao hơn giá hiện tại!");
                lblBidError.setVisible(true);
                return;
            }

            // NEW: Check minimum bid increment
            double bidIncrement = bidAmount - currentPriceValue;
            double minBidIncrement = currentAuction.getMinBidIncrement();
            
            if (bidIncrement < minBidIncrement) {
                double minRequiredBid = currentPriceValue + minBidIncrement;
                lblBidError.setText(
                    String.format("Bước giá tối thiểu: %.0f. Giá tối thiểu: %.0f",
                        minBidIncrement, minRequiredBid)
                );
                lblBidError.setVisible(true);
                return;
            }

            // NEW: Warn if approaching price ceiling
            if (currentAuction.getPriceCeiling() != null) {
                if (bidAmount >= currentAuction.getPriceCeiling()) {
                    showAlert("Thông báo", "Giá của bạn đạt giới hạn trần - đấu giá sẽ kết thúc!");
                } else if (bidAmount > currentAuction.getPriceCeiling() * 0.9) {
                    showAlert("Cảnh báo", "Giá của bạn gần giới hạn trần!");
                }
            }

            // Send bid request
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            payload.addProperty("amount", bidAmount);
            payload.addProperty("username", username);
            
            new Thread(() -> {
                Request request = new Request("PLACE_BID", payload.toString(), SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                Platform.runLater(() -> {
                    // Issue 5 Fix: Add comprehensive error logging and handling
                    if (response != null) {
                        if ("SUCCESS".equals(response.getStatus())) {
                            showAlert("Thành công", "Đặt giá thành công!");
                            txtBidInput.clear();
                            lblBidError.setVisible(false);
                            
                            // Cập nhật giá hiển thị tạm thời với giá vừa đặt (Optimistic UI)
                            // Giá chuẩn cuối cùng sẽ được cập nhật qua handleRealTimeUpdate (Socket)
                            // currentPriceValue = bidAmount;
                            // lblCurrentBid.setText(formatVietnameseCurrency(bidAmount));

                            // Không thêm điểm chart ở đây, để handleRealTimeUpdate xử lý
                            // tránh bị trùng 2 điểm
                        } else {
                            String errorMsg = response.getMessage();
                            // Log full error for debugging
                            System.err.println("PlaceBid Error - Status: " + response.getStatus() + ", Message: " + errorMsg);
                            lblBidError.setText(errorMsg != null ? errorMsg : "Đặt giá thất bại. Vui lòng thử lại.");
                            lblBidError.setVisible(true);
                        }
                    } else {
                        System.err.println("PlaceBid: No response from server");
                        showAlert("Lỗi", "Không nhận được phản hồi từ server! Vui lòng kiểm tra kết nối.");
                    }
                });
            }).start();
            
        } catch (NumberFormatException e) {
            lblBidError.setText("Vui lòng nhập số hợp lệ!");
            lblBidError.setVisible(true);
        }
    }
    
    private void toggleAutoBid() {
        if (autoBidEnabled) {
            // Gửi yêu cầu hủy Auto-Bid lên server
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            
            new Thread(() -> {
                Request request = new Request("DEACTIVATE_AUTO_BID", payload.toString(), SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                Platform.runLater(() -> {
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        disableAutoBid("Đã tắt thủ công");
                    } else {
                        showAlert("Lỗi", "Không thể tắt Auto-Bid trên máy chủ!");
                    }
                });
            }).start();
            return;
        }

        String maxBidText = txtMaxBid.getText().trim();
        String incrementText = txtIncrement.getText().trim();
        
        if (maxBidText.isEmpty() || incrementText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập giá tối đa và bước giá!");
            return;
        }
        
        try {
            double maxBid = Double.parseDouble(maxBidText);
            double increment = Double.parseDouble(incrementText);
            
            if (maxBid <= currentPriceValue) {
                showAlert("Lỗi", "Giá tối đa phải cao hơn giá hiện tại!");
                return;
            }
            
            if (increment <= 0) {
                showAlert("Lỗi", "Bước giá phải lớn hơn 0!");
                return;
            }
            
            // Send SET_AUTO_BID request to server
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            payload.addProperty("maxBid", maxBid);
            payload.addProperty("increment", increment);
            
            new Thread(() -> {
                Request request = new Request("SET_AUTO_BID", payload.toString(), SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                Platform.runLater(() -> {
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        autoBidEnabled = true;
                        btnToggleAutoBid.setText("Tắt Auto-Bid");
                        btnToggleAutoBid.setStyle("-fx-background-color: #6c757d;");
                        showAlert("Thành công", "Đấu giá tự động đã được kích hoạt!");
                    } else {
                        String errorMsg = (response != null && response.getMessage() != null) 
                                          ? response.getMessage() : "Lỗi không xác định";
                        showAlert("Lỗi", "Không thể kích hoạt Auto-Bid: " + errorMsg);
                        System.out.println(errorMsg);
                    }
                });
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số hợp lệ!");
        }
    }

    private void disableAutoBid(String reason) {
        autoBidEnabled = false;
        btnToggleAutoBid.setText("Bật Auto-Bid");
        btnToggleAutoBid.setStyle(""); // Trả về style mặc định trong CSS
        if (lblAutoBidStatus != null) {
            lblAutoBidStatus.setText(reason);
        }
    }
    
    private void checkAutoBidState() {
        if (currentAuction == null) return;
        
        String username = SessionManager.getUsername();
        if (username == null || username.isEmpty()) return;
        
        new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("auctionId", currentAuction.getId());
                
                Request request = new Request("GET_AUTO_BID", payload.toString(), SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                Platform.runLater(() -> {
                    if (response != null && "SUCCESS".equals(response.getStatus()) && response.getData() != null) {
                        try {
                            JsonObject autoBidJson = JsonParser.parseString((String) response.getData()).getAsJsonObject();
                            
                            if (autoBidJson.has("isActive") && autoBidJson.get("isActive").getAsBoolean()) {
                                double maxBid = autoBidJson.get("maxBid").getAsDouble();
                                double increment = autoBidJson.get("increment").getAsDouble();
                                
                                // Đã có auto-bid đang hoạt động trên server
                                autoBidEnabled = true;
                                btnToggleAutoBid.setText("Tắt Auto-Bid");
                                btnToggleAutoBid.setStyle("-fx-background-color: #6c757d;");
                                txtMaxBid.setText(String.valueOf(maxBid));
                                txtIncrement.setText(String.valueOf(increment));
                                if (lblAutoBidStatus != null) {
                                    lblAutoBidStatus.setText("Auto-Bid đang bật");
                                }
                            }
                        } catch (Exception e) {
                            // Không parse được -> không có auto-bid, giữ trạng thái mặc định
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error checking auto-bid state: " + e.getMessage());
            }
        }).start();
    }
    
    private String formatVietnameseCurrency(double amount) {
        NumberFormat vnCurrencyFormat = NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        return vnCurrencyFormat.format(amount);
    }
    
    @FXML
    private void handleBack() {
        goTo("/views/common/home-view.fxml");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        if (currentAuction == null || currentAuction.getEndTime() == null) {
            if (lblTimeLeft != null) {
                lblTimeLeft.setText("Không xác định");
            }
            return;
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateCountdownLabel();
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();

        // Cập nhật ngay lập tức lần đầu
        updateCountdownLabel();
    }

    private void updateCountdownLabel() {
        if (currentAuction == null || currentAuction.getEndTime() == null) {
            if (lblTimeLeft != null) {
                lblTimeLeft.setText("Không xác định");
            }
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = currentAuction.getEndTime();

        if (now.isAfter(endTime)) {
            if (lblTimeLeft != null) {
                lblTimeLeft.setText("Phiên đấu giá đã kết thúc");
                lblTimeLeft.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
            
            // Khóa các điều khiển đấu giá
            txtBidInput.setDisable(true);
            btnPlaceBid.setDisable(true);
            txtMaxBid.setDisable(true);
            txtIncrement.setDisable(true);
            btnToggleAutoBid.setDisable(true);
            
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            return;
        }

        // Kiểm tra xem auction status có phải là OPEN không, nếu không phải thì cũng khóa
        if (!"OPEN".equals(currentAuction.getStatus())) {
            if (lblTimeLeft != null) {
                lblTimeLeft.setText("Đã đóng hoặc tạm dừng");
                lblTimeLeft.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            }
            txtBidInput.setDisable(true);
            btnPlaceBid.setDisable(true);
            txtMaxBid.setDisable(true);
            txtIncrement.setDisable(true);
            btnToggleAutoBid.setDisable(true);
            
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            return;
        }

        // Bật lại các điều khiển nếu phiên đấu giá đang mở
        txtBidInput.setDisable(false);
        btnPlaceBid.setDisable(false);
        txtMaxBid.setDisable(false);
        txtIncrement.setDisable(false);
        btnToggleAutoBid.setDisable(false);

        java.time.Duration duration = java.time.Duration.between(now, endTime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ngày ");
        }
        if (days > 0 || hours > 0) {
            sb.append(hours).append(" giờ ");
        }
        if (days > 0 || hours > 0 || minutes > 0) {
            sb.append(minutes).append(" phút ");
        }
        sb.append(seconds).append(" giây");

        if (lblTimeLeft != null) {
            lblTimeLeft.setText(sb.toString().trim());
            
            // Highlight màu đỏ nếu thời gian còn dưới 1 phút, màu cam dưới 1 giờ, màu xanh thông thường
            if (days == 0 && hours == 0 && minutes < 1) {
                lblTimeLeft.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if (days == 0 && hours < 1) {
                lblTimeLeft.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            } else {
                lblTimeLeft.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }
    }
}
