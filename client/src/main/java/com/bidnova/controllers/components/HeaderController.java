package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.bidnova.controllers.bidder.CategoryController;
import com.bidnova.utils.SessionManager;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeaderController {
    @FXML private HBox header;
    @FXML private BorderPane category;
    @FXML private MenuButton menuButton;
    @FXML private Label trangChuLabel;
    @FXML private Label taiSanLabel;
    @FXML private Label phienDauGiaLabel;
    @FXML private Label gioiThieuLabel;
    @FXML private Label lienHeLabel;
    
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    @FXML private HBox authBox;
    @FXML private HBox userBox;
    @FXML private Label userLabel;

    private UserPopupController userPopupController;
    private Popup popup;

    private static HeaderController instance;

    public static HeaderController getInstance() {
        return instance;
    }

    public void updateHeaderUI() {
        boolean loggedIn = SessionManager.isLoggedIn();

        userBox.setVisible(loggedIn);
        userBox.setManaged(loggedIn);

        authBox.setVisible(!loggedIn);

        if (loggedIn) {
            userLabel.setText("Xin chào " + SessionManager.getUsername());
        }
    }

    @FXML
    public void initialize() {
        startClock();
        header.toFront();

        if (SessionManager.isLoggedIn()) {
            showUserBox(SessionManager.getUsername());
        } else {
            showAuthBox();
        }

        Node icon = menuButton.getGraphic();
        // Tạo hiệu ứng xoay (RotateTransition) kéo dài 300 mili-giây
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), icon);
        // Lắng nghe sự kiện MenuButton mở/đóng
        menuButton.showingProperty().addListener((observable, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                // Khi menu xổ ra -> Xoay icon 180 độ (hướng xuống)
                rotateTransition.setToAngle(180);
            } else {
                // Khi menu đóng lại -> Xoay icon về 0 độ (vị trí ban đầu)
                rotateTransition.setToAngle(0);
            }
            rotateTransition.play(); // Chạy hiệu ứng
        });
    }

    private void startClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.S");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");

        Timeline clock = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(timeFormatter));
            dateLabel.setText(now.format(dateFormatter));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) header.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void goTo(String fxmlPath, Consumer<FXMLLoader> onLoadComplete) {
        try {
            Stage stage = (Stage) header.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
            onLoadComplete.accept(loader);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void showTrangChu(MouseEvent event) {
        goTo("/views/common/home-view.fxml");
    }

    @FXML
    private void showDanhMucBatDongSan(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.setCategory("Bất động sản");
        });
    }

    @FXML
    private void showDanhMucTaiSanNhaNuoc(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.setCategory("Tài sản nhà nước");
        });
    }

    @FXML
    private void showDanhMucPhuongTienXeCo(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.setCategory("Phương tiện - Xe cộ");
        });
    }

    @FXML
    private void showDanhMucSuuTamNgheThuat(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.setCategory("Sưu tầm - nghệ thuật");
        });
    }

    @FXML
    private void showDanhMucTaiSanKhac(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.setCategory("Tài sản khác");
        });
    }

    @FXML
    private void showGioiThieu() {
        goTo("/views/common/gioi-thieu-view.fxml");
    }

    @FXML
    private void showLienHe() {
        goTo("/views/common/lien-he-view.fxml");
    }

    @FXML
    private void goToSignup() {
        goTo("/views/auth/signup-view.fxml");
    }

    @FXML
    private void goToSignin() {
        goTo("/views/auth/signin-view.fxml");
    }

    @FXML
    private void goToHome() {
        goTo("/views/common/home-view.fxml");
    }

    @FXML
    private void toggleUserPopup() {
        if (popup == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/user-popup.fxml"));
                VBox popupContent = loader.load();
                userPopupController = loader.getController();
                
                popup = new Popup();
                popup.getContent().add(popupContent);
                popup.setAutoHide(true); // Tự động đóng khi nhấn ra ngoài
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        if (popup.isShowing()) {
            popup.hide();
        } else {
            // Lấy tọa độ tuyệt đối của header trên màn hình
            // X = Lề phải của header - chiều rộng popup (300) - khoảng cách (30)
            double x = header.localToScreen(header.getWidth(), 0).getX() - 330;
            // Y = Lề dưới của header + khoảng cách (10)
            double y = header.localToScreen(0, header.getHeight()).getY() + 10;
            
            userPopupController.updateData();
            
            popup.show(header.getScene().getWindow(), x, y);
            
            // Lấy node nội dung để áp dụng hiệu ứng
            Node content = popup.getContent().get(0);
            content.setOpacity(0); // Khởi tạo độ trong suốt bằng 0
            // Tạo và chạy hiệu ứng Fade In

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SessionManager.logout();
        if (popup != null) popup.hide();
        showAuthBox();
        goToHome();
    }

    public void showUserBox(String userName) {
        authBox.setVisible(false);
        authBox.setManaged(false);
        userBox.setVisible(true);
        userBox.setManaged(true);
        userLabel.setText("Xin chào, " + userName);
    }

    public void showAuthBox() {
        authBox.setVisible(true);
        authBox.setManaged(true);
        userBox.setVisible(false);
        userBox.setManaged(false);
    }
}