package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.bidnova.controllers.bidder.CategoryController;
import com.bidnova.utils.ProductLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeaderController {
    @FXML
    private HBox header;
    @FXML
    private BorderPane category;
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
            categoryController.resetCategoryData("BẤT ĐỘNG SẢN",
                    ProductLoader.loadProducts());
        });
    }

    @FXML
    private void showDanhMucTaiSanNhaNuoc(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.resetCategoryData("TÀI SẢN NHÀ NƯỚC", ProductLoader.loadProducts());
        });
    }

    @FXML
    private void showDanhMucPhuongTienXeCo(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.resetCategoryData("PHƯƠNG TIỆN - XE CỘ", ProductLoader.loadProducts());
        });
    }

    @FXML
    private void showDanhMucSuuTamNgheThuat(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.resetCategoryData("SƯU TẦM - NGHỆ THUẬT", ProductLoader.loadProducts());
        });
    }

    @FXML
    private void showDanhMucTaiSanKhac(ActionEvent event) {
        goTo("/views/bidder/category-view.fxml", loader -> {
            CategoryController categoryController = loader.getController();
            categoryController.resetCategoryData("TÀI SẢN KHÁC", ProductLoader.loadProducts());
        });
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