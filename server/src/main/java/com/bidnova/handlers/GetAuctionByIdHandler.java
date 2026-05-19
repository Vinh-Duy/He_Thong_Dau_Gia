package com.bidnova.handlers;

import java.time.LocalDateTime;

import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetAuctionByIdHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
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
