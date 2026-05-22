package com.bidnova.controllers.auth;

import com.bidnova.models.User;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SigninController {
    @FXML private HBox header;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        // Logic chuyển focus khi nhấn Enter
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleSignin());
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignup() {
        goTo("/views/auth/signup-view.fxml");
    }

    @FXML
    private void goToHome() {
        goTo("/views/common/home-view.fxml");
    }

    @FXML
    private void togglePassword() {
        if (passwordField.isVisible()) {

            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {

            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }

    private void alert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSignin() {
        // 1. Lấy dữ liệu từ giao diện
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null && passwordField.isVisible() ? passwordField.getText().trim() : passwordTextField.getText().trim();

        // 2. Kiểm tra sơ bộ (Validation)
        if (username.isEmpty() || password.isEmpty()) {
            alert("Lỗi", "Tên người dùng và mật khẩu không được để trống");
            return;
        }

        // 3. Đóng gói dữ liệu vào JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);

        // 4. Gửi lên Server
        new Thread(() -> {
            Request request = new Request("LOGIN", payload.toString());
            Response response = NetworkClient.getInstance().sendRequest(request);

            Platform.runLater(() -> {
                if ("SUCCESS".equals(response.getStatus())) {
                    alert("Thành công", response.getMessage());
                    System.out.println(response.getData());
                    
                    // Lấy thông tin user và lưu vào két sắt
                    Object rawData = response.getData();
                    String jsonString = (rawData instanceof String) ? (String) rawData : gson.toJson(rawData);
                    User user = gson.fromJson(jsonString, User.class);

                    SessionManager.login(user);

                    // chuyển trang tương ứng dựa vào role
                    String role = user.getRole();
                    String fxmlPath = "/views/common/home-view.fxml";
                    if ("SELLER".equals(role)) fxmlPath = "/views/seller/manage-product-view.fxml";
                    else if ("ADMIN".equals(role)) fxmlPath = "/views/admin/admin-view.fxml";

                    goTo(fxmlPath);
                } else {
                    alert("Lỗi", response.getMessage());
                }
            });
        }).start();
    }
}
