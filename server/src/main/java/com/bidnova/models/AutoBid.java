package com.bidnova.models;

import java.time.LocalDateTime;

public class AutoBid {
    private int id;
    private String auctionId;
    private int userId;
    private double maxBid;
    private double increment;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    public AutoBid() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }
    
    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
