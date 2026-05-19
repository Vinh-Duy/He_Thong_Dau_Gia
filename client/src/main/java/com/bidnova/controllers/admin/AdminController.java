package com.bidnova.controllers.admin;

import java.util.List;
import java.util.Optional;

import com.bidnova.models.User;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AdminController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        // Cấu hình các cột của bảng
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Cấu hình kích thước cột
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Tải dữ liệu người dùng từ Server
        loadUsers();
    }

    private void Alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /** Làm mới danh sách. */
    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    /** Lấy dữ liệu từ Server. */
    private void loadUsers() {
        new Thread(() -> {
            try {
                // Gửi yêu cầu lấy tất cả user
                Request request = new Request("GET_ALL_USERS", "");
                Response response = NetworkClient.getInstance().sendRequest(request);

                if ("SUCCESS".equals(response.getStatus())) {
                    // Ép kiểu JSON data thành List<User>
                    List<User> list = gson.fromJson(response.getData().toString(), 
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

    /** Xóa người dùng đã chọn. */
    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert("Thông báo", "Vui lòng chọn một người dùng để xóa!");
            return;
        }

        // Xác nhận trước khi xóa
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa người dùng " + selectedUser.getUsername() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Xóa người dùng
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("userId", selectedUser.getId());
                    
                    Request request = new Request("DELETE_USER", payload.toString());
                    Response response = NetworkClient.getInstance().sendRequest(request);
                    if ("SUCCESS".equals(response.getStatus())) {
                        Platform.runLater(() -> {
                            userList.remove(selectedUser);
                            userTable.getItems().remove(selectedUser);
                            Alert("Thông báo", response.getMessage());
                        });
                    } else {
                        Platform.runLater(() -> Alert("Lỗi", response.getMessage()));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> Alert("Lỗi", "Lỗi khi xóa người dùng!"));
                }
            }).start();
        }
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) userTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    /** Đăng xuất. */
    @FXML
    private void handleLogout() {
        try {
            // 1. Đăng xuất
            SessionManager.logout();

            // 2. Chuyển về trang chủ
            goTo("/views/auth/signin-view.fxml");
        }
        catch (Exception e) {
            Alert("Lỗi", "Lỗi khi đăng xuất!");
            e.printStackTrace();
        }
    }
}