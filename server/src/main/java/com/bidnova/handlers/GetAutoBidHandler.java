package com.bidnova.handlers;

import com.bidnova.models.AutoBid;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;

public class GetAutoBidHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final AutoBidDAO autoBidDAO = new AutoBidDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject data = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String auctionId = data.get("auctionId").getAsString();
            AutoBid autoBid = autoBidDAO.findByUserAndAuction(authUser.getUserId(), auctionId);
            if (autoBid != null) {
                return new Response("SUCCESS", "Auto-bid found", gson.toJson(autoBid));
            } else {
                return new Response("SUCCESS", "No auto-bid found", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "GET_AUTO_BID error: " + e.getMessage(), null);
        }
    }
}