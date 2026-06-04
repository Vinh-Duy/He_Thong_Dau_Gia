package com.bidnova.controllers.seller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ManageProductController {

    @FXML private TableView<Auction> productTable;
    @FXML private TableColumn<Auction, Integer> idCol;
    @FXML private TableColumn<Auction, String> nameCol;
    @FXML private TableColumn<Auction, String> descriptionCol;
    @FXML private TableColumn<Auction, String> categoryCol;
    @FXML private TableColumn<Auction, Double> startPriceCol;
    @FXML private TableColumn<Auction, Double> currentPriceCol;
    @FXML private TableColumn<Auction, String> startTimeCol;
    @FXML private TableColumn<Auction, String> endTimeCol;
    @FXML private TableColumn<Auction, String> statusCol;
    @FXML private TableColumn<Auction, Integer> sellerIdCol;
    private ObservableList<Auction> auctionList = FXCollections.observableArrayList();
    private java.util.function.Consumer<String> broadcastListener;
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @FXML
    public void initialize() {
        productTable.setItems(auctionList);

        /* Tham số truyền vào phải giống trong Auction.java */
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        startPriceCol.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        currentPriceCol.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        sellerIdCol.setCellValueFactory(new PropertyValueFactory<>("sellerId"));

        // Cấu hình kích thước cột
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        loadMyProducts(); // Lấy data từ server khi vừa mở màn hình

        // Lắng nghe real-time updates từ server
        // Lưu listener để có thể remove khi rời màn hình
        broadcastListener = message -> {
            try {
                // Nếu user không còn là SELLER thì bỏ qua
                String currentRole = SessionManager.getRole();
                if (!"SELLER".equalsIgnoreCase(currentRole) && !"ADMIN".equalsIgnoreCase(currentRole)) return;

                JsonObject data = JsonParser.parseString(message).getAsJsonObject();
                String action = data.has("action") ? data.get("action").getAsString() : null;
                if ("AUCTION_LIST_UPDATE".equals(action)) {
                    Platform.runLater(this::loadMyProducts);
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý broadcast message: " + e.getMessage());
            }
        };
        NetworkClient.getInstance().onMessageReceived(broadcastListener);
    }

    private void loadMyProducts() {
        new Thread(() -> {
            try {
                if (!SessionManager.isLoggedIn()) {
                    Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng đăng nhập để quản lý sản phẩm.")
                    );
                    return;
                }
                int sellerId = SessionManager.getUserId();

                JsonObject payload = new JsonObject();
                payload.addProperty("sellerId", sellerId);

                Request request = new Request("GET_MY_AUCTIONS", payload.toString(), SessionManager.getToken());
                Response response = NetworkClient.getInstance().sendRequest(request);

                Platform.runLater(() -> {
                    if (response == null || !"SUCCESS".equals(response.getStatus())) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi tải sản phẩm", response.getMessage());
                        return;
                    }

                    // Kiểm tra an toàn trước khi Parse mảng
                    String rawData = response.getData() != null ? response.getData().toString() : "[]";
                    JsonElement element = JsonParser.parseString(rawData);
                    
                    if (element.isJsonArray()) {
                        List<Auction> list = gson.fromJson(element, new TypeToken<List<Auction>>() {}.getType());
                        auctionList.clear();
                        if (list != null) auctionList.addAll(list);
                    } else {
                        System.err.println("Dữ liệu không phải mảng: " + rawData);
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý khi tải danh sách sản phẩm.")
                );
            }
        }).start();
    }

    @FXML
    private void handleDelete() {
        Auction selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Bác chọn 1 sản phẩm trong bảng để xóa nhé!");
            return;
        }

        // Cảnh báo xác nhận trước khi xóa
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bác có chắc muốn xóa [" + selected.getProductName() + "] không?");
        Optional<ButtonType> result = confirm.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                // Gửi ID sản phẩm cần xóa lên Server
                Request req = new Request("DELETE_PRODUCT", String.valueOf(selected.getId()));
                Response res = NetworkClient.getInstance().sendRequest(req);
                
                Platform.runLater(() -> {
                    if (res != null && "SUCCESS".equals(res.getStatus())) {
                        auctionList.remove(selected); // Xóa khỏi bảng
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa sản phẩm!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xóa được: " + (res != null ? res.getMessage() : "Mất kết nối"));
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        Auction selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Bác chọn 1 sản phẩm để sửa nhé!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/seller/add-product-view.fxml"));
            Parent root = loader.load();

            // Truyền dữ liệu sang AddProductController
            AddProductController controller = loader.getController();
            controller.setAuctionToEdit(selected); // <--- Gọi hàm đổ dữ liệu

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        // Mở file AddProductView bình thường, không truyền data gì cả
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/seller/add-product-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goBackHome(Event event) {
        try {
            // 0. Gỡ listener để tránh gọi GET_MY_AUCTIONS khi đã chuyển sang tài khoản khác
            if (broadcastListener != null) {
                NetworkClient.getInstance().getMessageListeners().remove(broadcastListener);
                broadcastListener = null;
            }

            // 1. Đăng xuất
            SessionManager.logout();
            
            // 2. Chuyển về màn hình đăng nhập
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/signin-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng nhập");
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}