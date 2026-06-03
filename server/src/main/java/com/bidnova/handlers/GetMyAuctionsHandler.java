package com.bidnova.handlers;

import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionService;
import com.bidnova.services.impl.AuctionServiceImpl;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetMyAuctionsHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            int sellerId;
            String role = authUser.getRole();

            if ("ADMIN".equalsIgnoreCase(role)) {
                // ADMIN được chỉ định sellerId từ payload
                if (request.getPayload() == null || request.getPayload().isEmpty()) {
                    return new Response("ERROR", "Payload không được để trống cho ADMIN", null);
                }
                JsonObject body = JsonParser.parseString(request.getPayload()).getAsJsonObject();
                if (!body.has("sellerId")) {
                    return new Response("ERROR", "Thiếu sellerId trong payload", null);
                }
                sellerId = body.get("sellerId").getAsInt();
            } else if ("SELLER".equalsIgnoreCase(role)) {
                // SELLER luôn xem sản phẩm của chính mình
                sellerId = authUser.getUserId();
            } else {
                return new Response("ERROR", "Forbidden: role không được phép truy cập", null);
            }

            List<Auction> list = auctionService.getAuctionsBySellerId(sellerId);
            return new Response("SUCCESS", "Lấy sản phẩm thành công", gson.toJson(list));

        } catch (Exception e) {
            return new Response("ERROR", "GET_MY_AUCTIONS lỗi: " + e.getMessage(), null);
        }
    }
}