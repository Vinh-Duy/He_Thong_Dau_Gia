package com.bidnova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/home-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 640);

            stage.setTitle("BidNova");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.out.println("Lỗi khi khởi chạy");
            e.printStackTrace();
        }
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