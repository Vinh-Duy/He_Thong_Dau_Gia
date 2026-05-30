package com.bidnova.handlers;

import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AutoBidService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeactivateAutoBidHandler implements ActionHandler {
    private final AutoBidService autoBidService = new AutoBidService();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject data = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String auctionId = data.get("auctionId").getAsString();

            boolean success = autoBidService.deactivateAutoBid(authUser.getUserId(), auctionId);
            if (success) {
                return new Response("SUCCESS", "Đã tắt đấu giá tự động", null);
            } else {
                return new Response("ERROR", "Không tìm thấy cấu hình Auto-Bid đang hoạt động", null);
            }
        } catch (Exception e) {
            return new Response("ERROR", "DEACTIVATE_AUTO_BID error: " + e.getMessage(), null);
        }
    }
}