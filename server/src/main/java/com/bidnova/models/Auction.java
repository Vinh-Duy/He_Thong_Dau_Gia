package com.bidnova.models;

import java.time.LocalDateTime;

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
    
    // ⭐️ NEW FIELDS
    private Double priceCeiling;          // Giá trần - null = vô giới hạn
    private double minBidIncrement = 1000; // Bước giá tối thiếu


    // Constructor
    public Auction() {
        this.status = "OPEN";
        this.minBidIncrement = 1000; // Default 1 triệu
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

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // ⭐️ NEW GETTERS & SETTERS
    public Double getPriceCeiling() { 
        return priceCeiling; 
    }
    
    public void setPriceCeiling(Double priceCeiling) { 
        this.priceCeiling = priceCeiling; 
    }

    public double getMinBidIncrement() { 
        return minBidIncrement; 
    }
    
    public void setMinBidIncrement(double minBidIncrement) { 
        this.minBidIncrement = minBidIncrement; 
    }

    // Helper method: Check if bid reaches ceiling
    public boolean isBidAtCeiling(double bidAmount) {
        if (priceCeiling == null) return false;
        return bidAmount >= priceCeiling;
    }

    // Helper method: Validate if bid respects min increment
    public boolean isBidIncrementValid(double bidAmount) {
        double increment = bidAmount - currentHighestBid;
        return increment >= minBidIncrement;
    }

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