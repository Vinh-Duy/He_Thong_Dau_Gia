package com.bidnova.services;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import com.bidnova.models.Auction;
import com.bidnova.dao.AuctionDAO;

public class AuctionManager {
    private static AuctionManager instance;
    
    // 1. Đổi Integer thành String để nhận ID kiểu "A001"
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
        // Kiểm tra và cập nhật trạng thái hết hạn cho tất cả
        for (Auction auction : activeAuctions.values()) {
            checkAndUpdateExpiredStatus(auction);
        }
        return activeAuctions.values();
    }
    
    /**
     * Kiểm tra nếu auction đã hết hạn (endTime đã qua) thì set status = "FINISHED"
     */
    public void checkAndUpdateExpiredStatus(Auction auction) {
        if (auction == null) return;
        if (auction.getEndTime() == null) return;
        
        String currentStatus = auction.getStatus();
        if ("FINISHED".equals(currentStatus) || "CLOSED".equals(currentStatus)) return;
        
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            auction.setStatus("FINISHED");
            AuctionDAO dao = new AuctionDAO();
            dao.updateAuction(auction);
        }
    }
}
