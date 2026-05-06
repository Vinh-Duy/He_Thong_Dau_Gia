package com.daugia.patterns.observer;

import com.daugia.models.Auction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observable Pattern - Subject cho Auction updates
 * Quản lý danh sách observers và notify khi có thay đổi
 * 
 * @author Design Patterns Team
 */
public class AuctionSubject {
    
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();
    private final Auction auction;
    
    public AuctionSubject(Auction auction) {
        this.auction = auction;
    }
    
    /**
     * Đăng ký observer mới
     */
    public void attach(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Hủy đăng ký observer
     */
    public void detach(AuctionObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notify tất cả observers khi có bid mới
     */
    public void notifyBidPlaced(double newBid, String bidder) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onBidPlaced(auction, newBid, bidder);
            } catch (Exception e) {
                System.err.println("Error notifying observer about bid: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify tất cả observers khi trạng thái thay đổi
     */
    public void notifyStatusChanged(String oldStatus, String newStatus) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onAuctionStatusChanged(auction, oldStatus, newStatus);
            } catch (Exception e) {
                System.err.println("Error notifying observer about status change: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify tất cả observers khi auto-bid được trigger
     */
    public void notifyAutoBidTriggered(double autoBidAmount, String username) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onAutoBidTriggered(auction, autoBidAmount, username);
            } catch (Exception e) {
                System.err.println("Error notifying observer about auto-bid: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lấy số lượng observers hiện tại
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Clear tất cả observers
     */
    public void clearObservers() {
        observers.clear();
    }
}
