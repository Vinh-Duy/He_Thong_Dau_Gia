package com.daugia.handlers;

import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GetAuctionsByCategoryHandler implements ActionHandler {
    private final Gson gson = new Gson();

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
