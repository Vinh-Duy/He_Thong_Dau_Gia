package com.bidnova.controllers.bidder;

import java.net.URL;
import java.util.ResourceBundle;

import java.time.LocalDateTime;
import java.text.NumberFormat;

import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.controllers.components.BidHistoryController;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class AuctionDetailController implements Initializable {
    @FXML private Button btnBack;
    @FXML private ImageView imgItem;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentBid;
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
    
    private Auction currentAuction;
    private boolean autoBidEnabled = false;
    private double currentPriceValue = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ItemDetailController đã được khởi tạo!");
        
        // Setup real-time listener
        NetworkClient.getInstance().addOnMessageReceivedListener(this::handleRealTimeUpdate);
        
        // Setup input validation
        txtBidInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
            lblBidError.setVisible(false);
        });
        
        // Setup button actions
        btnToggleAutoBid.setOnAction(e -> toggleAutoBid());
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
            txtDescription.setText(currentAuction.getDescription() != null ? currentAuction.getDescription() : "Không có mô tả.");
            
            loadImage(currentAuction.getImageUrl());
        }
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
        Platform.runLater(() -> {
            try {
                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                
                // Server có thể gửi BID_UPDATE qua field 'action' hoặc 'status' tùy logic Broadcast
                String action = data.has("action") ? data.get("action").getAsString() : "";
                
                if ("BID_UPDATE".equals(action)) {
                    JsonObject payload;
                    // Kiểm tra xem payload là String JSON hay là Object trực tiếp
                    if (data.get("payload").isJsonPrimitive()) {
                        payload = JsonParser.parseString(data.get("payload").getAsString()).getAsJsonObject();
                    } else {
                        payload = data.get("payload").getAsJsonObject();
                    }
                    
                    String updatedAuctionId = payload.get("auctionId").getAsString();
                    double newPrice = payload.get("newHighestBid").getAsDouble();
                    
                    if (currentAuction != null && updatedAuctionId.equals(currentAuction.getId())) {
                        currentPriceValue = newPrice;
                        lblCurrentBid.setText(formatVietnameseCurrency(currentPriceValue));
                        
                        // Cập nhật lại bảng lịch sử real-time
                        if (bidHistoryTableController != null) {
                            bidHistoryTableController.loadHistory(currentAuction.getId());
                        }

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

    @FXML
    private void goTo(String fxmlPath) {
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
        if (autoBidEnabled) {
            // Gửi yêu cầu hủy Auto-Bid lên server
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            
            new Thread(() -> {
                Request request = new Request("DEACTIVATE_AUTO_BID", payload.toString());
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
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("auctionId", currentAuction.getId());
            payload.addProperty("maxBid", maxBid);
            payload.addProperty("increment", increment);
            
            new Thread(() -> {
                Request request = new Request("SET_AUTO_BID", payload.toString());
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
                    }
                });
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số hợp lệ!");
        }
    }

    private void disableAutoBid(String reason) {
        autoBidEnabled = false;
        btnToggleAutoBid.setText("🔥 Bật Auto-Bid");
        btnToggleAutoBid.setStyle(""); // Trả về style mặc định trong CSS
        if (lblAutoBidStatus != null) {
            lblAutoBidStatus.setText(reason);
        }
    }

    private void checkAutoBid(double currentPrice) {
        try {
            double maxBid = Double.parseDouble(txtMaxBid.getText().trim());
            double increment = Double.parseDouble(txtIncrement.getText().trim());
            
            double nextBid = currentPrice + increment;
            
            if (nextBid <= maxBid) {
                // Việc đặt giá tự động thực tế nên để SERVER xử lý để đảm bảo công bằng.
                // Client chỉ cập nhật UI hoặc thông báo trạng thái.
                System.out.println("Auto-bid logic: Đang chờ server xử lý bước giá tiếp theo...");
            } else {
                Platform.runLater(() -> {
                    disableAutoBid("Đã dừng - Chạm mức tối đa");
                    showAlert("Thông báo", "Đã đạt giá tối đa, Auto-Bid tự động tắt!");
                });
            }
        } catch (Exception e) {
            disableAutoBid("Lỗi cấu hình");
        }
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
}
