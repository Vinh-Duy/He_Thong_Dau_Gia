package com.daugia.services;

import java.util.concurrent.ConcurrentHashMap;

import com.daugia.models.Auction;

public class AuctionManager {
    private static AuctionManager instance;
    
    private ConcurrentHashMap<Integer, Auction> activeAuctions;

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getAuctionId(), auction);
    }

    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }
}