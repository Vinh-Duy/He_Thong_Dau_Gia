package com.daugia.handlers;

import java.util.HashMap;
import java.util.Map;

/**
 * "BẢNG PHÂN CÔNG CÔNG VIỆC" CỦA SERVER.
 *
 * Khi client gửi lên 1 request với action="LOGIN", Server phải biết
 * đưa việc này cho AI làm. HandlerRegistry làm đúng việc đó:
 * - Nó lưu 1 bảng: action name -> đối tượng Handler tương ứng.
 * - Ví dụ: "LOGIN" -> new LoginHandler(), "PLACE_BID" -> new PlaceBidHandler().
 *
 * Cách hoạt động:
 * 1. ClientHandler nhận request từ client.
 * 2. ClientHandler gọi registry.get(request.getAction()) để lấy handler.
 * 3. ClientHandler gọi handler.handle(request, authUser) để xử lý.
 *
 * Muốn thêm chức năng mới? Chỉ cần:
 * 1. Viết class MyHandler implements ActionHandler.
 * 2. Thêm dòng handlers.put("MY_ACTION", new MyHandler()) ở constructor.
 */

public class HandlerRegistry {
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    public HandlerRegistry() {
        handlers.put("LOGIN", new LoginHandler());
        handlers.put("REGISTER", new RegisterHandler());
        handlers.put("GET_ALL_USERS", new GetAllUsersHandler());
        handlers.put("GET_ALL_AUCTIONS", new GetAllAuctionsHandler());
        handlers.put("GET_AUCTIONS_BY_CATEGORY", new GetAuctionsByCategoryHandler());
        handlers.put("GET_AUCTION_BY_ID", new GetAuctionByIdHandler());
        handlers.put("SET_AUTO_BID", new SetAutoBidHandler());
        handlers.put("GET_MY_AUCTIONS", new GetMyAuctionsHandler());
        handlers.put("UPDATE_PRODUCT", new UpdateProductHandler());
        handlers.put("DELETE_PRODUCT", new DeleteProductHandler());
        handlers.put("ADD_PRODUCT", new AddProductHandler());
        handlers.put("PLACE_BID", new PlaceBidHandler());
        handlers.put("DELETE_USER", new DeleteUserHandler());
    }

    public ActionHandler get(String action) {
        return handlers.get(action);
    }
}