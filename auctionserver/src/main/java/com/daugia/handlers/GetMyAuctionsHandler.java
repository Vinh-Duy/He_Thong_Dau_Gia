package com.daugia.handlers;

import java.util.List;

import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionService;
import com.daugia.services.impl.AuctionServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetMyAuctionsHandler implements ActionHandler {
    private final Gson gson = new Gson();
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