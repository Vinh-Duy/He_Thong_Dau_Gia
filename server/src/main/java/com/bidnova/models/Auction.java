package com.bidnova.models;

import java.time.LocalDateTime;

/**
 * Auction - Mô hình dữ liệu phiên đấu giá
 * 
 * <h2>Chức Năng:</h2>
 * <p>Đại diện cho một phiên đấu giá sản phẩm trong hệ thống.</p>
 * <ul>
 *   <li>Lưu thông tin sản phẩm (tên, mô tả, hình ảnh, danh mục)</li>
 *   <li>Quản lý giá (giá khởi điểm, giá cao nhất hiện tại, người dẫn đầu)</li>
 *   <li>Quản lý thời gian (thời gian bắt đầu, kết thúc, status)</li>
 *   <li>Tính năng nâng cao: giá trần (price ceiling), bước giá tối thiểu (min increment)</li>
 * </ul>
 * 
 * <h2>Trạng Thái (Status):</h2>
 * <ul>
 *   <li><b>OPEN:</b> Phiên đấu giá đang diễn ra, cho phép đặt giá</li>
 *   <li><b>FINISHED:</b> Phiên kết thúc (hết thời gian hoặc đạt giá trần)</li>
 *   <li><b>CLOSED:</b> Phiên bị đóng (người bán hủy)</li>
 * </ul>
 * 
 * <h2>Lưu Trữ:</h2>
 * <p>Dữ liệu được lưu trong bảng <code>auctions</code> của database MySQL.</p>
 * <pre>
 * CREATE TABLE auctions (
 *   id VARCHAR(50) PRIMARY KEY,
 *   name VARCHAR(255),
 *   description TEXT,
 *   image_url VARCHAR(500),
 *   start_price DECIMAL(20,2),
 *   current_highest_bid DECIMAL(20,2),
 *   highest_bidder VARCHAR(100),
 *   start_time DATETIME,
 *   end_time DATETIME,
 *   status VARCHAR(50) DEFAULT 'OPEN',
 *   category VARCHAR(255),
 *   seller_id INT,
 *   price_ceiling DECIMAL(20,2),                    -- Giá trần (mới)
 *   min_bid_increment DECIMAL(20,2) DEFAULT 1000,  -- Bước giá tối thiểu (mới)
 *   FOREIGN KEY (seller_id) REFERENCES users(id)
 * );
 * </pre>
 * 
 * <h2>Ví Dụ Luồng Đấu Giá:</h2>
 * <pre>
 * 1. Tạo phiên đấu giá:
 *    - startPrice: 100,000,000 đ
 *    - minIncrement: 1,000,000 đ
 *    - priceCeiling: 150,000,000 đ (giá trần - tự động kết thúc)
 *    - status: OPEN
 * 
 * 2. Người A đặt giá: 105,000,000 đ
 *    - currentHighestBid = 105,000,000
 *    - highestBidder = "UserA"
 * 
 * 3. Người B (AutoBid):
 *    - maxBid: 200,000,000 (cao hơn ceiling)
 *    - increment: 5,000,000
 *    - AutoBidService tính: 105M + 5M = 110M
 * 
 * 4. Người A đặt lại: 145,000,000 đ
 *    - AutoBid trigger: 145M + 5M = 150M >= ceiling
 *    - currentHighestBid = 150,000,000 (ceiling)
 *    - status = FINISHED (tự động)
 * </pre>
 * 
 * @author BidNova Team
 * @version 2.0 (với price ceiling & min increment)
 * @see com.bidnova.services.AutoBidService
 * @see com.bidnova.handlers.PlaceBidHandler
 */
public class Auction {
    private String id;
    private String productName;
    private double startPrice;
    private double currentHighestBid;
    private String highestBidder;
    private String status;
    private String category;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int sellerId;
    private String imageUrl;
    
    // TÍNH NĂNG NÂNG CAO
    /**
     * Giá trần - khi đạt giá này, phiên tự động kết thúc.
     * Null = vô giới hạn (không có giá trần)
     */
    private Double priceCeiling;
    
    /**
     * Bước giá tối thiểu - lượng tối thiểu phải tăng khi đặt giá mới.
     * Mặc định: 1,000 (1 triệu đ).
     * Ví dụ: nếu minIncrement = 2M, không thể đặt giá từ 100M → 101M (chỉ +1M)
     */
    private double minBidIncrement = 1000;


    /**
     * Constructor - Khởi tạo phiên đấu giá mới với status mặc định là OPEN
     */
    public Auction() {
        this.status = "OPEN";
        this.minBidIncrement = 1000; // Default 1 triệu
    }

    // ==================== GETTERS & SETTERS ====================
    
    /**
     * @return ID phiên đấu giá (format: "auction_" + timestamp)
     */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    /**
     * @return Tên sản phẩm/phiên đấu giá
     */
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    /**
     * @return Giá khởi điểm (initial bid)
     */
    public double getStartPrice() { return startPrice; }
    public void setStartPrice(double startPrice) { this.startPrice = startPrice; }

    /**
     * @return Giá cao nhất hiện tại (highest bid so far)
     */
    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    /**
     * @return Tên người dùng dẫn đầu (người có giá cao nhất)
     */
    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }

    /**
     * @return Trạng thái phiên ("OPEN", "FINISHED", "CLOSED")
     */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * @return Danh mục sản phẩm (ví dụ: "Vehicle", "RealEstate", "ArtCollectible")
     */
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    /**
     * @return Mô tả chi tiết sản phẩm
     */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * @return Thời gian bắt đầu phiên
     */
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /**
     * @return Thời gian kết thúc phiên
     */
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /**
     * @return ID người bán (FK tới bảng users)
     */
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    /**
     * @return URL hình ảnh sản phẩm
     */
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // NEW GETTERS & SETTERS - Price Ceiling & Min Increment
    
    /**
     * @return Giá trần (null = vô giới hạn)
     * @see #isBidAtCeiling(double)
     */
    public Double getPriceCeiling() { 
        return priceCeiling; 
    }
    
    /**
     * Thiết lập giá trần
     * 
     * @param priceCeiling Giá tối đa (null = loại bỏ giá trần)
     */
    public void setPriceCeiling(Double priceCeiling) { 
        this.priceCeiling = priceCeiling; 
    }

    /**
     * @return Bước giá tối thiểu (mặc định: 1,000 = 1 triệu đ)
     */
    public double getMinBidIncrement() { 
        return minBidIncrement; 
    }
    
    /**
     * Thiết lập bước giá tối thiểu
     * 
     * @param minBidIncrement Lượng tối thiểu phải tăng (ví dụ: 1,000 cho 1 triệu đ)
     */
    public void setMinBidIncrement(double minBidIncrement) { 
        this.minBidIncrement = minBidIncrement; 
    }

    // ==================== HELPER METHODS ====================
    
    /**
     * isBidAtCeiling() - Kiểm tra xem giá có đạt hoặc vượt quá giá trần không
     * 
     * @param bidAmount Giá được đặt
     * @return true nếu bidAmount >= priceCeiling (phiên sẽ kết thúc),
     *         false nếu priceCeiling == null hoặc bidAmount < priceCeiling
     * 
     * <h3>Ví Dụ:</h3>
     * <pre>
     * Auction auction = new Auction();
     * auction.setPriceCeiling(150000000.0);
     * 
     * auction.isBidAtCeiling(140000000) → false
     * auction.isBidAtCeiling(150000000) → true (ĐẠT CEILING, phiên kết thúc)
     * auction.isBidAtCeiling(160000000) → true (VỀ CEILING, phiên kết thúc)
     * auction.setPriceCeiling(null);
     * auction.isBidAtCeiling(1000000000) → false (không có giá trần)
     * </pre>
     */
    public boolean isBidAtCeiling(double bidAmount) {
        if (priceCeiling == null) return false;
        return bidAmount >= priceCeiling;
    }

    /**
     * isBidIncrementValid() - Kiểm tra xem bước giá tăng có hợp lệ không
     * 
     * @param bidAmount Giá được đặt
     * @return true nếu (bidAmount - currentHighestBid) >= minBidIncrement,
     *         false nếu bước tăng quá nhỏ
     * 
     * <h3>Ví Dụ:</h3>
     * <pre>
     * Auction auction = new Auction();
     * auction.setCurrentHighestBid(100000000);      // 100M
     * auction.setMinBidIncrement(1000);              // 1M minimum
     * 
     * auction.isBidIncrementValid(100500000) → false  (chỉ +0.5M)
     * auction.isBidIncrementValid(101000000) → true   (+1M, đủ)
     * auction.isBidIncrementValid(105000000) → true   (+5M, hợp lệ)
     * </pre>
     */
    public boolean isBidIncrementValid(double bidAmount) {
        double increment = bidAmount - currentHighestBid;
        return increment >= minBidIncrement;
    }

    /**
     * placeBid() - Đặt giá (thread-safe)
     * 
     * <p>DEPRECATED: Sử dụng PlaceBidHandler.handle() thay vì method này.</p>
     * 
     * @param username Tên người dùng đặt giá
     * @param bidAmount Giá được đặt
     * @return true nếu đặt giá thành công, false nếu phiên không OPEN hoặc giá không hợp lệ
     */
    public synchronized boolean placeBid(String username, double bidAmount) {
        if (!"OPEN".equals(status)) return false; 
        
        if (bidAmount > currentHighestBid) { 
            currentHighestBid = bidAmount;
            highestBidder = username;
            return true;
        }
        return false;
    }
    
}