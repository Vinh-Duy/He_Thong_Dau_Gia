package com.daugia.handlers;

import com.daugia.models.Auction;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionService;
import com.daugia.services.impl.AuctionServiceImpl;
import com.google.gson.Gson;

public class UpdateProductHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public Response handle(Request request) {
        try {
            Auction updated = gson.fromJson(request.getPayload(), Auction.class);
            boolean ok = auctionService.updateProduct(updated, 0, "SELLER");
            if (!ok) return new Response("ERROR", "Cập nhật thất bại", null);
            return new Response("SUCCESS", "Cập nhật thành công", null);
        } catch (Exception e) {
            return new Response("ERROR", "UPDATE_PRODUCT lỗi: " + e.getMessage(), null);
        }
    }
}
