package com.bidnova.models;

// Tên field PHẢI GIỐNG HỆT server BidMessage — Gson dùng tên field để map JSON
public class BidMessage {
    private String type;
    private int    auctionId;
    private String username;
    private double amount;
    private String timestamp;

    public BidMessage() {} // Gson cần constructor rỗng

    public String getType()      { return type; }
    public int    getAuctionId() { return auctionId; }
    public String getUsername()  { return username; }
    public double getAmount()    { return amount; }
    public String getTimestamp() { return timestamp; }
}