package com.bidnova.models;

import java.time.LocalDateTime;

public class BidHistory {
    private int id;
    private String auctionId;
    private int userId;
    private String username;
    private double bidAmount;
    private LocalDateTime bidTime;

    // Getters
    public int getId() { return id; }
    public String getAuctionId() { return auctionId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getBidTime() { return bidTime; }

    // Setters (Nếu cần cho GSON)
    public void setId(int id) { this.id = id; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
}