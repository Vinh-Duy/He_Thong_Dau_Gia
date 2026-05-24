package com.bidnova.handlers;

import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.services.AuctionService;
import com.bidnova.services.impl.AuctionServiceImpl;

/**
 * Class xử lý xóa sản phẩm (auction) của seller. Chỉ cho phép seller xóa sản phẩm của chính họ, admin có thể xóa tất cả.
 */
public class DeleteProductHandler implements ActionHandler {
    private final AuctionService auctionService = new AuctionServiceImpl();

    /**
     * Xử lý yêu cầu xóa sản phẩm. Kiểm tra quyền của user, nếu là seller thì chỉ được xóa sản phẩm của chính họ, admin có thể xóa tất cả. Sau khi xóa thành công trên DB, sẽ đồng bộ xóa trên RAM để đảm bảo dữ liệu nhất quán.
     * 
     * @param request Yêu cầu chứa auctionId cần xóa.
     * @param authUser Thông tin người dùng đã xác thực, dùng để kiểm tra quyền và xác định sellerId nếu là seller.
     * @return Phản hồi với status "SUCCESS" nếu xóa thành công, hoặc "ERROR" nếu có lỗi hoặc không có quyền. Message sẽ chứa thông tin chi tiết về kết quả.
     */
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