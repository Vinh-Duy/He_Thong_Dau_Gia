package com.daugia;

import com.daugia.network.NetworkClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        NetworkClient.connect("localhost", 8888);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/auth/SignupView.fxml"));

        Scene scene = new Scene(loader.load(), 1280, 650);
        stage.setTitle("Developed by Team 1");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Đang đóng ứng dụng và ngắt kết nối mạng...");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}