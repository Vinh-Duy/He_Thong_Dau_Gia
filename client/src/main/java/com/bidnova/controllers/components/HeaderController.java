package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private void showTrangChu(MouseEvent event) {
        goTo(event, "/views/common/home-view.fxml");
    }

    @FXML
    private void showDanhMucBatDongSan(ActionEvent event) {
        // todo: chuyển tới trang bất động sản
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
        // try {
        // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // FXMLLoader loader = new
        // FXMLLoader(getClass().getResource("/views/common/gioi-thieu-view.fxml"));
        // Scene scene = new Scene(loader.load(), 1280, 640);
        // stage.setScene(scene);
        // stage.setMaximized(true);
        // } catch (Exception e) {
        // System.out.println("Lỗi khi show giới thiệu");
        // e.printStackTrace();
        // }
        goTo(event, "/views/common/gioi-thieu-view.fxml");
    }

    @FXML
    private void showLienHe(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/lien-he-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 640);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            System.out.println("Lỗi khi show liên hệ");
            e.printStackTrace();
        }
        goTo(event, "/views/common/lien-he-view.fxml");
    }

    @FXML
    private void goToSignup(MouseEvent event) {
        // try {
        // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // FXMLLoader loader = new
        // FXMLLoader(getClass().getResource("/views/auth/SignupView.fxml"));
        // Parent root = loader.load();

        // stage.getScene().setRoot(root);
        // } catch (Exception e) {
        // System.err.println("Lỗi khi mở Đăng ký:");
        // e.printStackTrace();
        // }
        goTo(event, "/views/auth/signup-view.fxml");
    }

    @FXML
    private void goToSignin(MouseEvent event) {
        // try {
        // FXMLLoader loader = new
        // FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
        // Parent root = loader.load();
        // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // stage.getScene().setRoot(root);
        // } catch (Exception e) {
        // System.err.println("Lỗi khi mở Đăng nhập:");
        // e.printStackTrace();
        // }
        goTo(event, "/views/auth/signin-view.fxml");
    }
}