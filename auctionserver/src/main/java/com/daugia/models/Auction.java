package com.daugia.models;

public class Auction {
    private String id;       // Đổi sang String để dùng ID kiểu "A001"
    private String name;     // Gộp luôn tên sản phẩm vào đây cho nhẹ
    private double startingPrice;
    private double currentHighestBid;
    private String highestBidder;
    private String status;

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