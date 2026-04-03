package com.daugia.models;

public class Auction {
    private int auctionId;
    private Item item;
    private double currentHighestBid;
    private String highestBidder;
    private String status;

    public Auction(int auctionId, Item item) {
        this.auctionId = auctionId;
        this.item = item;
        this.currentHighestBid = item.getStartingPrice();
        this.highestBidder = null;
        this.status = "RUNNING";
    }

    public int getAuctionId() { return auctionId; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public String getHighestBidder() { return highestBidder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public synchronized boolean placeBid(String username, double bidAmount) {
        if (!status.equals("RUNNING")) {
            return false; 
        }
        if (bidAmount > currentHighestBid) { 
            currentHighestBid = bidAmount;
            highestBidder = username;
            return true;
        }
        return false;
    }
}