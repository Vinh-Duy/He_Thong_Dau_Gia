package com.bidnova.handlers;

import java.time.LocalDateTime;

import com.bidnova.ClientHandler;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
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

                // Lấy giá MỚI NHẤT sau khi auto-bids trigger (có thể đã tăng)
                double finalHighestBid = currentAuction.getCurrentHighestBid();

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", finalHighestBid);

                JsonObject event = new JsonObject();
                event.addProperty("action", "BID_UPDATE");
                event.addProperty("payload", gson.toJson(successData));

                // Broadcast BID_UPDATE cho TẤT CẢ clients (real-time update)
                JsonObject broadcastEvent = new JsonObject();
                broadcastEvent.addProperty("action", "BID_UPDATE");
                JsonObject broadcastPayload = new JsonObject();
                broadcastPayload.addProperty("auctionId", auctionId);
                broadcastPayload.addProperty("newHighestBid", finalHighestBid);
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

    private boolean isExpired(LocalDateTime endTime) {
        if (endTime == null) return false;
        return LocalDateTime.now().isAfter(endTime);
    }
}