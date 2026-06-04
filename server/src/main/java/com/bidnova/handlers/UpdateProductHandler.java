package com.bidnova.handlers;

import java.time.LocalDateTime;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.services.AuctionService;
import com.bidnova.services.impl.AuctionServiceImpl;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class UpdateProductHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
    private final AuctionService auctionService = new AuctionServiceImpl();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            // Fix: Validate payload exists before parsing
            if (request.getPayload() == null || request.getPayload().isEmpty()) {
                return new Response("ERROR", "Payload không được để trống", null);
            }
            
            Auction updated = gson.fromJson(request.getPayload(), Auction.class);
            if (updated == null || updated.getId() == null || updated.getId().isEmpty()) {
                return new Response("ERROR", "Thông tin sản phẩm không hợp lệ", null);
            }

            boolean ok = auctionService.updateProduct(
                updated,
                authUser.getUserId(),
                authUser.getRole()
            );

            if (!ok) {
                return new Response("ERROR", "Không có quyền hoặc cập nhật thất bại", null);
            }

            // Lấy lại dữ liệu đầy đủ từ DB để đồng bộ RAM (tránh mất các field không có trong payload update)
            Auction fullUpdated = auctionDAO.findById(updated.getId());
            if (fullUpdated != null) {
                AuctionManager.getInstance().addAuction(fullUpdated);
                AuctionManager.getInstance().checkAndUpdateExpiredStatus(fullUpdated);
                updated = fullUpdated; // Dùng bản full để tạo event trả về cho client
            }

            // Chuẩn bị payload event để ClientHandler thực hiện broadcast real-time cho các Bidder
            JsonObject event = new JsonObject();
            event.addProperty("action", "PRODUCT_UPDATED");
            event.addProperty("payload", gson.toJson(updated));

            JsonObject responsePayload = new JsonObject();
            responsePayload.add("event", event);

            return new Response("SUCCESS", "Cập nhật thành công", responsePayload);
        } catch (Exception e) {
            return new Response("ERROR", "UPDATE_PRODUCT lỗi: " + e.getMessage(), null);
        }
    }
}