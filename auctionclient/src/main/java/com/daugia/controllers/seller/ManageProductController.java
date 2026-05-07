package com.daugia.controllers.seller;

import java.util.List;
import java.util.Optional;

import com.daugia.models.Auction;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ManageProductController {

    @FXML private TableView<Auction> productTable;
    private ObservableList<Auction> auctionList = FXCollections.observableArrayList();
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        productTable.setItems(auctionList);
        loadMyProducts(); // Lấy data từ server khi vừa mở màn hình
    }

    private void loadMyProducts() {
        new Thread(() -> {
            try {
                String username = SessionManager.getUsername();
                if (username == null || username.isEmpty()) {
                    Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng đăng nhập để quản lý sản phẩm.")
                    );
                    return;
                }
                int sellerId = SessionManager.getUserId();

                com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                payload.addProperty("sellerId", sellerId);

                Request req = new Request("GET_MY_AUCTIONS", payload.toString());
                Response res = NetworkClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res == null) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không nhận được phản hồi từ server.");
                        return;
                    }

                    if (!"SUCCESS".equals(res.getStatus())) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi tải sản phẩm", res.getMessage());
                        return;
                    }

                    Object raw = res.getPayload();
                    String json = (raw instanceof String) ? (String) raw : gson.toJson(raw);

                    List<Auction> list = gson.fromJson(
                        json,
                        new TypeToken<List<Auction>>() {}.getType()
                    );

                    auctionList.clear();
                    if (list != null) {
                        auctionList.addAll(list);
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
    private void handleEdit(javafx.event.ActionEvent event) {
        Auction selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Bác chọn 1 sản phẩm để sửa nhé!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/seller/AddProductView.fxml"));
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
    private void handleAdd(javafx.event.ActionEvent event) {
        // Mở file AddProductView bình thường, không truyền data gì cả
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/seller/AddProductView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goBackHome(MouseEvent event) {
        try {
            // 1. Đăng xuất
            SessionManager.logout();
            
            // 2. Chuyển về màn hình đăng nhập
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/LoginPopup.fxml"));
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