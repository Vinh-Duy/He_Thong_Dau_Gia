package com.daugia.handlers;

import java.util.List;

import com.daugia.models.Auction;
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
    public Response handle(Request request) {
        try {
            JsonObject body = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            int sellerId = body.get("sellerId").getAsInt();

            List<Auction> list = auctionService.getAuctionsBySellerId(sellerId);
            return new Response("SUCCESS", "Lấy sản phẩm thành công", gson.toJson(list));
        } catch (Exception e) {
            return new Response("ERROR", "GET_MY_AUCTIONS lỗi: " + e.getMessage(), null);
        }
    }
}