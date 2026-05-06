package com.daugia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.daugia.services.DatabaseInitializer;

public class ServerMain {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        DatabaseInitializer.initializeActiveAuctions();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy tại port " + PORT + "...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("CÓ CLIENT MỚI KẾT NỐI: " + clientSocket.getInetAddress());
                
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}