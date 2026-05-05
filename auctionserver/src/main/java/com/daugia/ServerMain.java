package com.daugia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.services.AuctionManager;

public class ServerMain {
    private static final int PORT = 8888;
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    private static volatile boolean running = true;

    public static void main(String[] args) {
        
        logger.info("Đang kết nối Database để nạp dữ liệu...");
        AuctionDAO dao = new AuctionDAO();
        java.util.List<Auction> activeAuctions = dao.getAllActiveAuctions();
        
        if (activeAuctions != null && !activeAuctions.isEmpty()) {
            for (Auction a : activeAuctions) {
                AuctionManager.getInstance().addAuction(a);
            }
            logger.info("Đã tải " + activeAuctions.size() + " phiên đấu giá vào bộ nhớ thành công!");
        } else {
            logger.warning("Không có dữ liệu đấu giá nào hoặc kết nối DB thất bại!");
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server đang chạy tại port " + PORT + "...");
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("CÓ CLIENT MỚI KẾT NỐI: " + clientSocket.getInetAddress());
                
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}