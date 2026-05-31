package com.bidnova.controllers.auth;

import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SignupController {
    @FXML private RadioButton rbBidderButton;
    @FXML private RadioButton rbSellerButton;

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Label ruleLength, ruleUpper, ruleLower, ruleNumber, ruleSpecial;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> genderBox;

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

        // Logic chuyển focus khi nhấn Enter
        fullNameField.setOnAction(e -> usernameField.requestFocus());
        usernameField.setOnAction(e -> {
            if (passwordField.isVisible()) passwordField.requestFocus();
            else passwordTextField.requestFocus();
        });
        passwordField.setOnAction(e -> confirmPasswordField.requestFocus());
        passwordTextField.setOnAction(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> phoneField.requestFocus());

        // Khi nhấn Enter ở phoneField, nhảy sang genderBox và tự động mở danh sách
        phoneField.setOnAction(e -> {
            genderBox.requestFocus();
            genderBox.show();
        });

        // Khi đang focus ở genderBox, nhấn Enter sẽ mở danh sách nếu nó đang đóng
        genderBox.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !genderBox.isShowing()) {
                if (genderBox.getValue() == null) {
                    genderBox.show();
                }
                else {
                    handleSignup();
                }
            }
        });

        genderBox.getItems().addAll("Nam", "Nữ", "Chọn giới tính");
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
    private void goToSignin() {
        goTo("/views/auth/signin-view.fxml");
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
    private void handleSignup() {
        // 1. Lấy dữ liệu từ giao diện
        String role = rbBidderButton != null && rbBidderButton.isSelected() ? "BIDDER" : "SELLER";
        String fullName = fullNameField != null ? fullNameField.getText().trim() : "";
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null && passwordField.isVisible() ? passwordField.getText().trim() : passwordTextField.getText().trim();
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        String gender = genderBox.getValue();

        // 2. Kiểm tra sơ bộ (Validation)
        if (username.isEmpty() || password.isEmpty()) {
            alert("Lỗi", "Không được để trống các trường bắt buộc!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            alert("Lỗi", "Mật khẩu nhập lại không khớp");
            return;
        }

        // 3. Đóng gói dữ liệu vào JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("email", email);
        payload.addProperty("phone", phone);
        payload.addProperty("fullName", fullName);
        payload.addProperty("role", role);
        payload.addProperty("gender", gender);

        // 4. Gửi lên Server
        new Thread(() -> {
            Request request = new Request("REGISTER", payload.toString());
            Response response = NetworkClient.getInstance().sendRequest(request);

            Platform.runLater(() -> {
                if ("SUCCESS".equals(response.getStatus())) {
                    alert("Thành công", response.getMessage());
                    goToSignin();
                } else {
                    alert("Thất bại", response.getMessage());
                }
            });
        }).start();
    }
}