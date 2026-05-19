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

// package com.bidnova.controllers.components;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

// import com.bidnova.controllers.bidder.DanhSachSanPhamController;

// import javafx.animation.KeyFrame;
// import javafx.animation.Timeline;
// import javafx.application.Platform;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.geometry.Side;
// import javafx.scene.Node;
// import javafx.scene.Parent;
// import javafx.scene.control.ContextMenu;
// import javafx.scene.control.Label;
// import javafx.scene.control.MenuItem;
// import javafx.scene.control.ScrollPane;
// import javafx.scene.input.MouseEvent;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.StackPane;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
// import javafx.util.Duration;


// public class HeaderController {

//     @FXML private Label timeLabel;
//     @FXML private Label dateLabel;
    
//     @FXML private Label trangChuLabel;
//     @FXML private Label taiSanLabel;
//     @FXML private Label gioiThieuLabel;
//     @FXML private Label lienHeLabel;

//     @FXML private HBox authButtonsBox;
//     @FXML private HBox userInfoBox;
//     @FXML private Label userNameLabel;

//     // Mẹo để gọi Header từ bất cứ đâu
//     private static HeaderController instance;
//     public static HeaderController getInstance() { return instance; }

//     // Hàm "ảo thuật" chính
//     public void updateHeaderUI() {
//         boolean loggedIn = com.bidnova.utils.SessionManager.isLoggedIn();
        
//         // Hiện cái này thì ẩn cái kia
//         authButtonsBox.setVisible(!loggedIn);
//         authButtonsBox.setManaged(!loggedIn);
        
//         userInfoBox.setVisible(loggedIn);
//         userInfoBox.setManaged(loggedIn);

//         if (loggedIn) {
//             // Lấy tên user đã lưu trong Session (Bác nhớ lưu cả username vào SessionManager nhé)
//             userNameLabel.setText("Xin chào, " + com.bidnova.utils.SessionManager.getUsername());
//         }
//     }

//     @FXML
//     private void handleLogout(MouseEvent event) {
//         // 1. Xóa sạch Token và Username trong két sắt
//         com.bidnova.utils.SessionManager.logout();

//         // 2. Cập nhật lại giao diện Header (ẩn tên User, hiện lại nút Đăng nhập)
//         updateHeaderUI();

//         // 3. Thông báo cho người dùng biết
//         javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
//         alert.setTitle("Đăng xuất");
//         alert.setHeaderText(null);
//         alert.setContentText("Bác đã đăng xuất thành công!");
//         alert.showAndWait();

//         // 4. Chuyển về màn hình đăng nhập
//         try {
//             javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
//             javafx.scene.Parent root = loader.load();
//             javafx.scene.Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
//             scene.setRoot(root);
//             javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
//             stage.setTitle("Đăng nhập");
//             stage.sizeToScene();
//             stage.centerOnScreen();
//         } catch (Exception e) {
//             System.err.println("Lỗi khi chuyển về đăng nhập:");
//             e.printStackTrace();
//         }
//     }

//     @FXML
//     public void initialize() {
//         startClock();
//         instance = this; // Lưu lại chính nó khi vừa khởi tạo
//         updateHeaderUI(); // Kiểm tra trạng thái đăng nhập ngay khi mở app
//     }

//     private void startClock() {

//         DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.S");
//         DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");

//         Timeline clock = new Timeline(new KeyFrame(Duration.millis(100), e -> {
//             LocalDateTime now = LocalDateTime.now();
//             timeLabel.setText(now.format(timeFormatter));
//             dateLabel.setText(now.format(dateFormatter));
//         }));
//         clock.setCycleCount(Timeline.INDEFINITE);
//         clock.play();
//     }

//     private void setActiveMenu(Label activeLabel) {

//         Label[] menuLabels = {trangChuLabel, taiSanLabel, gioiThieuLabel, lienHeLabel};
        
//         for (Label label : menuLabels) {
//             label.getStyleClass().remove("menu-item-active");
//         }
        
//         activeLabel.getStyleClass().add("menu-item-active");
//     }

//     private void updateMainContent(Parent newContent) {
//         Stage stage = (Stage) timeLabel.getScene().getWindow();
//         if (stage.getScene() == null) return;
        
//         Parent root = stage.getScene().getRoot();
//         Node mainNode = root;
        
//         if (root instanceof ScrollPane) {
//             mainNode = ((ScrollPane) root).getContent();
            
//             ScrollPane sp = (ScrollPane) root;
//             Platform.runLater(() -> sp.setVvalue(0.0));
//         }
        
//         if (mainNode instanceof StackPane) {
//             StackPane stackPane = (StackPane) mainNode;
            
//             StackPane.setMargin(newContent, new Insets(100, 0, 0, 0));
            
//             if (!stackPane.getChildren().isEmpty()) {
//                 stackPane.getChildren().set(0, newContent);
//             } else {
//                 stackPane.getChildren().add(newContent);
//             }
//         } 
//         else if (mainNode instanceof BorderPane) {
//             BorderPane borderPane = (BorderPane) mainNode;
            
//             BorderPane.setMargin(newContent, new Insets(0, 0, 0, 0));
            
//             borderPane.setCenter(newContent); 
//         } 
//         else {
//             System.err.println("Lỗi: Không tìm thấy StackPane hay BorderPane ở gốc để thay đổi nội dung!");
//         }
//     }

//     @FXML
//     private void showTrangChu(MouseEvent event) {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/home-view.fxml"));
//             Parent root = loader.load();
//             Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//             stage.getScene().setRoot(root);
            
//             if (root instanceof ScrollPane) {
//                 ScrollPane sp = (ScrollPane) root;
                
//                 sp.requestFocus();
                
//                 Platform.runLater(() -> {
//                     sp.setVvalue(0.0);
//                     Platform.runLater(() -> {
//                         sp.setVvalue(0.0);
//                     });
//                 });
//             }
            
//             setActiveMenu(trangChuLabel);
//         } catch (Exception e) {
//             System.err.println("Lỗi khi chuyển về Trang chủ:");
//             e.printStackTrace();
//         }
//     }

//     private void loadPlaceholderContent(String title) {
//         VBox vbox = new VBox();
//         vbox.setAlignment(Pos.CENTER);
//         vbox.getChildren().add(new Label("Placeholder cho: " + title));
        
//         updateMainContent(vbox);
//     }

//     @FXML
//     private void showTaiSanMenu(MouseEvent event) {
//         setActiveMenu(taiSanLabel); 
        
//         ContextMenu menu = new ContextMenu();

//         MenuItem batDongSan = new MenuItem("Bất động sản");
//         batDongSan.setOnAction(e -> loadCategoryView("Bất động sản"));

//         MenuItem nhaNuoc = new MenuItem("Tài sản nhà nước");
//         nhaNuoc.setOnAction(e -> loadCategoryView("Tài sản nhà nước"));

//         MenuItem phuongTien = new MenuItem("Phương tiện - xe cộ");
//         phuongTien.setOnAction(e -> loadCategoryView("Phương tiện - xe cộ"));

//         MenuItem suuTam = new MenuItem("Sưu tầm - nghệ thuật");
//         suuTam.setOnAction(e -> loadCategoryView("Sưu tầm - nghệ thuật"));

//         MenuItem khac = new MenuItem("Tài sản khác");
//         khac.setOnAction(e -> loadCategoryView("Tài sản khác"));

//         menu.getItems().addAll(batDongSan, nhaNuoc, phuongTien, suuTam, khac);
//         menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
//     }

//     @FXML
//     private void showLienHe(MouseEvent event) {
//         setActiveMenu(lienHeLabel);
//         loadCenterContent("/views/common/LienHeView.fxml");
//     }

//     private void loadCategoryView(String categoryName) {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/DanhSachSanPhamView.fxml"));
//             Parent content = loader.load();

//             DanhSachSanPhamController controller = loader.getController();
//             controller.setCategory(categoryName);

//             updateMainContent(content);
            
//         } catch (Exception e) {
//             System.err.println("Lỗi tải danh mục: " + categoryName);
//             e.printStackTrace();
//         }
//     }

//     @FXML
//     private void showGioiThieu(MouseEvent event) {
//         loadCenterContent("/views/common/GioiThieuView.fxml");
//         setActiveMenu(gioiThieuLabel);
//     }


//     @FXML
//     private void goToSignupClick(MouseEvent event) {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/SignupView.fxml"));
//             Parent root = loader.load();
            
//             Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
//             stage.getScene().setRoot(root);
            
//         } catch (Exception e) {
//             System.err.println("Lỗi khi mở Đăng ký:");
//             e.printStackTrace();
//         }
//     }

//     @FXML
//     private void goToLoginClick(MouseEvent event) {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
//             Parent root = loader.load();
            
//             Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
//             stage.getScene().setRoot(root);
            
//         } catch (Exception e) {
//             System.err.println("Lỗi khi mở Đăng nhập:");
//             e.printStackTrace();
//         }
//     }

//     private void loadCenterContent(String fxmlPath) {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//             Parent newContent = loader.load();
            
//             updateMainContent(newContent);
            
//         } catch (Exception e) {
//             System.err.println("Lỗi khi tải trang: " + fxmlPath);
//             e.printStackTrace();
//         }
//     }
// }