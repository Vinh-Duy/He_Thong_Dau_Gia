package com.daugia.controllers.auth;

import java.io.IOException;

import com.daugia.controllers.bidder.DanhSachSanPhamController;
import com.daugia.models.User;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.utils.SessionManager;
import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginPopupController {
    @FXML private BorderPane mainBorderPane;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    private Gson gson = new Gson();

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (!validateLoginInputs(user, pass)) {
            return;
        }

        performLoginRequest(user, pass);
    }

    private boolean validateLoginInputs(String user, String pass) {
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn chưa nhập đủ tài khoản hoặc mật khẩu kìa!");
            return false;
        }
        return true;
    }

    private void performLoginRequest(String user, String pass) {
        new Thread(() -> {
            try {
                String requestPayload = "{\"username\":\"" + user + "\", \"password\":\"" + pass + "\"}";
                Request req = new Request("LOGIN", requestPayload);
                Response res = NetworkClient.getInstance().sendRequest(req);

                Platform.runLater(() -> handleLoginResponse(res));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleLoginResponse(Response res) {
        if (res != null && "SUCCESS".equals(res.getStatus())) {
            try {
                Object rawData = res.getData();
                String jsonString = (rawData instanceof String) ? (String) rawData : gson.toJson(rawData);
                User loggedInUser = gson.fromJson(jsonString, User.class);

                if (loggedInUser != null) {
                    handleLoginSuccess(loggedInUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý JSON, xem Console bác ơi!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    private void handleLoginSuccess(User loggedInUser) {
        // 1. Lưu vào két sắt
        SessionManager.setSession(loggedInUser.getId(), loggedInUser.getUsername(), loggedInUser.getToken());

        // 2. Phân quyền chuyển trang
        String viewPath = determineViewPath(loggedInUser);

        // Chuyển cảnh
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            loginButton.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải trang chính!");
        }
    }

    private String determineViewPath(User loggedInUser) {
        String viewPath = "/views/common/HomeView.fxml";
        if ("ADMIN".equals(loggedInUser.getRole())) {
            viewPath = "/views/admin/AdminUserView.fxml";
        } else if ("SELLER".equals(loggedInUser.getRole())) {
            viewPath = "/views/seller/AddProductView.fxml";
        }
        return viewPath;
    }

    @FXML
    private void goToSignup(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/SignupView.fxml"));
            Parent signupView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signupView, 1280, 650));
            
        } catch (Exception e) {
            System.err.println("Lỗi chuyển trang Đăng ký:");
            e.printStackTrace();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}