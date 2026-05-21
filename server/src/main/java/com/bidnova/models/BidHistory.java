package com.bidnova.models;

import java.time.LocalDateTime;

/**
 * Model đại diện cho một bản ghi lịch sử đấu giá.
 */
public class BidHistory {
    private int id;
    private String auctionId;
    private int userId;
    private String username;
    private double bidAmount;
    private LocalDateTime bidTime;

    public BidHistory(String auctionId, int userId, String username, double bidAmount, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.username = username;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    // Constructor đầy đủ (thường dùng khi đọc từ DB)
    public BidHistory(int id, String auctionId, int userId, String username, double bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.username = username;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public int getUserId() {
        return userId;
    }

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