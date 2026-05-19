package com.bidnova.handlers;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistry {
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    /**
     * Khởi tạo các handlers.
     */
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