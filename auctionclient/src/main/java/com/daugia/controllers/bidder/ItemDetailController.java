package com.daugia.controllers.bidder;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.daugia.models.Auction;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.utils.SessionManager;
import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

public class ItemDetailController implements Initializable {
    
    @FXML private Button btnBack;
    @FXML private ImageView imgItem;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblSeller;
    @FXML private Label lblCategory;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtBidInput;
    @FXML private Button btnPlaceBid;
    @FXML private Label lblBidError;
    
    // Auto-bid components
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;
    @FXML private Button btnToggleAutoBid;
    @FXML private VBox autoBidPanel;
    
    private Auction currentAuction;
    private Timer countdownTimer;
    private boolean autoBidEnabled = false;
    private long currentPriceValue = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ItemDetailController đã được khởi tạo!");
        
        // Setup real-time listener
        NetworkClient.getInstance().setOnMessageReceived(this::handleRealTimeUpdate);
        
        // Initially hide auto-bid panel
        autoBidPanel.setVisible(false);
        
        // Setup input validation
        txtBidInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
            lblBidError.setVisible(false);
        });
        
        // Setup button actions
        btnToggleAutoBid.setOnAction(e -> toggleAutoBid());
        btnPlaceBid.setOnAction(e -> placeBid());
    }
    
    public void setAuction(Auction auction) {
        this.currentAuction = auction;
        if (auction != null) {
            this.currentPriceValue = (long) auction.getCurrentHighestBid();
            updateUI();
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
                    Gson gson = new Gson();
                    Auction auction = gson.fromJson(response.getData().toString(), Auction.class);
                    
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
            lblSeller.setText("Người bán: " + getSellerName(currentAuction.getSellerId()));
            lblCategory.setText("Danh mục: " + currentAuction.getCategory());
            txtDescription.setText(currentAuction.getDescription());
            
            // Update current auction object
            currentAuction.setCurrentHighestBid(currentPriceValue);
        }
    }
    
    private void handleRealTimeUpdate(String message) {
        Platform.runLater(() -> {
            try {
                com.google.gson.JsonObject data = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
                
                if (data.has("action") && "BID_UPDATE".equals(data.get("action").getAsString())) {
                    String payloadString = data.get("payload").getAsString();
                    com.google.gson.JsonObject payload = com.google.gson.JsonParser.parseString(payloadString).getAsJsonObject();
                    
                    String updatedAuctionId = payload.get("auctionId").getAsString();
                    double newPrice = payload.get("newHighestBid").getAsDouble();
                    
                    if (currentAuction != null && updatedAuctionId.equals(currentAuction.getId())) {
                        currentPriceValue = (long) newPrice;
                        currentAuction.setCurrentHighestBid(newPrice);
                        lblCurrentBid.setText(formatVietnameseCurrency(currentPriceValue));
                        
                        // Flash effect for price update
                        lblCurrentBid.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> lblCurrentBid.setStyle("-fx-text-fill: black;"));
                            }
                        }, 1500);
                        
                        // Check auto-bid logic
                        if (autoBidEnabled) {
                            checkAutoBid(newPrice);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling real-time update: " + e.getMessage());
            }
        });
    }
    
    private void placeBid() {
        String username = SessionManager.getUsername();
        if (username == null || username.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng đăng nhập để tham gia đấu giá!");
            return;
        }
        
        String bidText = txtBidInput.getText().trim();
        if (bidText.isEmpty()) {
            lblBidError.setText("Vui lòng nhập giá đấu!");
            lblBidError.setVisible(true);
            return;
        }
        
        try {
            long bidAmount = Long.parseLong(bidText);
            
            if (bidAmount <= currentPriceValue) {
                lblBidError.setText("Giá đấu phải cao hơn giá hiện tại!");
                lblBidError.setVisible(true);
                return;
            }
            
            // Send bid request
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            payload.addProperty("amount", bidAmount);
            payload.addProperty("username", username);
            
            new Thread(() -> {
                Request request = new Request("PLACE_BID", payload.toString());
                Response response = NetworkClient.getInstance().sendRequest(request);
                
                Platform.runLater(() -> {
                    if (response != null) {
                        if ("SUCCESS".equals(response.getStatus())) {
                            showAlert("Thành công", "Đặt giá thành công!");
                            txtBidInput.clear();
                            lblBidError.setVisible(false);
                        } else {
                            lblBidError.setText(response.getMessage());
                            lblBidError.setVisible(true);
                        }
                    } else {
                        showAlert("Lỗi", "Không nhận được phản hồi từ server!");
                    }
                });
            }).start();
            
        } catch (NumberFormatException e) {
            lblBidError.setText("Vui lòng nhập số hợp lệ!");
            lblBidError.setVisible(true);
        }
    }
    
    private void toggleAutoBid() {
        autoBidEnabled = !autoBidEnabled;
        autoBidPanel.setVisible(autoBidEnabled);
        btnToggleAutoBid.setText(autoBidEnabled ? "Tắt Auto-Bid" : "Bật Auto-Bid");
        
        if (autoBidEnabled) {
            validateAutoBidSettings();
        }
    }
    
    private void validateAutoBidSettings() {
        String maxBidText = txtMaxBid.getText().trim();
        String incrementText = txtIncrement.getText().trim();
        
        if (maxBidText.isEmpty() || incrementText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập giá tối đa và bước giá!");
            toggleAutoBid(); // Turn off if invalid
            return;
        }
        
        try {
            long maxBid = Long.parseLong(maxBidText);
            long increment = Long.parseLong(incrementText);
            
            if (maxBid <= currentPriceValue) {
                showAlert("Lỗi", "Giá tối đa phải cao hơn giá hiện tại!");
                toggleAutoBid();
                return;
            }
            
            if (increment <= 0) {
                showAlert("Lỗi", "Bước giá phải lớn hơn 0!");
                toggleAutoBid();
                return;
            }
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số hợp lệ!");
            toggleAutoBid();
        }
    }
    
    private void checkAutoBid(double currentPrice) {
        try {
            long maxBid = Long.parseLong(txtMaxBid.getText().trim());
            long increment = Long.parseLong(txtIncrement.getText().trim());
            
            long nextBid = (long) currentPrice + increment;
            
            if (nextBid <= maxBid) {
                // Place auto-bid
                com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                payload.addProperty("auctionId", currentAuction.getId());
                payload.addProperty("amount", nextBid);
                payload.addProperty("username", SessionManager.getUsername());
                
                new Thread(() -> {
                    Request request = new Request("PLACE_BID", payload.toString());
                    NetworkClient.getInstance().sendRequest(request);
                }).start();
            } else {
                // Disable auto-bid when max is reached
                Platform.runLater(() -> {
                    autoBidEnabled = false;
                    autoBidPanel.setVisible(false);
                    btnToggleAutoBid.setText("Bật Auto-Bid");
                    showAlert("Thông báo", "Đã đạt giá tối đa, Auto-Bid tự động tắt!");
                });
            }
        } catch (Exception e) {
            System.err.println("Error in auto-bid logic: " + e.getMessage());
        }
    }
    
    private void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        
        countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Simple countdown logic - you can enhance this based on endTime
                    if (currentAuction != null && currentAuction.getEndTime() != null) {
                        lblTimeRemaining.setText("Thời gian còn lại: " + calculateTimeRemaining());
                    }
                });
            }
        }, 0, 1000);
    }
    
    private String calculateTimeRemaining() {
        // Simple implementation - you can enhance this
        return "Đang cập nhật...";
    }
    
    private String getSellerName(int sellerId) {
        // You can implement this to fetch seller name from database
        return "Seller " + sellerId;
    }
    
    private String formatVietnameseCurrency(long amount) {
        java.util.Locale localeVN = java.util.Locale.forLanguageTag("vi-VN");
        java.text.NumberFormat vnCurrencyFormat = java.text.NumberFormat.getCurrencyInstance(localeVN);
        return vnCurrencyFormat.format(amount);
    }
    
    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/common/HomeView.fxml"));
            javafx.scene.Parent homeRoot = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
            stage.getScene().setRoot(homeRoot);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể quay lại trang chủ!");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
