package com.bidnova;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.bidnova.services.DatabaseInitializer;

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