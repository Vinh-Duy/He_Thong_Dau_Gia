package com.bidnova.controllers.auth;

import java.io.IOException;

import com.bidnova.controllers.bidder.DanhSachSanPhamController;
import com.bidnova.controllers.components.HeaderController;
import com.bidnova.models.User;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.SessionManager;
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

        new Thread(() -> {
            try {
                String requestPayload = "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
                Request req = new Request("LOGIN", requestPayload);
                Response res = NetworkClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && "SUCCESS".equals(res.getStatus())) {
                        try {
                            // CHỖ NÀY QUAN TRỌNG: Sửa lỗi BEGIN_OBJECT
                            Object rawData = res.getData(); 
                            String jsonString = (rawData instanceof String) ? (String) rawData : gson.toJson(rawData);

                            // Chuyển thành User
                            User loggedInUser = gson.fromJson(jsonString, User.class);

                            if (loggedInUser != null) {
                                // 1. Lưu vào két sắt
                                SessionManager.login(loggedInUser.getId(), loggedInUser.getUsername(), loggedInUser.getToken());

                                // 2. ÉP HEADER CẬP NHẬT NGAY LẬP TỨC (Dành cho Bidder/Admin/Seller)
                                if (HeaderController.getInstance() != null) {
                                    HeaderController.getInstance().updateHeaderUI();
                                }

                                // 3. Phân quyền chuyển trang
                                String viewPath = "/views/common/home-view.fxml";
                                if ("ADMIN".equals(loggedInUser.getRole())) {
                                    viewPath = "/views/admin/AdminUserView.fxml";
                                } else if ("SELLER".equals(loggedInUser.getRole())) {
                                    viewPath = "/views/seller/ManageProductView.fxml";
                                }

                                // Chuyển cảnh
                                FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
                                Parent root = loader.load();
                                loginButton.getScene().setRoot(root);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý JSON, xem Console bác ơi!");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tài khoản hoặc mật khẩu!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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