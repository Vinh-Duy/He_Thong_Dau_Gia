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

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colFullName;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;

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
                            new TypeToken<List<User>>() {
                            }.getType());

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

        System.out.println("Đang xóa user: " + selectedUser.getUsername());
        // Chỗ này bác làm tương tự gửi Request "DELETE_USER" lên server nhé
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
            // 1. Xác định chính xác chuỗi đường dẫn (Bác kiểm tra lại hoa/thường của
            // LoginPopup)
            String fxmlPath = "/com/daugia/views/auth/LoginPopup.fxml";

            System.out.println("=> [DEBUG] Đang tìm file tại: " + fxmlPath);

            // 2. Lấy URL của file
            java.net.URL location = getClass().getResource(fxmlPath);

            // 3. Nếu vẫn NULL, thử tìm theo đường dẫn ngắn (nếu bác để trong
            // resources/views)
            if (location == null) {
                System.out.println("=> [THỬ LẠI] Không thấy ở đường dẫn cũ, thử tìm tại /views/auth/LoginPopup.fxml");
                location = getClass().getResource("/views/auth/LoginPopup.fxml");
            }

            if (location == null) {
                // Cú chốt: Báo lỗi cụ thể để bác biết đường dẫn nào đang bị sai
                System.err.println("=> [LỖI CỰC NẶNG] Vẫn không thấy file LoginPopup.fxml ở bất cứ đâu!");
                return;
            }

            // 4. Load khi đã có location chuẩn
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(location);
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene currentScene = userTable.getScene();

            // 3. THAY ĐỔI GỐC (ROOT) - Đây là chìa khóa để giữ nguyên cửa sổ
            currentScene.setRoot(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.setTitle("Đăng nhập");

            // Nếu màn hình Login nhỏ hơn màn hình Admin, dùng dòng này để cửa sổ co lại cho
            // đẹp:
            stage.sizeToScene();
            stage.centerOnScreen();

            System.out.println("=> [ADMIN] Chuyển cảnh thành công!");

        } catch (Exception e) {
            System.err.println("=> [EXCEPTION] Lỗi khi load FXML:");
            e.printStackTrace();
        }
    }
}