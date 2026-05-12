package com.daugia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.services.AuctionManager;

public class ServerMain {
    private static final int PORT = 8888;

    public static void main(String[] args) {

        // ── Load dữ liệu thật từ DB thay vì hardcode ────────────────────────
        // AuctionDAO đọc bảng auctions JOIN items → tạo Auction objects → nạp vào AuctionManager
        // Sau bước này, getAuction(1) và getAuction(2) sẽ trả về object thật từ DB
        AuctionDAO    dao     = new AuctionDAO();
        List<Auction> list    = dao.loadAllAuctions();
        AuctionManager manager = AuctionManager.getInstance();

        for (Auction auction : list) {
            manager.addAuction(auction);
            System.out.println("Đã load phiên đấu giá: id="
                + auction.getAuctionId() + " | " + auction.getItem().getName()
                + " | Giá khởi điểm: " + String.format("%,.0f", auction.getCurrentHighestBid()));
        }
        System.out.println("Tổng: " + list.size() + " phiên đấu giá đang active.");
        // ────────────────────────────────────────────────────────────────────

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy tại port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("CLIENT KẾT NỐI: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}