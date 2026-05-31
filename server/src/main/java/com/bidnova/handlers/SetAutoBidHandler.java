package com.bidnova.handlers;

import com.bidnova.models.AutoBid;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;

public class SetAutoBidHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final AutoBidDAO autoBidDAO = new AutoBidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String auctionId = bidData.get("auctionId").getAsString();
            double maxBid = bidData.get("maxBid").getAsDouble();
            double increment = bidData.get("increment").getAsDouble();

            // Validate auction exists and is active
            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null) {
                return new Response("ERROR", "Không tìm thấy phiên đấu giá", null);
            }

            if (!"OPEN".equals(auction.getStatus())) {
                return new Response("ERROR", "Phiên đấu giá đã đóng", null);
            }

            // Check if user already has an auto-bid for this auction
            AutoBid existingAutoBid = autoBidDAO.findByUserAndAuction(authUser.getUserId(), auctionId);
            if (existingAutoBid != null) {
                return new Response("ERROR", "Bạn đã bật tính năng Auto-bid cho phiên đấu giá này", null);
            }

            // Create new auto-bid
            AutoBid autoBid = new AutoBid();
            autoBid.setAuctionId(auctionId);
            autoBid.setUserId(authUser.getUserId());
            autoBid.setMaxBid(maxBid);
            autoBid.setIncrement(increment);

            boolean created = autoBidDAO.createAutoBid(autoBid);
            if (created) {
                return new Response("SUCCESS", "Bật thành công tính năng Auto-bid", gson.toJson(autoBid));
            } else {
                return new Response("ERROR", "Bật thất bại tính năng Auto-bid", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "SET_AUTO_BID error: " + e.getMessage(), null);
        }
    }
}
