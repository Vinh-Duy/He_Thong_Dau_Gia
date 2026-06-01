package com.daugia.patterns.observer;

import com.daugia.models.Auction;

/**
 * Observer Pattern Interface - Observer cho Auction updates
 * 
 * @author Design Patterns Team
 */
public interface AuctionObserver {
    
    /**
     * Được gọi khi có bid mới được đặt
     * @param auction Phiên đấu giá được cập nhật
     * @param newBid Giá mới
     * @param bidder Tên người đặt
     */
    void onBidPlaced(Auction auction, double newBid, String bidder);
    
    /**
     * Được gọi khi trạng thái phiên đấu giá thay đổi
     * @param auction Phiên đấu giá
     * @param oldStatus Trạng thái cũ
     * @param newStatus Trạng thái mới
     */
    void onAuctionStatusChanged(Auction auction, String oldStatus, String newStatus);
    
    /**
     * Được gọi khi auto-bid được trigger
     * @param auction Phiên đấu giá
     * @param autoBidAmount Số tiền auto-bid
     * @param username Người dùng auto-bid
     */
    void onAutoBidTriggered(Auction auction, double autoBidAmount, String username);
}
