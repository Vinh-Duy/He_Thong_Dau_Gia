package com.bidnova.patterns.observer;

import com.bidnova.models.Auction;

/**
 * AuctionObserver - Observer Pattern Interface
 * 
 * <h2>Chức Năng:</h2>
 * <p>Định nghĩa hợp đồng cho tất cả observers muốn theo dõi sự thay đổi của một phiên đấu giá.</p>
 * <p>Khi có sự kiện xảy ra (bid mới, status thay đổi, auto-bid trigger), AuctionSubject sẽ
 * gọi các method tương ứng của tất cả observers đã đăng ký.</p>
 * 
 * <h2>Observer Pattern Benefit:</h2>
 * <ul>
 *   <li><b>Real-time Updates:</b> Các observers được thông báo ngay lập tức khi có thay đổi</li>
 *   <li><b>Broadcast Events:</b> Một sự kiện có thể thông báo cho nhiều observers</li>
 *   <li><b>Decoupling:</b> AuctionSubject không cần biết chi tiết observers là gì</li>
 *   <li> <b>Easy to Extend:</b> Dễ thêm observer mới mà không sửa code AuctionSubject</li>
 * </ul>
 * 
 * <h2>Sử Dụng Trong Hệ Thống:</h2>
 * <pre>
 * Mỗi phiên đấu giá (Auction) có một AuctionSubject:
 * 
 * Client 1 (Bidder A)  ──┐
 * Client 2 (Bidder B)  ──┼─→ AuctionSubject("auction123")
 * Client 3 (Observer)  ──┘
 *                          │
 *                          ├─ attach(observer1)
 *                          ├─ attach(observer2)
 *                          └─ attach(observer3)
 * 
 * Khi Bidder A đặt giá 105M:
 * PlaceBidHandler gọi:
 *   auctionSubject.notifyBidPlaced(105M, "BidderA")
 * 
 * AuctionSubject gọi:
 *   observer1.onBidPlaced(auction, 105M, "BidderA") ← Broadcast tới Client 1
 *   observer2.onBidPlaced(auction, 105M, "BidderA") ← Broadcast tới Client 2
 *   observer3.onBidPlaced(auction, 105M, "BidderA") ← Broadcast tới Client 3
 * </pre>
 * 
 * <h2>Event Types:</h2>
 * <table border="1">
 *   <tr><th>Event</th><th>When Triggered</th><th>Handler Method</th></tr>
 *   <tr><td>Bid Placed</td><td>Khi có bid mới (manual hoặc auto)</td><td>onBidPlaced()</td></tr>
 *   <tr><td>Status Changed</td><td>Khi status thay đổi (OPEN → FINISHED)</td><td>onAuctionStatusChanged()</td></tr>
 *   <tr><td>AutoBid Triggered</td><td>Khi AutoBid tự động đặt giá</td><td>onAutoBidTriggered()</td></tr>
 * </table>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see AuctionSubject
 * @see Auction
 * @see com.bidnova.handlers.PlaceBidHandler
 */
public interface AuctionObserver {
    
    /**
     * onBidPlaced() - Được gọi khi có bid mới được đặt
     * 
     * <h3>Kích hoạt Khi:</h3>
     * <ul>
     *   <li>User đặt giá thủ công thành công</li>
     *   <li>AutoBid tự động đặt giá</li>
     * </ul>
     * 
     * <h3>Ứng Dụng:</h3>
     * <p>Client cần cập nhật UI để hiển thị:</p>
     * <ul>
     *   <li>Giá cao nhất hiện tại</li>
     *   <li>Tên người dẫn đầu</li>
     *   <li>Thời gian cập nhật cuối cùng</li>
     *   <li>Lịch sử bid</li>
     * </ul>
     * 
     * @param auction  Phiên đấu giá được cập nhật
     * @param newBid   Giá mới được đặt
     * @param bidder   Tên người dùng đặt giá
     * 
     * @example
     * <pre>
     * // Ví dụ implementation
     * @Override
     * public void onBidPlaced(Auction auction, double newBid, String bidder) {
     *     System.out.println(bidder + " đặt giá " + newBid + " cho " + auction.getProductName());
     *     
     *     // Client cập nhật UI
     *     updateAuctionUI(auction, newBid, bidder);
     *     broadcastToAllClients(auction);
     * }
     * </pre>
     */
    void onBidPlaced(Auction auction, double newBid, String bidder);
    
    /**
     * onAuctionStatusChanged() - Được gọi khi trạng thái phiên thay đổi
     * 
     * <h3>Trạng Thái Transitions:</h3>
     * <ul>
     *   <li>OPEN → FINISHED (hết thời gian hoặc đạt ceiling)</li>
     *   <li>OPEN → CLOSED (người bán hủy)</li>
     * </ul>
     * 
     * <h3>Ứng Dụng:</h3>
     * <p>Thông báo tất cả clients:</p>
     * <ul>
     *   <li>Phiên đã kết thúc, không thể đặt giá nữa</li>
     *   <li>Hiển thị người chiến thắng</li>
     *   <li>Cập nhật danh sách phiên đấu giá</li>
     * </ul>
     * 
     * @param auction    Phiên đấu giá
     * @param oldStatus  Trạng thái cũ (ví dụ: "OPEN")
     * @param newStatus  Trạng thái mới (ví dụ: "FINISHED")
     */
    void onAuctionStatusChanged(Auction auction, String oldStatus, String newStatus);
    
    /**
     * onAutoBidTriggered() - Được gọi khi auto-bid được trigger
     * 
     * <h3>Kích hoạt Khi:</h3>
     * <p>AutoBidService tự động đặt giá cho người dùng khi có bid mới.</p>
     * 
     * <h3>Ứng Dụng:</h3>
     * <p>Thông báo cho tất cả clients về auto-bid placement (có thể lấy quyền ưu tiên):</p>
     * <ul>
     *   <li>Cập nhật giá cao nhất</li>
     *   <li>Thông báo ai dẫn đầu hiện tại</li>
     *   <li>Log auto-bid events cho audit</li>
     * </ul>
     * 
     * @param auction        Phiên đấu giá
     * @param autoBidAmount  Giá mà auto-bid đã đặt
     * @param username       Tên người dùng có auto-bid
     */
    void onAutoBidTriggered(Auction auction, double autoBidAmount, String username);
}
