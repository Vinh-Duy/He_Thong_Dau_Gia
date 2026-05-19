package com.bidnova.models;

public class AutoBid {
    private int id;
    private String auctionId;
    private String username;
    private double maxBid;
    private double increment;
    private boolean isActive;
    private long createdAt;
    
    public AutoBid() {
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }
    
    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
