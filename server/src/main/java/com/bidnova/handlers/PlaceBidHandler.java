package com.bidnova.handlers;

import java.time.LocalDateTime;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.dao.BidHistoryDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.BidHistory;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.services.AutoBidService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 💰 PlaceBidHandler - Xử lý yêu cầu đặt giá từ client
 * 
 * <h2>Chức Năng:</h2>
 * <p>Nhận request "PLACE_BID" từ client, thực hiện các kiểm tra an toàn,
 * cập nhật giá cao nhất, ghi lịch sử, trigger AutoBid, và broadcast updates.</p>
 * 
 * <h2>Quy Trình Xử Lý:</h2>
 * <pre>
 * Client Request: PLACE_BID
 * {
 *   "action": "PLACE_BID",
 *   "payload": {
 *     "auctionId": "auction_123456",
 *     "amount": 105000000
 *   },
 *   "token": "..."
 * }
 *         ↓
 *    ┌─── PlaceBidHandler.handle() ───┐
 *    │
 *    ├─ 1. Xác thực user (authUser != null)
 *    ├─ 2. Kiểm tra phiên đấu giá tồn tại
 *    ├─ 3. Lock phiên (synchronized) để tránh race condition
 *    ├─ 4. Kiểm tra status = "OPEN"
 *    ├─ 5. Kiểm tra phiên chưa hết thời gian
 *    ├─ 6. Kiểm tra bidAmount > currentHighestBid ✓
 *    ├─ 7. ⭐ Kiểm tra MIN BID INCREMENT ✓
 *    ├─ 8. ⭐ Kiểm tra PRICE CEILING ✓
 *    ├─ 9. Cập nhật currentHighestBid vào DB
 *    ├─ 10. Ghi BidHistory vào DB
 *    ├─ 11. Anti-snipping: gia hạn 5 phút nếu bid trong 5 phút cuối
 *    ├─ 12. Trigger AutoBidService để execute AutoBids
 *    ├─ 13. Broadcast updates tới tất cả clients
 *    └─ 14. Trả Response lại client
 *         ↓
 * Client Response: SUCCESS
 * {
 *   "status": "SUCCESS",
 *   "message": "Đặt giá thành công",
 *   "payload": {
 *     "auctionId": "auction_123456",
 *     "newHighestBid": 105000000,
 *     "highestBidder": "username",
 *     "ceilingReached": false,
 *     "action": "BID_UPDATE",
 *     "event": { ... }
 *   }
 * }
 * </pre>
 * 
 * <h2>Validation Errors:</h2>
 * <table border="1">
 *   <tr><th>Error Case</th><th>Response Message</th></tr>
 *   <tr><td>Unauthorized</td><td>"Unauthorized"</td></tr>
 *   <tr><td>Phiên không tồn tại</td><td>"Phiên đấu giá không tồn tại!"</td></tr>
 *   <tr><td>Status != OPEN</td><td>"Phiên đấu giá đã đóng"</td></tr>
 *   <tr><td>Hết thời gian</td><td>"Phiên đấu giá đã hết thời gian"</td></tr>
 *   <tr><td>Giá <= hiện tại</td><td>"Giá đặt phải cao hơn giá hiện tại (X)"</td></tr>
 *   <tr><td>❌ Bước giá < minimum</td><td>"Bước giá tối thiểu là X. Giá tối thiểu yêu cầu: Y"</td></tr>
 *   <tr><td>❌ Vượt giá trần</td><td>Tự động kết thúc phiên (status = FINISHED)</td></tr>
 * </table>
 * 
 * <h2>Tính Năng Nâng Cao:</h2>
 * <ul>
 *   <li>✅ <b>Min Bid Increment Validation:</b> Đảm bảo bước giá tăng tối thiểu</li>
 *   <li>✅ <b>Price Ceiling Check:</b> Tự động kết thúc phiên khi đạt trần</li>
 *   <li>✅ <b>AutoBid Triggering:</b> Kích hoạt các AutoBid rules hoạt động</li>
 *   <li>✅ <b>Anti-Snipping Logic:</b> Gia hạn 5 phút nếu bid trong 5 phút cuối</li>
 *   <li>✅ <b>Real-time Broadcasting:</b> Gửi updates tới tất cả clients</li>
 *   <li>✅ <b>Thread-Safe:</b> Sử dụng synchronized block để tránh race condition</li>
 * </ul>
 * 
 * <h2>Ví Dụ Scenarios:</h2>
 * <pre>
 * ==================== Scenario 1: Normal Bid ====================
 * Current: 100M, Min Increment: 1M, Ceiling: 150M
 * User A bids: 105M
 * 
 * Validation:
 *   ✓ 105M > 100M (higher than current)
 *   ✓ (105M - 100M = 5M) >= 1M (meets min increment)
 *   ✓ 105M < 150M (below ceiling)
 * 
 * Result: BID ACCEPTED
 *   - currentHighestBid = 105M
 *   - highestBidder = "UserA"
 *   - status = "OPEN" (still ongoing)
 * 
 * 
 * ==================== Scenario 2: Below Min Increment ====================
 * Current: 100M, Min Increment: 2M
 * User A bids: 101M
 * 
 * Validation:
 *   ✓ 101M > 100M
 *   ❌ (101M - 100M = 1M) < 2M (TOO SMALL!)
 * 
 * Result: BID REJECTED
 *   Error: "Bước giá tối thiểu là 2,000,000. Giá tối thiểu yêu cầu: 102,000,000"
 * 
 * 
 * ==================== Scenario 3: Price Ceiling ====================
 * Current: 145M, Ceiling: 150M, Min Increment: 1M
 * User A bids: 150M
 * 
 * Validation:
 *   ✓ 150M > 145M
 *   ✓ (150M - 145M = 5M) >= 1M
 *   ⭐ 150M >= 150M (AT CEILING!)
 * 
 * Result: BID ACCEPTED + AUCTION FINISHED
 *   - currentHighestBid = 150M
 *   - status = "FINISHED" (auto-closed)
 *   - ceilingReached = true
 *   - action = "AUCTION_FINISHED"
 *   - All AutoBids deactivated
 * </pre>
 * 
 * @author BidNova Team
 * @version 2.0 (with min increment & price ceiling)
 * @see ActionHandler
 * @see AutoBidService
 * @see Auction
 */
public class PlaceBidHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidHistoryDAO bidHistoryDAO = new BidHistoryDAO();
    private final AutoBidService autoBidService = new AutoBidService();

    /**
     * handle() - Xử lý request PLACE_BID
     * 
     * @param request Request từ client chứa auctionId và bidAmount
     * @param authUser Thông tin user đã xác thực
     * @return Response object chứa status, message, và payload
     * 
     * @throws Exception Nếu có lỗi parse JSON hoặc DB access
     */
    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String auctionId = bidData.get("auctionId").getAsString();
            double bidAmount = bidData.get("amount").getAsDouble();

            Auction currentAuction = AuctionManager.getInstance().getAuction(auctionId);
            if (currentAuction == null) {
                return new Response("ERROR", "Phiên đấu giá không tồn tại!", null);
            }

            synchronized (currentAuction) {
                String status = currentAuction.getStatus();
                if (status != null && !status.equalsIgnoreCase("OPEN")) {
                    return new Response("ERROR", "Phiên đấu giá đã đóng", null);
                }

                if (isExpired(currentAuction.getEndTime())) {
                    currentAuction.setStatus("FINISHED");
                    return new Response("ERROR", "Phiên đấu giá đã hết thời gian", null);
                }

                if (bidAmount <= currentAuction.getCurrentHighestBid()) {
                    return new Response(
                            "ERROR",
                            "Giá đặt phải cao hơn giá hiện tại (" + currentAuction.getCurrentHighestBid() + ")",
                            null
                    );
                }

                // ⭐️ NEW: Validate minimum bid increment
                double bidIncrement = bidAmount - currentAuction.getCurrentHighestBid();
                if (bidIncrement < currentAuction.getMinBidIncrement()) {
                    double minRequiredBid = currentAuction.getCurrentHighestBid() + currentAuction.getMinBidIncrement();
                    return new Response(
                        "ERROR",
                        String.format("Bước giá tối thiểu là %.0f. Giá tối thiểu yêu cầu: %.0f", 
                            currentAuction.getMinBidIncrement(), 
                            minRequiredBid),
                        null
                    );
                }

                currentAuction.setCurrentHighestBid(bidAmount);
                currentAuction.setHighestBidder(authUser.getUsername());
                auctionDAO.updateHighestBid(auctionId, bidAmount);

                // Ghi lại lịch sử đấu giá vào Database
                BidHistory history = new BidHistory(
                    auctionId, 
                    authUser.getUserId(), 
                    authUser.getUsername(), 
                    bidAmount, 
                    LocalDateTime.now()
                );
                bidHistoryDAO.addBid(history);

                // Anti-snipping logic: Nếu đặt giá trong 5 phút cuối (300 giây), gia hạn thêm 5 phút
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endTime = currentAuction.getEndTime();
                boolean isExtended = false;
                if (endTime != null) {
                    java.time.Duration timeRemaining = java.time.Duration.between(now, endTime);
                    long secondsRemaining = timeRemaining.getSeconds();
                    if (secondsRemaining > 0 && secondsRemaining <= 300) {
                        LocalDateTime newEndTime = endTime.plusMinutes(5);
                        currentAuction.setEndTime(newEndTime);
                        auctionDAO.updateEndTime(auctionId, newEndTime);
                        isExtended = true;
                        System.out.println("[Anti-Snipping] Gia hạn phiên đấu giá " + auctionId + " thêm 5 phút. Hạn mới: " + newEndTime);
                    }
                }
                // Execute auto-bids after this bid is placed
                autoBidService.executeAutoBids(auctionId, bidAmount);

                // Lấy giá MỚI NHẤT sau khi auto-bids trigger (có thể đã tăng)
                double finalHighestBid = currentAuction.getCurrentHighestBid();

                // ⭐️ NEW: Check if price ceiling reached
                boolean ceilingReached = false;
                if (currentAuction.isBidAtCeiling(finalHighestBid)) {
                    currentAuction.setStatus("FINISHED");
                    auctionDAO.updateStatus(auctionId, "FINISHED");
                    // Remove auction khỏi AuctionManager để ngăn người khác tiếp tục đặt giá
                    AuctionManager.getInstance().removeAuction(auctionId);
                    ceilingReached = true;
                    System.out.println("🎯 Auction " + auctionId + " FINISHED - Price ceiling reached!");
                }

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", finalHighestBid);
                successData.addProperty("ceilingReached", ceilingReached); // ⭐️ NEW
                if (currentAuction.getEndTime() != null) {
                    successData.addProperty("newEndTime", currentAuction.getEndTime().toString());
                }
                successData.addProperty("isExtended", isExtended);

                JsonObject event = new JsonObject();
                event.addProperty("action", ceilingReached ? "AUCTION_FINISHED" : "BID_UPDATE"); // ⭐️ UPDATED
                event.addProperty("payload", gson.toJson(successData));

                JsonObject result = new JsonObject();
                result.add("bidResult", successData);
                result.add("event", event);

                return new Response("SUCCESS", "Đặt giá thành công", gson.toJson(result));
            }

        } catch (Exception e) {
            return new Response("ERROR", "PLACE_BID lỗi: " + e.getMessage(), null);
        }
    }

    private boolean isExpired(LocalDateTime endTime) {
        if (endTime == null) return false;
        return LocalDateTime.now().isAfter(endTime);
    }
}