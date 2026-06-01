package com.bidnova;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bidnova.models.Auction;
import com.bidnova.services.DatabaseInitializer;
import com.bidnova.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 🚀 ServerMain - Điểm khởi động của hệ thống Auction Server
 * 
 * <h2>Chức Năng:</h2>
 * <ul>
 *   <li>Khởi động server trên port 8888</li>
 *   <li>Chờ kết nối từ các client</li>
 *   <li>Tạo một thread riêng cho mỗi client kết nối (mô hình multi-threaded)</li>
 *   <li>Khởi tạo dữ liệu phiên đấu giá hoạt động từ database</li>
 * </ul>
 * 
 * <h2>Kiến Trúc:</h2>
 * <pre>
 * Client 1 ──┐
 * Client 2 ──┼─→ ServerSocket (Port 8888) ──→ Thread Pool
 * Client 3 ──┘
 * 
 * Mỗi client được xử lý bởi một ClientHandler instance chạy trong thread riêng
 * </pre>
 * 
 * <h2>Luồng Hoạt Động:</h2>
 * <ol>
 *   <li>Khởi tạo DatabaseInitializer để load phiên đấu giá từ DB</li>
 *   <li>Mở ServerSocket lắng nghe trên port 8888</li>
 *   <li>Vòng lặp while(true) chấp nhận client kết nối</li>
 *   <li>Tạo ClientHandler instance và chạy trong thread mới</li>
 * </ol>
 * 
 * @author BidNova Team
 * @version 1.0
 * @since 2026-05
 */
public class ServerMain {
    private static final int PORT = 8888;

    /**
     * Main method - Điểm khởi động của server
     * 
     * @param args Tham số dòng lệnh (không được sử dụng)
     * 
     * <p><strong>Ví dụ chạy:</strong></p>
     * <pre>
     * $ mvn exec:java -Dexec.mainClass="com.bidnova.ServerMain"
     * Server đang chạy tại port 8888...
     * CÓ CLIENT MỚI KẾT NỐI: 192.168.1.100
     * CÓ CLIENT MỚI KẾT NỐI: 192.168.1.101
     * </pre>
     */
    public static void main(String[] args) {
        DatabaseInitializer.initializeActiveAuctions();

        // Background thread kiểm tra auction hết hạn và broadcast event khi cần
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Auction> expiredAuctions = AuctionManager.getInstance().scanExpiredAuctions();
                Gson gson = new Gson();

                for (Auction auction : expiredAuctions) {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("auctionId", auction.getId());
                    payload.addProperty("highestBidder", auction.getHighestBidder() != null ? auction.getHighestBidder() : "Không có người thắng");
                    payload.addProperty("finalBid", auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice());
                    payload.addProperty("message", "Phiên đấu giá đã kết thúc theo thời gian. Người thắng: " +
                            (auction.getHighestBidder() != null ? auction.getHighestBidder() : "Không có người thắng") +
                            ", Giá: " + (auction.getCurrentHighestBid() > 0 ? auction.getCurrentHighestBid() : auction.getStartPrice()));

                    JsonObject event = new JsonObject();
                    event.addProperty("action", "AUCTION_FINISHED");
                    event.addProperty("payload", gson.toJson(payload));
                    ClientHandler.broadcastAll(gson.toJson(event));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20, 20, TimeUnit.SECONDS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy tại port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("CÓ CLIENT MỚI KẾT NỐI: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdown();
        }
    }
}