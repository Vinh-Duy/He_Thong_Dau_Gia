package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.bidnova.utils.SessionManager;

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

    private static HeaderController instance;

    public static HeaderController getInstance() {
        return instance;
    }

    public void updateHeaderUI() {
        boolean loggedIn = SessionManager.isLoggedIn();

        userBox.setVisible(loggedIn);
        userBox.setManaged(loggedIn);

        authBox.setVisible(!loggedIn);
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
        // goTo("/views/bidder/category-view.fxml", loader -> {
        //     CategoryController categoryController = loader.getController();
        //     categoryController.resetCategoryData("BẤT ĐỘNG SẢN", ProductLoader.loadProducts());
        // });
    }

    @FXML
    private void showDanhMucTaiSanNhaNuoc(ActionEvent event) {
        // goTo("/views/bidder/category-view.fxml", loader -> {
        //     CategoryController categoryController = loader.getController();
        //     categoryController.resetCategoryData("TÀI SẢN NHÀ NƯỚC", ProductLoader.loadProducts());
        // });
    }

    @FXML
    private void showDanhMucPhuongTienXeCo(ActionEvent event) {
        // goTo("/views/bidder/category-view.fxml", loader -> {
        //     CategoryController categoryController = loader.getController();
        //     categoryController.resetCategoryData("PHƯƠNG TIỆN - XE CỘ", ProductLoader.loadProducts());
        // });
    }

    @FXML
    private void showDanhMucSuuTamNgheThuat(ActionEvent event) {
        // goTo("/views/bidder/category-view.fxml", loader -> {
        //     CategoryController categoryController = loader.getController();
        //     categoryController.resetCategoryData("SƯU TẦM - NGHỆ THUẬT", ProductLoader.loadProducts());
        // });
    }

    @FXML
    private void showDanhMucTaiSanKhac(ActionEvent event) {
        // goTo("/views/bidder/category-view.fxml", loader -> {
        //     CategoryController categoryController = loader.getController();
        //     categoryController.resetCategoryData("TÀI SẢN KHÁC", ProductLoader.loadProducts());
        // });
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
    private void handleLogout(MouseEvent event) {
        SessionManager.logout();
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