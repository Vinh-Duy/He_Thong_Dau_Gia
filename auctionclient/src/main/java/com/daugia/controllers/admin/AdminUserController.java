package com.daugia.controllers.admin;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

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
}