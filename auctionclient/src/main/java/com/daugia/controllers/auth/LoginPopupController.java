package com.daugia.controllers.auth;

import java.io.IOException;

import com.daugia.controllers.bidder.DanhSachSanPhamController;
import com.daugia.controllers.components.HeaderController;
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

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bác chưa nhập đủ tài khoản hoặc mật khẩu kìa!");
            return;
        }

        System.out.println("Đang gửi yêu cầu đăng nhập lên Server...");

        new Thread(() -> {
            try {
                String payload = "{\"username\":\"" + user + "\", \"password\":\"" + pass + "\"}";
                Request req = new Request("LOGIN", payload);
                Response res = NetworkClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && "SUCCESS".equals(res.getStatus())) {
                        try {
                            // 1. Chuyển JSON thành đối tượng User
                            User loggedInUser = gson.fromJson(res.getData().toString(), User.class);
                            
                            // 2. Lưu Session
                            SessionManager.setSession(loggedInUser.getUsername(), loggedInUser.getToken()); 

                            // 3. Cập nhật Header
                            if (HeaderController.getInstance() != null) {
                                HeaderController.getInstance().updateHeaderUI();
                            }

                            // 4. Phân quyền rẽ nhánh giao diện
                            String viewPath = "/views/common/HomeView.fxml";
                            String welcomeMsg = "Đăng nhập thành công!";

                            if ("ADMIN".equals(loggedInUser.getRole())) {
                                viewPath = "/views/admin/AdminUserView.fxml";
                                welcomeMsg = "Chào mừng sếp Admin đã quay lại!";
                            }

                            showAlert(Alert.AlertType.INFORMATION, "Thành công", welcomeMsg);

                            // Chuyển trang
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
                            Parent root = loader.load();
                            loginButton.getScene().setRoot(root);

                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi khi xử lý dữ liệu người dùng!");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tài khoản hoặc mật khẩu!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không kết nối được tới Server!"));
            }
        }).start();
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