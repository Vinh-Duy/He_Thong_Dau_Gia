package com.bidnova.controllers.auth;

import com.bidnova.utils.SocketClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
// import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
// import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class SignupController {
    @FXML
    private RadioButton rbBidderButton;

    @FXML
    private RadioButton rbSellerButton;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField username;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label ruleLength, ruleUpper, ruleLower, ruleNumber, ruleSpecial;

    @FXML
    private ComboBox<String> genderBox;

    private void updateRule(Label label, boolean ok) {
        if (ok) {
            label.setText("✔ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setText("✖ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRule(ruleLength, newVal.length() >= 8);
            updateRule(ruleUpper, newVal.matches(".*[A-Z].*"));
            updateRule(ruleLower, newVal.matches(".*[a-z].*"));
            updateRule(ruleNumber, newVal.matches(".*\\d.*"));
            updateRule(ruleSpecial, newVal.matches(".*[!@#$%^&*()].*"));
        });

        genderBox.getItems().addAll("Nam", "Nữ", "Chọn giới tính");
    }

    @FXML
    private void goTo(Event event, String fxmlPath) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignin(Event event) {
        goTo(event, "/views/auth/signin-view.fxml");
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

    // định nghĩa hàm alert để hiển thị thông báo cho người dùng
    private void alert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSignup(Event event) {
        // 1. Lấy dữ liệu từ giao diện
        String user = username.getText();
        String pass = passwordField.getText();
        String email = emailField.getText();
        String fullName = firstNameField.getText() + " " + middleNameField.getText() + " " + lastNameField.getText();
        String phone = phoneField.getText();
        String gender = genderBox.getValue();
        String role = (rbBidderButton != null && rbBidderButton.isSelected()) ? "BIDDER" : "SELLER";

        // 2. Kiểm tra sơ bộ (Validation)
        if (user.isEmpty() || pass.isEmpty()) {
            alert("Lỗi", "Không được để trống các trường bắt buộc!");
            return;
        }

        // 3. Gửi yêu cầu REGISTER tới Server
        Request request = new Request("REGISTER", user, pass, email, fullName, phone, gender, role);
        Response response = SocketClient.sendRequest(request);
        
        if (response != null && response.getStatus().equals("SUCCESS")) {
            alert("Thành công", response.getMessage());
            goToSignin(event);
        } else {
            alert("Lỗi", response != null ? response.getMessage() : "Đăng ký thất bại!");
        }
    }
}
