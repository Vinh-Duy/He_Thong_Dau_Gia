package com.daugia.models;

public class Auction {
    private String id;
    private String productName;  // Chỉ giữ lại productName
    private double startPrice;  // Chỉ giữ lại startPrice
    private double currentHighestBid;
    private String highestBidder;
    private String status;
    private String category;
    private String description;
    private String endTime;
    private int sellerId;


    // Constructor
    public Auction() {
        this.status = "OPEN";
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getStartPrice() { return startPrice; }
    public void setStartPrice(double startPrice) { this.startPrice = startPrice; }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

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