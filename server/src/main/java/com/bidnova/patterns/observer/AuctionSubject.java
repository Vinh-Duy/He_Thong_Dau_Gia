package com.bidnova.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bidnova.models.Auction;

/**
 * AuctionSubject - Subject trong Observer Pattern
 * 
 * <h2>Chức Năng:</h2>
 * <p>Quản lý danh sách observers và thực hiện notify tất cả observers khi có sự kiện
 * xảy ra trong phiên đấu giá (bid, status change, auto-bid trigger).</p>
 * 
 * <h2>Kiến Trúc:</h2>
 * <pre>
 * ┌──────────────────────────────────────────────┐
 * │  Auction (Subject)                           │
 * │  ├─ id, name, price, status                  │
 * │  ├─ AuctionSubject                           │
 * │  │  ├─ List<AuctionObserver> observers       │
 * │  │  ├─ attach(observer)                      │
 * │  │  ├─ detach(observer)                      │
 * │  │  ├─ notifyBidPlaced()                     │
 * │  │  ├─ notifyStatusChanged()                 │
 * │  │  └─ notifyAutoBidTriggered()              │
 * │  └─ Observer event triggering                │
 * └──────────────────────────────────────────────┘
 * </pre>
 * 
 * <h2>Thread-Safety:</h2>
 * <p>Sử dụng <code>CopyOnWriteArrayList</code> để đảm bảo thread-safe khi:</p>
 * <ul>
 *   <li>Nhiều threads attach/detach observers</li>
 *   <li>Notify xảy ra đồng thời</li>
 * </ul>
 * 
 * <h2>Ví Dụ Sử Dụng:</h2>
 * <pre>
 * // 1. Tạo AuctionSubject cho một phiên
 * Auction auction = new Auction();
 * auction.setId("auction123");
 * AuctionSubject subject = new AuctionSubject(auction);
 * 
 * // 2. Observers đăng ký
 * AuctionObserver clientObserver = new ClientObserver();
 * AuctionObserver loggingObserver = new LoggingObserver();
 * 
 * subject.attach(clientObserver);
 * subject.attach(loggingObserver);
 * 
 * // 3. Khi có sự kiện
 * // Bid mới
 * subject.notifyBidPlaced(105000000, "UserA");
 * 
 * // Status thay đổi
 * subject.notifyStatusChanged("OPEN", "FINISHED");
 * 
 * // Auto-bid trigger
 * subject.notifyAutoBidTriggered(110000000, "UserB");
 * </pre>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see AuctionObserver
 * @see Auction
 * @see com.bidnova.handlers.PlaceBidHandler
 */
public class AuctionSubject {
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();
    private final Auction auction;
    
    /**
     * Constructor - Tạo Subject cho một phiên đấu giá
     * 
     * @param auction Phiên đấu giá cần monitoring
     */
    public AuctionSubject(Auction auction) {
        this.auction = auction;
    }
    
    /**
     * attach() - Đăng ký observer để theo dõi phiên
     * 
     * <p>Observer đăng ký sẽ được thông báo mọi sự kiện của phiên này.</p>
     * 
     * @param observer Observer cần đăng ký (không được null)
     * 
     * <h3>Ví Dụ:</h3>
     * <pre>
     * AuctionObserver observer = new ClientObserver();
     * subject.attach(observer);  // observer sẽ nhận update
     * </pre>
     */
    public void attach(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * detach() - Hủy đăng ký observer
     * 
     * <p>Observer sẽ không còn nhận update từ phiên này.</p>
     * 
     * @param observer Observer cần hủy đăng ký
     */
    public void detach(AuctionObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * notifyBidPlaced() - Thông báo tất cả observers có bid mới
     * 
     * <h3>Kích hoạt Từ:</h3>
     * <ul>
     *   <li>PlaceBidHandler.handle() - Sau khi user đặt giá thành công</li>
     *   <li>AutoBidService.placeAutoBidOnAuction() - Sau khi auto-bid placement</li>
     * </ul>
     * 
     * <h3>Thông báo Bao Gồm:</h3>
     * <ul>
     *   <li>Giá mới được đặt</li>
     *   <li>Tên người dặt giá</li>
     *   <li>Chi tiết phiên đấu giá</li>
     * </ul>
     * 
     * @param newBid Giá mới được đặt
     * @param bidder Tên người dùng đặt giá
     * 
     * <h3>Exception Handling:</h3>
     * <p>Nếu observer throw exception, log lỗi nhưng tiếp tục notify các observer khác.</p>
     */
    public void notifyBidPlaced(double newBid, String bidder) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onBidPlaced(auction, newBid, bidder);
            } catch (Exception e) {
                System.out.println("[WARN] Error notifying observer about bid: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * notifyStatusChanged() - Thông báo trạng thái phiên đã thay đổi
     * 
     * <h3>Kích hoạt Từ:</h3>
     * <ul>
     *   <li>Hết thời gian đấu giá → OPEN → FINISHED</li>
     *   <li>Đạt giá trần → OPEN → FINISHED</li>
     *   <li>Người bán hủy phiên → OPEN → CLOSED</li>
     * </ul>
     * 
     * @param oldStatus Trạng thái cũ (ví dụ: "OPEN")
     * @param newStatus Trạng thái mới (ví dụ: "FINISHED")
     */
    public void notifyStatusChanged(String oldStatus, String newStatus) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onAuctionStatusChanged(auction, oldStatus, newStatus);
            } catch (Exception e) {
                System.out.println("[WARN] Error notifying observer about status change: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * notifyAutoBidTriggered() - Thông báo auto-bid đã được trigger
     * 
     * <h3>Kích hoạt Từ:</h3>
     * <p>AutoBidService.executeAutoBids() - Khi auto-bid tự động đặt giá.</p>
     * 
     * @param autoBidAmount Giá mà auto-bid đã đặt
     * @param username      Tên người dùng có auto-bid
     */
    public void notifyAutoBidTriggered(double autoBidAmount, String username) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onAutoBidTriggered(auction, autoBidAmount, username);
            } catch (Exception e) {
                System.out.println("[WARN] Error notifying observer about auto-bid: " + e.getMessage());
                e.printStackTrace();
                System.err.println("Error notifying observer about auto-bid: " + e.getMessage());
            }
        }
    }
    
    /**
     * getObserverCount() - Lấy số lượng observers hiện tại
     * 
     * @return Số observers đã đăng ký
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * clearObservers() - Xóa tất cả observers
     * 
     * <p>Hữu ích khi phiên đấu giá kết thúc để giải phóng resources.</p>
     */
    public void clearObservers() {
        observers.clear();
    }
}
