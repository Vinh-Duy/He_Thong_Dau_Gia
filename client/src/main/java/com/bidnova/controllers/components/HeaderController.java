package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeaderController {
    @FXML
    private HBox header;

    @FXML
    private Label trangChuLabel;
    @FXML
    private Label taiSanLabel;
    @FXML
    private Label phienDauGiaLabel;
    @FXML
    private Label gioiThieuLabel;
    @FXML
    private Label lienHeLabel;

    @FXML
    private Label timeLabel;
    @FXML
    private Label dateLabel;

    @FXML
    public void initialize() {
        startClock();
        header.toFront();
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
    private void showTrangChu(MouseEvent event) {
        goTo("/views/common/home-view.fxml");
    }

    @FXML
    private void showDanhMucBatDongSan(ActionEvent event) {
        goTo("/views/bidder/bat-dong-san-view.fxml");
        System.out.println("Đang show danh mục Bất động sản");
    }

    @FXML
    private void showDanhMucTaiSanNhaNuoc(ActionEvent event) {
        // todo: chuyển tới trang tài sản nhà nước
        System.out.println("Đang show danh mục Tài sản nhà nước");
    }

    @FXML
    private void showDanhMucPhuongTienXeCo(ActionEvent event) {
        // todo: chuyển tới trang phương tiện xe cộ
        System.out.println("Đang show danh mục Phương tiện xe cộ");
    }

    @FXML
    private void showDanhMucSuuTamNgheThuat(ActionEvent event) {
        // todo: chuyển tới trang sưu tầm nghệ thuật
        System.out.println("Đang show danh mục Sưu tầm nghệ thuật");
    }

    @FXML
    private void showDanhMucTaiSanKhac(ActionEvent event) {
        // todo: chuyển tới trang tài sản khác
        System.out.println("Đang show danh mục Tài sản khác");
    }

    @FXML
    private void showDanhMucSapDauGia(ActionEvent event) {
        // todo: chuyển tới trang sắp đấu giá
        System.out.println("Đang show danh mục Sắp đấu giá");
    }

    @FXML
    private void showDanhMucDangDauGia(ActionEvent event) {
        // todo: chuyển tới trang đang đấu giá
        System.out.println("Đang show danh mục Đang đấu giá");
    }

    @FXML
    private void showDanhMucDaDauGia(ActionEvent event) {
        // todo: chuyển tới trang đã đấu giá
        System.out.println("Đang show danh mục Đã đấu giá");
    }

    @FXML
    private void showGioiThieu(MouseEvent event) {
        goTo("/views/common/gioi-thieu-view.fxml");
    }

    @FXML
    private void showLienHe(MouseEvent event) {
        goTo("/views/common/lien-he-view.fxml");
    }

    @FXML
    private void goToSignup(MouseEvent event) {
        goTo("/views/auth/signup-view.fxml");
    }

    @FXML
    private void goToSignin(MouseEvent event) {
        goTo("/views/auth/signin-view.fxml");
    }
}