package com.daugia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.services.AuctionManager;

public class ServerMain {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        
        System.out.println("Đang kết nối Database để nạp dữ liệu...");
        AuctionDAO dao = new AuctionDAO();
        java.util.List<Auction> activeAuctions = dao.getAllActiveAuctions();
        
        if (activeAuctions != null && !activeAuctions.isEmpty()) {
            for (Auction a : activeAuctions) {
                AuctionManager.getInstance().addAuction(a);
            }
            System.out.println("Đã tải " + activeAuctions.size() + " phiên đấu giá vào bộ nhớ thành công!");
        } else {
            System.out.println("Không có dữ liệu đấu giá nào hoặc kết nối DB thất bại!");
        }

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