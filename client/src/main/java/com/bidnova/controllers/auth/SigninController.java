package com.bidnova.controllers.auth;

import com.bidnova.utils.SocketClient;
import com.bidnova.utils.UserSession;
import com.bidnova.network.Request;
import com.bidnova.network.Response;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SigninController {
    @FXML
    private HBox header;

    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) username.getScene().getWindow();
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
        String user = username.getText();
        String pass = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        // 2. Gửi yêu cầu LOGIN đến server
        Request request = new Request("LOGIN", user, pass);
        Response response = SocketClient.sendRequest(request);
        
        // 3. Xử lý response
        if (response != null && response.getStatus().equals("SUCCESS")) {
            alert("Thành công", response.getMessage());
            System.out.println("Role: " + response.getData());
            // update header để hiển thị tên người dùng và nút đăng xuất
            UserSession.getInstance().signin(user);

            // navigate đến trang chủ
            goToHome();
        } else {
            alert("Lỗi", response != null ? response.getMessage() : "Lỗi kết nối server");
        }
    }
}
