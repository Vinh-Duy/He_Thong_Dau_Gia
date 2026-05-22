package com.bidnova.handlers;

import java.time.LocalDateTime;

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

public class UpdateProductHandler implements ActionHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();
    private final AuctionService auctionService = new AuctionServiceImpl();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            Auction updated = gson.fromJson(request.getPayload(), Auction.class);

            boolean ok = auctionService.updateProduct(
                updated,
                authUser.getUserId(),
                authUser.getRole()
            );

            if (!ok) {
                return new Response("ERROR", "Không có quyền hoặc cập nhật thất bại", null);
            }

            // Nếu sản phẩm đang có trong AuctionManager, kiểm tra và cập nhật trạng thái sau khi sửa
            AuctionManager.getInstance().checkAndUpdateExpiredStatus(updated);

            return new Response("SUCCESS", "Cập nhật thành công", null);
        } catch (Exception e) {
            return new Response("ERROR", "UPDATE_PRODUCT lỗi: " + e.getMessage(), null);
        }
    }
}