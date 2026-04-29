package com.bidnova;

import java.net.ServerSocket;
import java.net.Socket;

import com.bidnova.network.ClientHandler;

public class ServerMain {
    private static final int PORT = 8888;
    
    public static void main(String[] args) {
        System.out.println("Server đang chạy trên cổng " + PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Đã kết nối với client: " + clientSocket.getInetAddress());
                // Tạo một luồng mới để xử lý kết nối với client
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi khởi động server!");
            e.printStackTrace();
        }
    }
}
