package com.daugia.handlers;

import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.dao.AuctionDAO;
import com.google.gson.Gson;

public class GetAuctionByIdHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            String auctionId = request.getPayload();
            
            if (auctionId == null || auctionId.trim().isEmpty()) {
                return new Response("ERROR", "Auction ID is required", null);
            }

            Auction auction = auctionDAO.findById(auctionId);
            
            if (auction != null) {
                String auctionJson = gson.toJson(auction);
                return new Response("SUCCESS", "Auction found", auctionJson);
            } else {
                return new Response("ERROR", "Auction not found", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "Error retrieving auction: " + e.getMessage(), null);
        }
    }
}
