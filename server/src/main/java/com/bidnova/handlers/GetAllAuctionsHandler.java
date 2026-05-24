package com.bidnova.handlers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetAllAuctionsHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

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
