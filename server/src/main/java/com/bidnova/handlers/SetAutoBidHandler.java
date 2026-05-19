package com.bidnova.handlers;

import com.bidnova.models.AutoBid;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SetAutoBidHandler implements ActionHandler {
    private final Gson gson = new Gson();
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
                return new Response("ERROR", "Auction not found", null);
            }

            if (!"OPEN".equals(auction.getStatus()) && !"RUNNING".equals(auction.getStatus())) {
                return new Response("ERROR", "Auction is not active", null);
            }

            // Check if user already has an auto-bid for this auction
            AutoBid existingAutoBid = autoBidDAO.findByUserAndAuction(authUser.getUsername(), auctionId);
            if (existingAutoBid != null) {
                return new Response("ERROR", "You already have an active auto-bid for this auction", null);
            }

            // Create new auto-bid
            AutoBid autoBid = new AutoBid();
            autoBid.setAuctionId(auctionId);
            autoBid.setUsername(authUser.getUsername());
            autoBid.setMaxBid(maxBid);
            autoBid.setIncrement(increment);

            boolean created = autoBidDAO.createAutoBid(autoBid);
            if (created) {
                return new Response("SUCCESS", "Auto-bid activated successfully", gson.toJson(autoBid));
            } else {
                return new Response("ERROR", "Failed to activate auto-bid", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "SET_AUTO_BID error: " + e.getMessage(), null);
        }
    }
}
