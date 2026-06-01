package com.daugia.handlers;

import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuctionManager;
import com.daugia.services.AuctionService;
import com.daugia.services.impl.AuctionServiceImpl;

public class DeleteProductHandler implements ActionHandler {
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            String auctionId = request.getPayload();
            if (auctionId == null || auctionId.isBlank()) {
                return new Response("ERROR", "Thiếu auctionId", null);
            }

            boolean ok = auctionService.deleteProduct(
                auctionId,
                authUser.getUserId(),
                authUser.getRole()
            );

            if (!ok) {
                return new Response("ERROR", "Không có quyền hoặc xóa thất bại", null);
            }

            // sync RAM sau khi xóa DB thành công
            AuctionManager.getInstance().getAllAuctions().removeIf(a -> auctionId.equals(a.getId()));

            return new Response("SUCCESS", "Xóa thành công!", null);
        } catch (Exception e) {
            return new Response("ERROR", "DELETE_PRODUCT lỗi: " + e.getMessage(), null);
        }
    }
}