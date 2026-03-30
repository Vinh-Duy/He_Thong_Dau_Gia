package com.daugia.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.daugia.controllers.bidder.DanhSachSanPhamController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeaderController {

    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    
    @FXML private Label trangChuLabel;
    @FXML private Label taiSanLabel;
    @FXML private Label phienDauGiaLabel;
    @FXML private Label gioiThieuLabel;
    @FXML private Label lienHeLabel;

    @FXML
    public void initialize() {
        startClock();
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

    private void setActiveMenu(Label activeLabel) {

        Label[] menuLabels = {trangChuLabel, taiSanLabel, phienDauGiaLabel, gioiThieuLabel, lienHeLabel};
        
        for (Label label : menuLabels) {
            label.getStyleClass().remove("menu-item-active");
        }
        
        activeLabel.getStyleClass().add("menu-item-active");
    }

    private void updateMainContent(Parent newContent) {
        Stage stage = (Stage) timeLabel.getScene().getWindow();
        if (stage.getScene() == null) return;
        
        Parent root = stage.getScene().getRoot();
        Node mainNode = root;
        
        if (root instanceof ScrollPane) {
            mainNode = ((ScrollPane) root).getContent();
            
            ScrollPane sp = (ScrollPane) root;
            Platform.runLater(() -> sp.setVvalue(0.0));
        }
        
        if (mainNode instanceof StackPane) {
            StackPane stackPane = (StackPane) mainNode;
            
            StackPane.setMargin(newContent, new Insets(100, 0, 0, 0));
            
            if (!stackPane.getChildren().isEmpty()) {
                stackPane.getChildren().set(0, newContent);
            } else {
                stackPane.getChildren().add(newContent);
            }
        } 
        else if (mainNode instanceof BorderPane) {
            BorderPane borderPane = (BorderPane) mainNode;
            
            BorderPane.setMargin(newContent, new Insets(0, 0, 0, 0));
            
            borderPane.setCenter(newContent); 
        } 
        else {
            System.err.println("Lỗi: Không tìm thấy StackPane hay BorderPane ở gốc để thay đổi nội dung!");
        }
    }

    @FXML
    private void showTrangChu(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/HomeView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            
            if (root instanceof ScrollPane) {
                ScrollPane sp = (ScrollPane) root;
                
                sp.requestFocus();
                
                Platform.runLater(() -> {
                    sp.setVvalue(0.0);
                    Platform.runLater(() -> {
                        sp.setVvalue(0.0);
                    });
                });
            }
            
            setActiveMenu(trangChuLabel);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển về Trang chủ:");
            e.printStackTrace();
        }
    }

    private void loadPlaceholderContent(String title) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(new Label("Placeholder cho: " + title));
        
        updateMainContent(vbox);
    }

    @FXML
    private void showTaiSanMenu(MouseEvent event) {
        setActiveMenu(taiSanLabel); 
        
        ContextMenu menu = new ContextMenu();

        MenuItem batDongSan = new MenuItem("Bất động sản");
        batDongSan.setOnAction(e -> loadCategoryView("Bất động sản"));

        MenuItem nhaNuoc = new MenuItem("Tài sản nhà nước");
        nhaNuoc.setOnAction(e -> loadCategoryView("Tài sản nhà nước"));

        MenuItem phuongTien = new MenuItem("Phương tiện - xe cộ");
        phuongTien.setOnAction(e -> loadCategoryView("Phương tiện - xe cộ"));

        MenuItem suuTam = new MenuItem("Sưu tầm - nghệ thuật");
        suuTam.setOnAction(e -> loadCategoryView("Sưu tầm - nghệ thuật"));

        MenuItem khac = new MenuItem("Tài sản khác");
        khac.setOnAction(e -> loadCategoryView("Tài sản khác"));

        menu.getItems().addAll(batDongSan, nhaNuoc, phuongTien, suuTam, khac);
        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
    }

    @FXML
    private void showPhienDauGiaMenu(MouseEvent event) {
        setActiveMenu(phienDauGiaLabel);
        
        ContextMenu menu = new ContextMenu();
        
        MenuItem sapdaugia = new MenuItem("Sắp đấu giá");
        sapdaugia.setOnAction(e -> loadCenterContent("/views/SapDauGiaView.fxml"));

        MenuItem dangdienra = new MenuItem("Đang diễn ra");
        dangdienra.setOnAction(e -> loadCenterContent("/views/DangDienRaView.fxml"));

        MenuItem daketthuc = new MenuItem("Phiên đấu giá đã kết thúc");
        daketthuc.setOnAction(e -> loadCenterContent("/views/DaKetThucView.fxml"));

        menu.getItems().addAll(sapdaugia, dangdienra, daketthuc);
        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
    }

    @FXML
    private void showLienHe(MouseEvent event) {
        setActiveMenu(lienHeLabel);
        loadCenterContent("/views/common/LienHeView.fxml");
    }

    private void loadCategoryView(String categoryName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/DanhSachSanPhamView.fxml"));
            Parent content = loader.load();

            DanhSachSanPhamController controller = loader.getController();
            controller.setCategory(categoryName);

            updateMainContent(content);
            
        } catch (Exception e) {
            System.err.println("Lỗi tải danh mục: " + categoryName);
            e.printStackTrace();
        }
    }

    @FXML
    private void showGioiThieu(MouseEvent event) {
        loadCenterContent("/views/common/GioiThieuView.fxml");
        setActiveMenu(gioiThieuLabel);
    }


    @FXML
    private void goToSignupClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/SignupView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.getScene().setRoot(root);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi mở Đăng ký:");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLoginClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.getScene().setRoot(root);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi mở Đăng nhập:");
            e.printStackTrace();
        }
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();
            
            updateMainContent(newContent);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tải trang: " + fxmlPath);
            e.printStackTrace();
        }
    }
}