package com.daugia;

import com.daugia.network.NetworkClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Kết nối mạng
            NetworkClient.connect("localhost", 8888);

            // NẠP TRANG CHỦ (HOMEVIEW) ĐẦU TIÊN
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/HomeView.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 720);
            
            stage.setTitle("Hệ thống Đấu Giá - Team 1");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("=== LỖI SẬP APP CHI TIẾT TẠI ĐÂY ===");
            e.printStackTrace(); // Lệnh này giúp in ra tận gốc dòng code gây lỗi
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