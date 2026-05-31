package com.bidnova.models;

import java.time.LocalDateTime;

/**
 * 📝 BidHistory - Ghi chép lịch sử từng lần đặt giá
 * 
 * <h2>Chức Năng:</h2>
 * <p>Lưu trữ mọi lần đặt giá trong một phiên đấu giá để có thể:</p>
 * <ul>
 *   <li>Theo dõi lịch sử đấu giá</li>
 *   <li>Xem ai đã đặt giá bao nhiêu vào lúc nào</li>
 *   <li>Kiểm tra tính minh bạch và công bằng</li>
 *   <li>Phục vụ mục đích audit/báo cáo</li>
 * </ul>
 * 
 * <h2>Lưu Trữ:</h2>
 * <p>Dữ liệu được lưu trong bảng <code>bid_history</code> của database MySQL.</p>
 * <pre>
 * CREATE TABLE bid_history (
 *   id INT PRIMARY KEY AUTO_INCREMENT,
 *   auction_id VARCHAR(50) NOT NULL,
 *   user_id INT NOT NULL,
 *   username VARCHAR(100) NOT NULL,
 *   bid_amount DECIMAL(20,2) NOT NULL,
 *   bid_time DATETIME NOT NULL,
 *   FOREIGN KEY (auction_id) REFERENCES auctions(id),
 *   FOREIGN KEY (user_id) REFERENCES users(id)
 * );
 * </pre>
 * 
 * <h2>Ví Dụ Lịch Sử:</h2>
 * <pre>
 * Auction: "Toyota Camry 2020"
 * 
 * | ID | Username | Bid Amount   | Time                |
 * |----|----------|--------------|---------------------|
 * | 1  | UserA    | 100,000,000  | 2026-05-31 14:00:00 |
 * | 2  | UserB    | 105,000,000  | 2026-05-31 14:05:00 |
 * | 3  | UserA    | 115,000,000  | 2026-05-31 14:10:00 |
 * | 4  | UserB    | 120,000,000  | 2026-05-31 14:12:00 | (AutoBid)
 * | 5  | UserC    | 125,000,000  | 2026-05-31 14:15:00 |
 * | 6  | UserB    | 130,000,000  | 2026-05-31 14:16:00 | (AutoBid)
 * </pre>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see Auction
 * @see User
 */
public class BidHistory {
    private int id;
    private String auctionId;
    private int userId;
    private String username;
    private double bidAmount;
    private LocalDateTime bidTime;

    /**
     * Constructor cơ bản - Tạo record lịch sử (thường dùng khi thêm mới)
     * 
     * @param auctionId ID phiên đấu giá
     * @param userId    ID người dùng đặt giá
     * @param username  Tên người dùng
     * @param bidAmount Số tiền đặt giá
     * @param bidTime   Thời gian đặt giá
     */
    public BidHistory(String auctionId, int userId, String username, double bidAmount, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.username = username;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    /**
     * Constructor đầy đủ - Tạo record với ID (thường dùng khi đọc từ DB)
     * 
     * @param id        ID duy nhất của record (auto-generated)
     * @param auctionId ID phiên đấu giá
     * @param userId    ID người dùng
     * @param username  Tên người dùng
     * @param bidAmount Số tiền đặt giá
     * @param bidTime   Thời gian đặt giá
     */
    public BidHistory(int id, String auctionId, int userId, String username, double bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.username = username;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    // ==================== GETTERS ====================
    
    /**
     * @return ID duy nhất của record (auto-generated)
     */
    public int getId() {
        return id;
    }

    /**
     * @return ID phiên đấu giá
     */
    public String getAuctionId() {
        return auctionId;
    }

    /**
     * @return ID người dùng đặt giá (FK tới User)
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return Tên người dùng (snapshot tại thời điểm đặt giá)
     */
    public String getUsername() {
        return username;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    @Override
    public String toString() {
        return "BidHistory{" + "id=" + id + ", auctionId='" + auctionId + '\'' + ", userId=" + userId + ", username='" + username + '\'' + ", bidAmount=" + bidAmount + ", bidTime=" + bidTime + '}';
    }
}