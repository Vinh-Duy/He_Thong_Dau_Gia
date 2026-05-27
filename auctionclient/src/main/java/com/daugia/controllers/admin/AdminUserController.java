package com.daugia.controllers.admin;

import java.util.List;

import com.daugia.models.User;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminUserController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        // 1. Ánh xạ các cột với thuộc tính trong model User
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // 2. Load dữ liệu ngay khi mở trang
        loadUsersFromServer();
    }

    @FXML
    private void handleRefresh() {
        loadUsersFromServer();
    }

    private void loadUsersFromServer() {
        new Thread(() -> {
            try {
                // Gửi yêu cầu lấy tất cả user
                Request req = new Request("GET_ALL_USERS", "");
                Response res = NetworkClient.getInstance().sendRequest(req);

                if (res != null && "SUCCESS".equals(res.getStatus())) {
                    // Ép kiểu JSON data thành List<User>
                    List<User> list = gson.fromJson(res.getData().toString(), 
                                      new TypeToken<List<User>>(){}.getType());
                    
                    Platform.runLater(() -> {
                        userList.setAll(list);
                        userTable.setItems(userList);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn một người dùng để xóa!");
            return;
        }
        
        // Xác nhận trước khi xóa
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa người dùng '" + selectedUser.getUsername() + "'?");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            new Thread(() -> {
                try {
                    com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                    payload.addProperty("userId", selectedUser.getId());
                    
                    Request req = new Request("DELETE_USER", payload.toString());
                    Response res = NetworkClient.getInstance().sendRequest(req);
                    
                    Platform.runLater(() -> {
                        if (res != null && "SUCCESS".equals(res.getStatus())) {
                            userList.remove(selectedUser);
                            showAlert("Thành công", "Đã xóa người dùng '" + selectedUser.getUsername() + "'!");
                        } else {
                            showAlert("Lỗi", "Không thể xóa: " + (res != null ? res.getMessage() : "Mất kết nối"));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Lỗi", "Lỗi khi xóa người dùng!"));
                }
            }).start();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            // 1. Đăng xuất
            com.daugia.utils.SessionManager.logout();
            
            // 2. Chuyển về màn hình đăng nhập
            java.net.URL location = getClass().getResource("/views/auth/LoginPopup.fxml");
            if (location == null) {
                System.err.println("=> [LỖI] Không tìm thấy file LoginPopup.fxml");
                showAlert("Lỗi", "Không tìm thấy màn hình đăng nhập!");
                return;
            }
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(location);
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.Scene currentScene = userTable.getScene();
            currentScene.setRoot(root);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.setTitle("Đăng nhập");
            stage.sizeToScene();
            stage.centerOnScreen();
            
            System.out.println("=> [ADMIN] Đăng xuất thành công!");
        } catch (Exception e) {
            System.err.println("=> [EXCEPTION] Lỗi khi đăng xuất:");
            e.printStackTrace();
        }
    }
}