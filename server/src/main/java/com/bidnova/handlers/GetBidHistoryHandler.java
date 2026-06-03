package com.bidnova.handlers;

import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.dao.BidHistoryDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.BidHistory;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetBidHistoryHandler implements ActionHandler {
    private final BidHistoryDAO bidHistoryDAO = new BidHistoryDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            // Fix: Check if user is authenticated (since GET_BID_HISTORY is not a public action)
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized: Vui lòng đăng nhập", null);
            }
            
            String auctionId = request.getPayload();
            if (auctionId == null || auctionId.isBlank()) {
                return new Response("ERROR", "Thiếu mã phiên đấu giá", null);
            }

            List<BidHistory> history = bidHistoryDAO.getBidHistoryForAuction(auctionId);
            
            // Trả về danh sách lịch sử dưới dạng JSON
            return new Response("SUCCESS", "Lấy lịch sử thành công", gson.toJson(history));
            
        } catch (Exception e) {
            return new Response("ERROR", "Lỗi lấy lịch sử: " + e.getMessage(), null);
        }
    }
}