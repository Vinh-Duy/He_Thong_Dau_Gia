package com.bidnova.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;

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

    // Remove auction khỏi active auctions (khi kết thúc)
    public void removeAuction(String id) {
        activeAuctions.remove(id);
    }

    // Update auction status trong memory
    public void updateAuctionStatus(String id, String status) {
        Auction auction = activeAuctions.get(id);
        if (auction != null) {
            auction.setStatus(status);
        }
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
     * Kiểm tra và cập nhật trạng thái auction:
     * - Nếu endTime đã qua và chưa FINISHED/CLOSED → set FINISHED
     * - Nếu endTime chưa qua và đang FINISHED/CLOSED → set OPEN (reopen)
     */
    public void checkAndUpdateExpiredStatus(Auction auction) {
        if (auction == null) return;
        if (auction.getEndTime() == null) return;
        
        String currentStatus = auction.getStatus();
        boolean isExpired = LocalDateTime.now().isAfter(auction.getEndTime());
        
        if (isExpired) {
            // Hết hạn nhưng chưa đánh dấu → set FINISHED
            if (!"FINISHED".equals(currentStatus) && !"CLOSED".equals(currentStatus)) {
                auction.setStatus("FINISHED");
                AuctionDAO dao = new AuctionDAO();
                dao.updateAuction(auction);
            }
        } else {
            // Chưa hết hạn nhưng đang FINISHED/CLOSED → reopen về OPEN
            if ("FINISHED".equals(currentStatus) || "CLOSED".equals(currentStatus)) {
                auction.setStatus("OPEN");
                AuctionDAO dao = new AuctionDAO();
                dao.updateAuction(auction);
            }
        }
    }

    public List<Auction> scanExpiredAuctions() {
        List<Auction> expiredAuctions = new ArrayList<>();
        AuctionDAO dao = new AuctionDAO();

        for (Auction auction : activeAuctions.values()) {
            if (auction == null || auction.getEndTime() == null) continue;

            boolean isExpired = LocalDateTime.now().isAfter(auction.getEndTime());
            if (isExpired && !"FINISHED".equalsIgnoreCase(auction.getStatus()) && !"CLOSED".equalsIgnoreCase(auction.getStatus())) {
                auction.setStatus("FINISHED");
                dao.updateStatus(auction.getId(), "FINISHED");
                expiredAuctions.add(auction);
                removeAuction(auction.getId());
            }
        }

        return expiredAuctions;
    }
}