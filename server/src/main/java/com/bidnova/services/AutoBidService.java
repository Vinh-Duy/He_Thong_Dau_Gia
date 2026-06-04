package com.bidnova.services;

import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.dao.BidHistoryDAO;
import com.bidnova.dao.UserDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AutoBid;
import com.bidnova.models.BidHistory;
import com.bidnova.models.User;

/**
 * AutoBidService - Xử lý logic tự động đặt giá
 * 
 * <h2>Chức Năng:</h2>
 * <p>Quản lý các quy tắc tự động đặt giá (AutoBid):</p>
 * <ul>
 *   <li>Kích hoạt AutoBid khi giá được đặt</li>
 *   <li>Tính toán giá tiếp theo: current + increment</li>
 *   <li>Kiểm tra constraints (maxBid, minIncrement, priceCeiling)</li>
 *   <li>Tự động đặt giá cho người dùng</li>
 *   <li>Ghi lịch sử AutoBid đặt giá</li>
 *   <li>Vô hiệu hóa AutoBid khi hết hạn</li>
 * </ul>
 * 
 * <h2>Quy Trình Hoạt Động:</h2>
 * <pre>
 * PlaceBidHandler.handle()
 *   │
 *   ├─ Cập nhật currentHighestBid = 105M
 *   │
 *   └─ autoBidService.executeAutoBids("auction123", 105)
 *        │
 *        ├─ Lấy danh sách AutoBid hoạt động
 *        │
 *        └─ Cho mỗi AutoBid (theo thứ tự FIFO):
 *             │
 *             ├─ Kiểm tra valid? (không phải người hiện tại, isActive=true)
 *             │
 *             ├─ Tính: nextBid = 105M + increment
 *             │
 *             ├─ Điều chỉnh nếu < minIncrement
 *             │
 *             ├─ Kiểm tra nextBid <= maxBid?
 *             │
 *             ├─ Kiểm tra nextBid >= priceCeiling?
 *             │
 *             ├─ Nếu >= ceiling:
 *             │   ├─ Đặt giá = ceiling
 *             │   ├─ Đóng phiên (status = FINISHED)
 *             │   └─ Vô hiệu hóa AutoBid
 *             │
 *             └─ Nếu < ceiling:
 *                 ├─ Đặt giá = nextBid
 *                 └─ Giữ AutoBid hoạt động
 * </pre>
 * 
 * <h2>Ví Dụ Chi Tiết:</h2>
 * <pre>
 * ==================== AutoBid Execution Example ====================
 * 
 * Setup:
 * - Phiên: "Toyota Camry"
 * - Current highest: 100M (User A)
 * - Min increment: 2M
 * - Price ceiling: 150M
 * 
 * User B's AutoBid:
 * - maxBid: 180M (cao hơn ceiling)
 * - increment: 5M
 * - isActive: true
 * 
 * Execution Flow:
 * 
 * 1️⃣ User C bids 105M
 *    → executeAutoBids("auction123", 105)
 *    → nextBid = 105 + 5 = 110M
 *    → 110M < 150M ceiling ✓
 *    → Place bid 110M for User B
 *    → AutoBid stays active
 * 
 * 2️⃣ User A bids 115M
 *    → executeAutoBids("auction123", 115)
 *    → nextBid = 115 + 5 = 120M
 *    → 120M < 150M ceiling ✓
 *    → Place bid 120M for User B
 *    → AutoBid stays active
 * 
 * 3️⃣ User C bids 140M
 *    → executeAutoBids("auction123", 140)
 *    → nextBid = 140 + 5 = 145M
 *    → 145M < 150M ceiling ✓
 *    → Place bid 145M for User B
 *    → AutoBid stays active
 * 
 * 4️⃣ User A bids 148M
 *    → executeAutoBids("auction123", 148)
 *    → nextBid = 148 + 5 = 153M
 *    → 153M >= 150M ceiling!
 *    → Set nextBid = 150M (ceiling)
 *    → Place bid 150M for User B
 *    → Mark auction as FINISHED
 *    → Deactivate AutoBid
 *    → Result: User B wins at 150M
 * </pre>
 * 
 * <h2>Tính Năng Nâng Cao:</h2>
 * <ul>
 *   <li> <b>FIFO Priority:</b> Sắp xếp AutoBid theo thời gian tạo (FIFO)</li>
 *   <li> <b>Min Increment Adjustment:</b> Tự động điều chỉnh nếu < minIncrement</li>
 *   <li> <b>Price Ceiling Handling:</b> Tự động kết thúc phiên khi đạt trần</li>
 *   <li> <b>User Validation:</b> Kiểm tra người dùng còn tồn tại không</li>
 *   <li> <b>Bid History Recording:</b> Ghi lại mọi AutoBid placement</li>
 *   <li> <b>Multiple AutoBids:</b> Hỗ trợ nhiều AutoBid trên cùng phiên</li>
 * </ul>
 * 
 * @author BidNova Team
 * @version 2.0 (with min increment & price ceiling)
 * @see PlaceBidHandler
 * @see AutoBid
 * @see Auction
 */
public class AutoBidService {
    private final AutoBidDAO autoBidDAO;
    private final AuctionDAO auctionDAO;
    private final BidHistoryDAO bidHistoryDAO;
    private final UserDAO userDAO;
    private final AuctionManager auctionManager;

    /**
     * Constructor cho dependency injection (hữu ích để testing)
     * 
     * @param autoBidDAO     DAO để quản lý AutoBid records
     * @param auctionDAO     DAO để quản lý Auction records
     * @param bidHistoryDAO  DAO để ghi lịch sử bids
     * @param userDAO        DAO để lấy thông tin user
     * @param auctionManager Manager để quản lý auctions in-memory
     */
    public AutoBidService(AutoBidDAO autoBidDAO, AuctionDAO auctionDAO,
                         BidHistoryDAO bidHistoryDAO, UserDAO userDAO,
                         AuctionManager auctionManager) {
        this.autoBidDAO = autoBidDAO;
        this.auctionDAO = auctionDAO;
        this.bidHistoryDAO = bidHistoryDAO;
        this.userDAO = userDAO;
        this.auctionManager = auctionManager;
    }

    /**
     * Constructor mặc định - Sử dụng DAO instances mặc định (production)
     */
    public AutoBidService() {
        this.autoBidDAO = new AutoBidDAO();
        this.auctionDAO = new AuctionDAO();
        this.bidHistoryDAO = new BidHistoryDAO();
        this.userDAO = new UserDAO();
        this.auctionManager = AuctionManager.getInstance();
    }

    /**
     * executeAutoBids() - Kích hoạt tất cả AutoBids để đấu chứng với bid mới
     * 
     * <h3>Quy Trình:</h3>
     * <ol>
     *   <li>Lấy danh sách AutoBids hoạt động cho phiên</li>
     *   <li>Lấy Auction object từ AuctionManager</li>
     *   <li>Cho mỗi AutoBid (theo thứ tự FIFO):
     *     <ul>
     *       <li>Kiểm tra người dùng còn tồn tại</li>
     *       <li>Kiểm tra tính hợp lệ (người khác + isActive)</li>
     *       <li>Tính nextBidAmount = currentHighestBid + increment</li>
     *       <li>Điều chỉnh nếu < minBidIncrement</li>
     *       <li>Kiểm tra nextBidAmount <= maxBid?
     *         <ul>
     *           <li>Nếu < ceiling: đặt giá bình thường</li>
     *           <li>Nếu >= ceiling: đặt tại ceiling + đóng phiên</li>
     *         </ul>
     *       </li>
     *       <li>Ngược lại: vô hiệu hóa AutoBid</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param auctionId         ID phiên đấu giá
     * @param currentHighestBid Giá cao nhất hiện tại (mới được đặt)
     * 
     * <h3>Exception Handling:</h3>
     * <p>Mọi exception bị catch và in log, không làm crash server.</p>
     * 
     * @see Auction#isBidAtCeiling(double)
     * @see AutoBid#getMaxBid()
     * @see AutoBid#getIncrement()
     */
    public void executeAutoBids(String auctionId, double currentHighestBid) {
        try {
            List<AutoBid> autoBids = autoBidDAO.getActiveAutoBids(auctionId);
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) return;
            
            // Sort by creation time (FIFO) - first person to set auto-bid gets priority
            for (AutoBid autoBid : autoBids) {
                User user = userDAO.findById(autoBid.getUserId());
                if (user == null) continue; // User no longer exists
                String username = user.getUsername();
                
                String currentLeader = auction.getHighestBidder();
                if (!isAutoBidValid(autoBid, currentHighestBid, currentLeader, user)) {
                    continue;
                }

                // Calculate next bid amount
                double nextBidAmount = currentHighestBid + autoBid.getIncrement();

                // NEW: Validate minimum bid increment
                double minimumRequiredIncrement = auction.getMinBidIncrement();
                if ((nextBidAmount - currentHighestBid) < minimumRequiredIncrement) {
                    // Adjust to minimum increment
                    nextBidAmount = currentHighestBid + minimumRequiredIncrement;
                    System.out.println("📊 Adjusted bid from " + 
                        (currentHighestBid + autoBid.getIncrement()) + 
                        " to " + nextBidAmount + " (min increment: " + minimumRequiredIncrement + ")");
                }

                // Check if next bid is within max limit
                if (nextBidAmount <= autoBid.getMaxBid()) {
                    
                    // NEW: Check if bid reaches price ceiling
                    if (auction.getPriceCeiling() != null && nextBidAmount >= auction.getPriceCeiling()) {
                        // Bid reaches ceiling - place bid and close auction
                        nextBidAmount = auction.getPriceCeiling();
                        placeAutoBidOnAuction(auctionId, username, nextBidAmount);
                        
                        // Close auction
                        auction.setStatus("FINISHED");
                        auctionDAO.updateStatus(auctionId, "FINISHED");
                        autoBidDAO.deactivateAutoBid(autoBid.getId());
                        
                        System.out.println("Auto-bid at ceiling: " + username + 
                            " bid " + nextBidAmount + " - Auction finished!");
                    } else {
                        // Normal auto-bid
                        placeAutoBidOnAuction(auctionId, username, nextBidAmount);
                        System.out.println("✓ Auto-bid placed: " + username + " bid " + nextBidAmount);
                    }
                    
                    // Update current highest bid for next auto-bid check
                    currentHighestBid = nextBidAmount;
                } else {
                    // Max bid reached - deactivate this auto-bid
                    autoBidDAO.deactivateAutoBid(autoBid.getId());
                    System.out.println("⊘ Auto-bid deactivated: " + username + " (max bid exceeded)");
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing auto-bids: " + e.getMessage());
        }
    }

    /**
     * Validate if auto-bid should be executed
     */
    private boolean isAutoBidValid(AutoBid autoBid, double currentBid, String currentLeader, User user) {
        // Auto-bid must be active
        if (!autoBid.isActive()) {
            return false;
        }

        // Auto-bid max must be higher than current bid
        if (autoBid.getMaxBid() <= currentBid) {
            return false;
        }

        // Không tự đấu giá đè lên chính mình
        if (user.getUsername().equals(currentLeader)) {
            return false;
        }

        return true;
    }

    /**
     * Place an automatic bid on the auction
     */
    private synchronized void placeAutoBidOnAuction(String auctionId, String username, double bidAmount) {
        try {
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) {
                System.err.println("Auction not found: " + auctionId);
                return;
            }

            synchronized (auction) {
                // Verify auction is still active
                if (!"OPEN".equals(auction.getStatus())) {
                    return;
                }

                // Place the bid
                if (bidAmount > auction.getCurrentHighestBid()) {
                    auction.setCurrentHighestBid(bidAmount);
                    auction.setHighestBidder(username);
                    
                    // Update database
                    auctionDAO.updateHighestBid(auctionId, bidAmount);
                    
                    // Ghi lại lịch sử đấu giá cho Auto-bid (Sử dụng trim để tránh lỗi khoảng trắng)
                    User user = userDAO.findByUsername(username.trim());
                    if (user != null) {
                        BidHistory history = new BidHistory(
                            auctionId,
                            user.getId(),
                            username.trim(),
                            bidAmount,
                            LocalDateTime.now()
                        );
                        bidHistoryDAO.addBid(history);
                    }
                    
                    System.out.println("Auto-bid executed: " + username + " -> " + bidAmount);
                } else {
                    System.out.println("Auto-bid skipped (bid amount not higher): " + bidAmount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error placing auto-bid: " + e.getMessage());
        }
    }

    /**
     * Deactivate auto-bid for a user on an auction
     */
    public boolean deactivateAutoBid(int userId, String auctionId) {
        try {
            AutoBid autoBid = autoBidDAO.findByUserAndAuction(userId, auctionId);
            if (autoBid != null) {
                return autoBidDAO.deactivateAutoBid(autoBid.getId());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deactivating auto-bid: " + e.getMessage());
            return false;
        }
    }
}
