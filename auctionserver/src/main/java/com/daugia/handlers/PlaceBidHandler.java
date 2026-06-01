package com.daugia.handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.daugia.ClientHandler;
import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AntiSnipingService;
import com.daugia.services.AuctionManager;
import com.daugia.services.AutoBidService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * XỬ LÝ VIỆC ĐẶT GIÁ (Ra Giá) TRONG PHIÊN ĐẤU GIÁ.
 *
 * Luồng xử lý khi User bấm "Ra Giá" ở client:
 * 1. Client gửi request action="PLACE_BID" với {auctionId, amount, username}.
 * 2. Server nhận -> HandlerRegistry đưa request này cho PlaceBidHandler.
 * 3. PlaceBidHandler kiểm tra:
 *    - User đã đăng nhập chưa (authUser != null).
 *    - Auction có tồn tại không.
 *    - Phiên đấu giá còn mở không (status = OPEN/RUNNING).
 *    - Phiên đã hết hạn chưa (endTime).
 *    - Giá đặt có CAO HƠN giá hiện tại không.
 * 4. Nếu hợp lệ:
 *    - Cập nhật giá cao nhất trong AuctionManager (RAM) và AuctionDAO (Database).
 *    - Gọi AutoBidService.executeAutoBids() -> tự động đặt giá cho những user đã
 *      bật auto-bid trước đó.
 *    - Lấy giá MỚI NHẤT sau auto-bid.
 *    - Gửi BID_UPDATE broadcast cho TẤT CẢ clients đang online
 *      -> Mọi người thấy giá nhảy real-time trên màn hình.
 * 5. Trả response về client gọi.
 */
public class PlaceBidHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AutoBidService autoBidService = new AutoBidService();
    private final AntiSnipingService antiSnipingService = new AntiSnipingService();

    private static final DateTimeFormatter[] END_TIME_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

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
                return new Response("ERROR", "Mã hàng không tồn tại!", null);
            }

            synchronized (currentAuction) {
                String status = currentAuction.getStatus();
                if (status != null
                        && !status.equalsIgnoreCase("OPEN")
                        && !status.equalsIgnoreCase("RUNNING")) {
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

                currentAuction.setCurrentHighestBid(bidAmount);
                auctionDAO.updateHighestBid(auctionId, bidAmount);

                // Execute auto-bids after this bid is placed
                autoBidService.executeAutoBids(auctionId, bidAmount);
                
                // Check and apply anti-sniping if needed
                String newEndTime = antiSnipingService.checkAndExtendIfNeeded(auctionId, currentAuction);

                // Lấy giá MỚI NHẤT sau khi auto-bids trigger (có thể đã tăng)
                double finalHighestBid = currentAuction.getCurrentHighestBid();

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", finalHighestBid);
                if (newEndTime != null) {
                    successData.addProperty("newEndTime", newEndTime);
                    System.out.println("✓ Anti-Sniping: Time extended to " + newEndTime);
                }

                JsonObject event = new JsonObject();
                event.addProperty("action", "BID_UPDATE");
                event.addProperty("payload", gson.toJson(successData));

                // Broadcast BID_UPDATE cho TẤT CẢ clients (real-time update)
                JsonObject broadcastEvent = new JsonObject();
                broadcastEvent.addProperty("action", "BID_UPDATE");
                JsonObject broadcastPayload = new JsonObject();
                broadcastPayload.addProperty("auctionId", auctionId);
                broadcastPayload.addProperty("newHighestBid", finalHighestBid);
                if (newEndTime != null) {
                    broadcastPayload.addProperty("newEndTime", newEndTime);
                }
                broadcastEvent.addProperty("payload", gson.toJson(broadcastPayload));
                ClientHandler.broadcastAll(gson.toJson(broadcastEvent));

                JsonObject result = new JsonObject();
                result.add("bidResult", successData);
                result.add("event", event);

                return new Response("SUCCESS", "Đặt giá thành công", gson.toJson(result));
            }

        } catch (Exception e) {
            return new Response("ERROR", "PLACE_BID lỗi: " + e.getMessage(), null);
        }
    }

    private boolean isExpired(String endTimeRaw) {
        if (endTimeRaw == null || endTimeRaw.isBlank()) return false;
        for (DateTimeFormatter fmt : END_TIME_FORMATS) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTimeRaw, fmt);
                return LocalDateTime.now().isAfter(end);
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }
}