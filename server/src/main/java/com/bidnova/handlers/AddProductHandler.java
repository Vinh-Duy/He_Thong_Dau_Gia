package com.bidnova.handlers;

import java.time.LocalDateTime;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AddProductHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            String role = authUser.getRole();
            if (!"SELLER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                return new Response("ERROR", "Forbidden: chỉ SELLER/ADMIN được thêm sản phẩm", null);
            }

            Auction newAuction = gson.fromJson(request.getPayload(), Auction.class);
            if (newAuction == null) {
                return new Response("ERROR", "Payload không hợp lệ", null);
            }

            // Không tin sellerId từ client, ép theo user đăng nhập (trừ khi admin muốn set tay thì sửa rule sau)
            if ("SELLER".equalsIgnoreCase(role)) {
                newAuction.setSellerId(authUser.getUserId());
            }

            String newId = "A" + System.currentTimeMillis();
            newAuction.setId(newId);

            if (newAuction.getStatus() == null || newAuction.getStatus().isBlank()) {
                newAuction.setStatus("OPEN");
            }

            boolean success = auctionDAO.addAuction(
                newAuction.getId(),
                newAuction.getProductName(),
                newAuction.getDescription(),
                newAuction.getStartPrice(),
                newAuction.getStartTime(),
                newAuction.getEndTime(),
                newAuction.getStatus(),
                newAuction.getCategory(),
                newAuction.getSellerId(),
                newAuction.getImageUrl()
            );

            if (!success) {
                return new Response("ERROR", "Lỗi DB: Database từ chối lưu dữ liệu", null);
            }

            AuctionManager.getInstance().addAuction(newAuction);
            return new Response("SUCCESS", "Đăng sản phẩm thành công!", newAuction);

        } catch (Exception e) {
            return new Response("ERROR", "ADD_PRODUCT lỗi: " + e.getMessage(), null);
        }
    }
}