package com.daugia.controllers.auth;

import java.io.IOException;

import com.daugia.controllers.bidder.DanhSachSanPhamController;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SignUpController {

    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    @FXML private ComboBox<Integer> dayBox;
    @FXML private ComboBox<Integer> monthBox;
    @FXML private ComboBox<Integer> yearBox;
    @FXML private ComboBox<String> genderBox;

    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleNumber;
    @FXML private Label ruleSpecial;
    @FXML private RadioButton personalRadio;
    @FXML private RadioButton orgRadio;
    @FXML private TextField passwordTextField;

    @FXML private TextField usernameField;
    // @FXML private PasswordField passwordField;

    @FXML private Label usernameError;
    @FXML private Label passwordError;

    @FXML private BorderPane mainBorderPane;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtEmail;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPhone;

    @FXML private ToggleGroup roleGroup; // Nó tự nhận từ FXML nhờ fx:id="roleGroup"
    @FXML private RadioButton rbBidder;
    @FXML private RadioButton rbSeller;

    @FXML
    private void handleSignup(ActionEvent event) {
        usernameError.setText("");
        passwordError.setText("");

        String username = usernameField.getText();
        String password = txtPassword.getText();

        boolean hasError = false;

        if (username.length() < 3) {
            usernameError.setText("Ít nhất 3 ký tự");
            hasError = true;
        }

        if (password.length() < 6) {
            passwordError.setText("Mật khẩu ≥ 6 ký tự");
            hasError = true;
        }

        if (hasError) return;

        System.out.println("Đang gửi yêu cầu đăng ký lên Server...");
        
        String payload = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

        Request req = new Request("REGISTER", payload);

        Response res = NetworkClient.getInstance().sendRequest(req);

        if (res != null && "SUCCESS".equals(res.getStatus())) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText(res.getMessage());
            alert.showAndWait();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml")); 
                Parent loginView = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(loginView, 1280, 650));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi đăng ký");
            alert.setHeaderText(null);
            alert.setContentText(res != null ? res.getMessage() : "Lỗi kết nối đến máy chủ!");
            alert.showAndWait();
        }
    }

    @FXML
    private void goToSignin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginView, 1280, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePassword() {
        if (txtPassword.isVisible()) {

            passwordTextField.setText(txtPassword.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {

            txtPassword.setText(passwordTextField.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }
    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        if (personalRadio != null) personalRadio.setToggleGroup(group);
        if (orgRadio != null) orgRadio.setToggleGroup(group);

        if (txtPassword != null) {
            txtPassword.textProperty().addListener((obs, oldVal, newVal) -> validatePassword(newVal));
        }

        if (genderBox != null) {
            genderBox.getItems().addAll("Nam", "Nữ");
        }

        if (dayBox != null) {
            for (int i = 1; i <= 31; i++) dayBox.getItems().add(i);
        }
        if (monthBox != null) {
            for (int i = 1; i <= 12; i++) monthBox.getItems().add(i);
        }
        if (yearBox != null) {
            for (int i = 1950; i <= 2025; i++) yearBox.getItems().add(i);
        }
    }
    @FXML

    private void validatePassword(String pass) {

        updateRule(ruleLength, pass.length() >= 8);

        updateRule(ruleUpper, pass.matches(".*[A-Z].*"));

        updateRule(ruleLower, pass.matches(".*[a-z].*"));

        updateRule(ruleNumber, pass.matches(".*\\d.*"));

        updateRule(ruleSpecial, pass.matches(".*[!@#$%^&*()].*"));
    }

    private void updateRule(Label label, boolean ok) {
        if (ok) {
            label.setText("✔ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setText("✖ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: red;");
        }
    }



    private void loadCategoryView(String categoryName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/DanhSachSanPhamView.fxml"));
            Parent content = loader.load();

            DanhSachSanPhamController controller = loader.getController();

            controller.setCategory(categoryName);

            mainBorderPane.setCenter(content);
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Không thể tải trang danh mục: " + categoryName);
            alert.showAndWait();
        }
    }

    // === HÀM XỬ LÝ NÚT BẤM ĐĂNG KÝ (Logic Server) ===
    @FXML
    public void xuLyDangKy() { 
        String username = txtUsername != null ? txtUsername.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText().trim() : "";
        String confirmPassword = txtConfirmPassword != null ? txtConfirmPassword.getText() : "";
        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        String firstName = txtFirstName != null ? txtFirstName.getText().trim() : "";
        String middleName = txtMiddleName != null ? txtMiddleName.getText().trim() : "";
        String lastName = txtLastName != null ? txtLastName.getText().trim() : "";
        String fullName = (lastName + " " + middleName + " " + firstName).replaceAll("\\s+", " ").trim();
        String phone = txtPhone != null ? txtPhone.getText().trim() : "";

        // 1. Kiểm tra Validate cơ bản
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ Tài khoản và Mật khẩu!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Lỗi", "Mật khẩu nhập lại không khớp!");
            return;
        }

        String role = "BIDDER"; // Mặc định
        if (rbSeller.isSelected()) {
            role = "SELLER";
        }
        
        // Test thử xem nó lấy đúng chưa
        System.out.println("Role đã chọn: " + role);

        // 2. Đóng gói dữ liệu vào JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("email", email);
        payload.addProperty("fullName", fullName.trim());
        payload.addProperty("phone", phone);

        // (Tuỳ chọn) Gửi thêm giới tính/ngày sinh nếu bác muốn thêm cột vào DB sau này
        if (genderBox != null && genderBox.getValue() != null) {
            payload.addProperty("gender", genderBox.getValue());
        }

        
        // 3. Gửi lên Server
        new Thread(() -> {
            Request req = new Request("REGISTER", payload.toString());
            Response res = NetworkClient.getInstance().sendRequest(req); 

            Platform.runLater(() -> {
                if (res != null && "SUCCESS".equals(res.getStatus())) {
                    showAlert("Thành công", "Chào mừng bác gia nhập! Đăng ký thành công.");
                    // TODO: Gọi hàm chuyển về trang Đăng Nhập tại đây
                } else {
                    showAlert("Thất bại", res != null ? res.getMessage() : "Lỗi kết nối Server.");
                }
            });
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}