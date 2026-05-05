package com.daugia.services;

import java.util.concurrent.ConcurrentHashMap;

import com.daugia.models.Auction;

public class AuctionManager {
    private static AuctionManager instance;
    
    private ConcurrentHashMap<String, Auction> activeAuctions;

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
        // 2. Sử dụng getId() trả về String
        activeAuctions.put(auction.getId(), auction);
    }

    // 3. Tham số truyền vào phải là String
    public Auction getAuction(String id) {
        return activeAuctions.get(id);
    }
    // Thêm hàm này vào để lấy toàn bộ danh sách món hàng đang có trong bộ nhớ
    public java.util.Collection<Auction> getAllAuctions() {
        return activeAuctions.values();
    }
}