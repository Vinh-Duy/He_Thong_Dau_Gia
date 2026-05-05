package com.daugia.models;

public class Auction {
    private String id;       // Đổi sang String để dùng ID kiểu "A001"
    private String name;     // Gộp luôn tên sản phẩm vào đây cho nhẹ
    private double startingPrice;
    private double currentHighestBid;
    private String highestBidder;
    private String status;
    // 1. CÁC BIẾN (Tương ứng với các cột trong Database)
    private String productName; // Tên sản phẩm
    private double startPrice;  // Giá khởi điểm
    private String category;
    private String description;
    private String endTime;
    private int sellerId; // ID của người bán (seller)

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public int getSellerId() {
        return sellerId;
    }
    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getCategory() { 
        return category; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }
    // Hàm lấy tên sản phẩm ra
    public String getProductName() {
        return productName;
    }

    // Hàm nhét tên sản phẩm vào thùng
    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Hàm lấy giá khởi điểm ra
    public double getStartPrice() {
        return startPrice;
    }

    // Hàm nhét giá vào thùng
    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    // 1. Hàm khởi tạo rỗng (Bắt buộc phải có để DAO khởi tạo)
    public Auction() {
        this.status = "OPEN";
    }

    // 2. Các hàm Getter / Setter (Bắt buộc phải có để gán dữ liệu)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Hàm đặt giá
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