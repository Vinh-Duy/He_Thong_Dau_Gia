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

public class GetAuctionsByCategoryHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            String requestedCategory = request.getPayload();
            List<Auction> allAuctions = new ArrayList<>(AuctionManager.getInstance().getAllAuctions());
            List<Auction> filteredAuctions = new ArrayList<>();

            for (Auction auc : allAuctions) {
                if (auc.getCategory() != null
                        && auc.getCategory().equalsIgnoreCase(requestedCategory)) {
                    filteredAuctions.add(auc);
                }
            }

            String payloadData = gson.toJson(filteredAuctions);
            return new Response("SUCCESS", "Lọc danh mục thành công", payloadData);

        } catch (Exception e) {
            return new Response("ERROR", "Lỗi khi lọc danh mục: " + e.getMessage(), null);
        }
    }
}
