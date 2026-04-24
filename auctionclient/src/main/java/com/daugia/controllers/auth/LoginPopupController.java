package com.daugia.controllers.auth;

import java.io.IOException;

import com.daugia.controllers.bidder.DanhSachSanPhamController;
import com.daugia.controllers.components.HeaderController;

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

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // 1. Kiểm tra không cho nhập rỗng
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Bác chưa nhập đủ tài khoản hoặc mật khẩu kìa!");
            alert.showAndWait();
            return;
        }

        System.out.println("Đang gửi yêu cầu đăng nhập lên Server...");

        try {
            // 2. GÓI DỮ LIỆU VÀ GỬI LÊN SERVER (MỞ KHÓA ĐOẠN NÀY)
            String payload = "{\"username\":\"" + user + "\", \"password\":\"" + pass + "\"}";
            
            // Lưu ý: phải import mấy class Request, Response, NetworkClient nhé
            com.daugia.network.Request req = new com.daugia.network.Request("LOGIN", payload);
            com.daugia.network.Response res = com.daugia.network.NetworkClient.getInstance().sendRequest(req);

            // 3. XỬ LÝ KHI SERVER TRẢ LỜI
            // 3. XỬ LÝ KHI SERVER TRẢ LỜI
            if (res != null && "SUCCESS".equals(res.getStatus())) {
                
                // 1. Lưu cả username và token vào Session
                String token = res.getData().toString();
                com.daugia.utils.SessionManager.setSession(user, token); 

                // 2. GỌI HEADER CẬP NHẬT GIAO DIỆN
                if (HeaderController.getInstance() != null) {
                    HeaderController.getInstance().updateHeaderUI();
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setContentText("Đăng nhập thành công!");
                alert.showAndWait();

                // 🔥 2. ĐÓNG POPUP ĐĂNG NHẬP (Chỗ này bác kiểm tra xem tên nút của bác là gì nhé)
                javafx.stage.Stage stage = (javafx.stage.Stage) loginButton.getScene().getWindow();
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/common/HomeView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    loginButton.getScene().setRoot(root);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // 🔥 3. (Tùy chọn) Bác có thể gọi hàm cập nhật lại cái Header (ẩn nút Đăng nhập, hiện tên User) ở đây
                
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Sai tài khoản hoặc mật khẩu!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi kết nối");
            alert.setContentText("Không kết nối được tới Server. Bác đã bật Server chưa?");
            alert.showAndWait();
        }
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
}