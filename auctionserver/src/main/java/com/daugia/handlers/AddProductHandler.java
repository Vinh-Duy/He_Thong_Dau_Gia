package com.daugia.handlers;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.google.gson.Gson;

public class AddProductHandler implements ActionHandler {
    private final Gson gson = new Gson();
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
                newAuction.getCurrentHighestBid(),
                newAuction.getEndTime(),
                newAuction.getSellerId(),
                newAuction.getStatus()
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