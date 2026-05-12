package com.daugia.models;

public class BidMessage {
    private String type;       // luôn là "NEW_BID"
    private int    auctionId;  // int, khớp Auction.auctionId
    private String username;   // người đặt giá
    private double amount;     // giá vừa đặt
    private String timestamp;  // "HH:mm:ss" — dùng thẳng làm nhãn trục X chart

    public BidMessage(String type, int auctionId,
                      String username, double amount, String timestamp) {
        this.type      = type;
        this.auctionId = auctionId;
        this.username  = username;
        this.amount    = amount;
        this.timestamp = timestamp;
    }

    public String getType()      { return type; }
    public int    getAuctionId() { return auctionId; }
    public String getUsername()  { return username; }
    public double getAmount()    { return amount; }
    public String getTimestamp() { return timestamp; }
}