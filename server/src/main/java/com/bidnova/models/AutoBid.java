package com.bidnova.models;

import java.time.LocalDateTime;

/**
 * 🤖 AutoBid - Mô hình dữ liệu tự động đặt giá
 * 
 * <h2>Chức Năng:</h2>
 * <p>Cho phép người dùng thiết lập một quy tắc tự động đặt giá.</p>
 * <p>Khi có ai đó đặt giá, hệ thống tự động tăng giá lên bước tiếp theo
 * cho đến khi đạt <code>maxBid</code> hoặc giá trần.</p>
 * 
 * <h2>Ví Dụ Sử Dụng:</h2>
 * <pre>
 * // Người B thiết lập AutoBid
 * Ví dụ 1: Phiên đấu giá sắp kết thúc, B không thể theo dõi liên tục
 *   - maxBid: 150,000,000 đ (tối đa B sẵn sàng trả)
 *   - increment: 5,000,000 đ (bước tăng mỗi lần)
 * 
 * Khi Người A đặt giá 100M:
 *   - AutoBidService tính: 100M + 5M = 105M
 *   - So sánh với maxBid (150M): 105M < 150M ✓
 *   - Tự động đặt giá 105M cho B
 * 
 * Khi Người A đặt lại 140M:
 *   - AutoBidService tính: 140M + 5M = 145M
 *   - So sánh với maxBid (150M): 145M < 150M ✓
 *   - Tự động đặt giá 145M cho B
 * 
 * Khi Người A đặt lại 148M:
 *   - AutoBidService tính: 148M + 5M = 153M
 *   - So sánh với maxBid (150M): 153M > 150M ✗
 *   - Chỉ đặt giá tới maxBid: 150M cho B
 *   - isActive = false (hết hạn)
 * </pre>
 * 
 * <h2>Lưu Trữ:</h2>
 * <p>Dữ liệu được lưu trong bảng <code>auto_bids</code> của database MySQL.</p>
 * <pre>
 * CREATE TABLE auto_bids (
 *   id INT PRIMARY KEY AUTO_INCREMENT,
 *   auction_id VARCHAR(50) NOT NULL,
 *   user_id INT NOT NULL,
 *   max_bid DECIMAL(20,2) NOT NULL,         -- Giá tối đa sẵn sàng trả
 *   increment DECIMAL(20,2) NOT NULL,       -- Bước giá tăng mỗi lần
 *   is_active BOOLEAN DEFAULT true,         -- Còn hoạt động không
 *   created_at DATETIME,
 *   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   FOREIGN KEY (auction_id) REFERENCES auctions(id),
 *   FOREIGN KEY (user_id) REFERENCES users(id)
 * );
 * </pre>
 * 
 * <h2>Trạng Thái (isActive):</h2>
 * <ul>
 *   <li><b>true:</b> AutoBid còn hoạt động, sẽ tự động đặt giá nếu bị vượt qua</li>
 *   <li><b>false:</b> AutoBid bị vô hiệu hóa (đạt maxBid, phiên kết thúc, hoặc người dùng huỷ)</li>
 * </ul>
 * 
 * <h2>Tính Năng Nâng Cao:</h2>
 * <ul>
 *   <li>✅ Tự động điều chỉnh increment nếu < minBidIncrement</li>
 *   <li>✅ Kiểm tra price ceiling - tự động kết thúc phiên nếu đạt</li>
 *   <li>✅ Thread-safe: Sử dụng synchronized trên Auction object</li>
 * </ul>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see com.bidnova.services.AutoBidService
 * @see Auction
 * @see User
 */
public class AutoBid {
    private int id;
    private String auctionId;
    private int userId;
    private double maxBid;
    private double increment;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    /**
     * Constructor - Khởi tạo AutoBid mới
     * 
     * <p>Thiết lập mặc định:</p>
     * <ul>
     *   <li>isActive = true (bắt đầu hoạt động)</li>
     *   <li>createdAt = LocalDateTime.now() (thời gian hiện tại)</li>
     * </ul>
     */
    public AutoBid() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    /**
     * @return ID duy nhất của AutoBid rule (auto-generated)
     */
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    /**
     * @return ID phiên đấu giá (FK tới Auction)
     */
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    
    /**
     * @return ID người dùng đặt AutoBid (FK tới User)
     */
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    /**
     * @return Giá tối đa sẵn sàng trả
     * 
     * <p>Khi AutoBid tính toán giá tiếp theo mà vượt quá maxBid,
     * sẽ chỉ đặt lên đến maxBid này và sau đó vô hiệu hóa.</p>
     */
    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }
    
    /**
     * @return Bước giá tự động tăng mỗi lần
     * 
     * <p>Ví dụ: nếu increment = 5,000,000 (5M):
     * <ul>
     *   <li>Giá hiện tại 100M → AutoBid sẽ đặt 105M</li>
     *   <li>Giá hiện tại 110M → AutoBid sẽ đặt 115M</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Lưu ý:</strong> AutoBidService sẽ điều chỉnh nếu increment
     * nhỏ hơn minBidIncrement của phiên đấu giá.</p>
     */
    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }
    
    /**
     * @return true nếu AutoBid còn hoạt động, false nếu bị vô hiệu hóa
     * 
     * <h3>Khi nào bị vô hiệu hóa (isActive = false)?</h3>
     * <ul>
     *   <li>AutoBid đặt giá tới maxBid và bị vượt qua</li>
     *   <li>Phiên đấu giá kết thúc (FINISHED hoặc CLOSED)</li>
     *   <li>Người dùng huỷ AutoBid</li>
     *   <li>Đạt price ceiling của phiên</li>
     * </ul>
     */
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    /**
     * @return Thời gian tạo AutoBid
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
