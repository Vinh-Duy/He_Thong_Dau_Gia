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

public class PlaceBidHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidHistoryDAO bidHistoryDAO = new BidHistoryDAO();
    private final AutoBidService autoBidService = new AutoBidService();

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

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", finalHighestBid);
                if (currentAuction.getEndTime() != null) {
                    successData.addProperty("newEndTime", currentAuction.getEndTime().toString());
                }
                successData.addProperty("isExtended", isExtended);

                JsonObject event = new JsonObject();
                event.addProperty("action", "BID_UPDATE");
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