package com.bidnova;

import com.bidnova.network.NetworkClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            NetworkClient.getInstance();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/home-view.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setTitle("BidNova");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("=== LỖI SẬP APP CHI TIẾT TẠI ĐÂY ===");
            e.printStackTrace(); 

            System.exit(0);
            NetworkClient.getInstance().closeConnection();
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Đang đóng ứng dụng và ngắt kết nối mạng...");
        
        System.exit(0);
        NetworkClient.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}