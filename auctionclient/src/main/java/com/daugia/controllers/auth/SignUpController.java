package com.daugia.controllers.auth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.daugia.controllers.bidder.DanhSachSanPhamController;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SignUpController {

    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    @FXML private ComboBox<Integer> dayBox;
    @FXML private ComboBox<Integer> monthBox;
    @FXML private ComboBox<Integer> yearBox;
    @FXML private ComboBox<String> genderBox;

    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleNumber;
    @FXML private Label ruleSpecial;
    @FXML private RadioButton personalRadio;
    @FXML private RadioButton orgRadio;
    @FXML private TextField passwordTextField;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Label usernameError;
    @FXML private Label passwordError;

    @FXML private BorderPane mainBorderPane;

    @FXML
    private void handleSignup(ActionEvent event) {
        usernameError.setText("");
        passwordError.setText("");

        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean hasError = false;

        if (username.length() < 3) {
            usernameError.setText("Ít nhất 3 ký tự");
            hasError = true;
        }

        if (password.length() < 6) {
            passwordError.setText("Mật khẩu ≥ 6 ký tự");
            hasError = true;
        }

        if (hasError) return;

        System.out.println("Đang gửi yêu cầu đăng ký lên Server...");
        
        String payload = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

        Request req = new Request("REGISTER", payload);

        Response res = NetworkClient.sendRequest(req);

        if (res != null && "SUCCESS".equals(res.getStatus())) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText(res.getMessage());
            alert.showAndWait();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml")); 
                Parent loginView = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(loginView, 1280, 650));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi đăng ký");
            alert.setHeaderText(null);
            alert.setContentText(res != null ? res.getMessage() : "Lỗi kết nối đến máy chủ!");
            alert.showAndWait();
        }
    }

    @FXML
    private void goToSignin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/LoginPopup.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginView, 1280, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePassword() {
        if (passwordField.isVisible()) {

            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {

            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }
    @FXML
    public void initialize() {

        ToggleGroup group = new ToggleGroup();
        if (personalRadio != null) personalRadio.setToggleGroup(group);
        if (orgRadio != null) orgRadio.setToggleGroup(group);

        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword(newVal));
        }

        if (genderBox != null) {
            genderBox.getItems().addAll("Nam", "Nữ");
        }

        if (dayBox != null) {
            for (int i = 1; i <= 31; i++) dayBox.getItems().add(i);
        }
        if (monthBox != null) {
            for (int i = 1; i <= 12; i++) monthBox.getItems().add(i);
        }
        if (yearBox != null) {
            for (int i = 1950; i <= 2025; i++) yearBox.getItems().add(i);
        }

        startClock();
    }
    @FXML

    private void validatePassword(String pass) {

        updateRule(ruleLength, pass.length() >= 8);

        updateRule(ruleUpper, pass.matches(".*[A-Z].*"));

        updateRule(ruleLower, pass.matches(".*[a-z].*"));

        updateRule(ruleNumber, pass.matches(".*\\d.*"));

        updateRule(ruleSpecial, pass.matches(".*[!@#$%^&*()].*"));
    }

    private void updateRule(Label label, boolean ok) {
        if (ok) {
            label.setText("✔ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setText("✖ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void startClock() {

        Platform.runLater(() -> {

            if (timeLabel == null || dateLabel == null) {

                new Timeline(new KeyFrame(Duration.millis(100), e -> startClock())).play();
                return;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    LocalDateTime now = LocalDateTime.now();
                    timeLabel.setText(now.format(timeFormatter));
                    dateLabel.setText(now.format(dateFormatter));
                })
            );

            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        });
    }

    @FXML
    private void showPhienDauGiaMenu(MouseEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem sapdaugia = new MenuItem("Phiên đấu giá sắp đấu giá");
        sapdaugia.setOnAction(e -> loadCenterContent("/views/SapDauGiaView.fxml"));

        MenuItem  dangdienra = new MenuItem("Phiên đấu giá đang diễn ra");
        dangdienra.setOnAction(e -> loadCenterContent("/views/DangDienRaView.fxml"));

        MenuItem daketthuc = new MenuItem("Phiên đấu giá đã kết thúc");
        daketthuc.setOnAction(e -> loadCenterContent("/views/DaKetThucView.fxml"));

        menu.getItems().addAll(sapdaugia, dangdienra, daketthuc /* thêm các mục khác sau */);

        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
    }


    @FXML
    private void goToGioiThieu(MouseEvent event) {
        loadCenterContent("/views/common/GioiThieuView.fxml");
    }

    @FXML
    private void goToLienHe(MouseEvent event) {
        loadCenterContent("/views/common/LienHeView.fxml");
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            mainBorderPane.setCenter(content);
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Không thể tải trang: " + fxmlPath);
            alert.showAndWait();
        }
    }

    @FXML
    private void showTaiSanMenu(MouseEvent event) {

        ContextMenu menu = new ContextMenu();

        MenuItem batDongSan = new MenuItem("Bất động sản");

        MenuItem nhaNuoc = new MenuItem("Tài sản nhà nước");

        MenuItem phuongTien = new MenuItem("Phương tiện - xe cộ");

        MenuItem suuTam = new MenuItem("Sưu tầm - nghệ thuật");

        MenuItem khac = new MenuItem("Tài sản khác");

        menu.getItems().addAll(batDongSan, nhaNuoc, phuongTien, suuTam, khac);

        menu.show((Node) event.getSource(), Side.BOTTOM, 0, 5);
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