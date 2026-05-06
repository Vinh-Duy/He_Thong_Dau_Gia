package com.daugia.handlers;

import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GetAllAuctionsHandler implements ActionHandler {
    private final Gson gson = new Gson();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            List<Auction> danhSachHang = new ArrayList<>(AuctionManager.getInstance().getAllAuctions());
            String payloadData = gson.toJson(danhSachHang);
            return new Response("SUCCESS", "Lấy danh sách thành công", payloadData);

        } catch (Exception e) {
            return new Response("ERROR", "Lỗi nội bộ Server: " + e.getMessage(), null);
        }
    }
}
