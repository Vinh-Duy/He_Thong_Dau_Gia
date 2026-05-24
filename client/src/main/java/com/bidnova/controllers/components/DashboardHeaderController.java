package com.bidnova.controllers.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bidnova.utils.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardHeaderController {
    @FXML private HBox header;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private HBox userBox;
    @FXML private Label userLabel;

    @FXML
    public void initialize() {
        startClock();
        header.toFront();

        if (SessionManager.isLoggedIn()) {
            showUserBox(SessionManager.getUsername());
        }
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
    private void handleLogout(MouseEvent event) {
        SessionManager.logout();
        
        // Chuyển về màn hình đăng nhập
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/signin-view.fxml"));
            Stage stage = (Stage) header.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng nhập");
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showUserBox(String userName) {
        userBox.setVisible(true);
        userBox.setManaged(true);
        userLabel.setText("Xin chào, " + userName);
    }
}