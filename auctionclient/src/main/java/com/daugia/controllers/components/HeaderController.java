package com.daugia.controllers.components;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.daugia.controllers.bidder.DanhSachSanPhamController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeaderController {

    @FXML private Label taiSanLabel;
    @FXML private Label phienDauGiaLabel;
    @FXML private Label gioiThieuLabel;
    @FXML private Label lienHeLabel;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        startClock();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            timeLabel.setText(now.format(timeFormatter));
            dateLabel.setText(now.format(dateFormatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void loadCategoryView(String categoryName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/DanhSachSanPhamView.fxml"));
            Parent content = loader.load();

            DanhSachSanPhamController controller = loader.getController();
            controller.setCategory(categoryName);

            BorderPane mainBorderPane = (BorderPane) taiSanLabel.getScene().getRoot();
            mainBorderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            BorderPane mainBorderPane = (BorderPane) taiSanLabel.getScene().getRoot();
            mainBorderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTaiSanMenu(MouseEvent event) {
        ContextMenu menu = new ContextMenu();
        MenuItem batDongSan = new MenuItem("Bất động sản");
        batDongSan.setOnAction(e -> loadCategoryView("Bất động sản"));

        MenuItem nhaNuoc = new MenuItem("Tài sản nhà nước");
        nhaNuoc.setOnAction(e -> loadCategoryView("Tài sản nhà nước"));

        MenuItem phuongTien = new MenuItem("Phương tiện - xe cộ");
        phuongTien.setOnAction(e -> loadCategoryView("Phương tiện - xe cộ"));

        MenuItem suuTam = new MenuItem("Sưu tầm - nghệ thuật");
        suuTam.setOnAction(e -> loadCategoryView("Sưu tầm - nghệ thuật"));

        menu.getItems().addAll(batDongSan, nhaNuoc, phuongTien, suuTam);
        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
    }

    @FXML
    private void showPhienDauGiaMenu(MouseEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem sapdaugia = new MenuItem("Phiên đấu giá sắp đấu giá");
        sapdaugia.setOnAction(e -> loadCenterContent("/view/SapDauGiaView.fxml"));

        MenuItem  dangdienra = new MenuItem("Phiên đấu giá đang diễn ra");
        dangdienra.setOnAction(e -> loadCenterContent("/view/DangDienRaView.fxml"));

        MenuItem daketthuc = new MenuItem("Phiên đấu giá đã kết thúc");
        daketthuc.setOnAction(e -> loadCenterContent("/view/DaKetThucView.fxml"));

        menu.getItems().addAll(sapdaugia, dangdienra, daketthuc /* thêm các mục khác sau */);

        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
    }

    @FXML
    private void showGioiThieu(MouseEvent event) {
        loadCenterContent("/views/common/GioiThieuView.fxml");
    }

    @FXML
    private void showLienHe(MouseEvent event) {
        loadCenterContent("/views/common/LienHeView.fxml");
    }
    @FXML
    public void goToLoginClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) taiSanLabel.getScene().getWindow();
            stage.setScene(new Scene(loginView, 1280, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSignupClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/SignupView.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) taiSanLabel.getScene().getWindow();
            stage.setScene(new Scene(signupView, 1280, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}