package com.daugia.handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlaceBidHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionDAO auctionDAO = new AuctionDAO();

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

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", bidAmount);

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