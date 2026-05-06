package com.daugia.handlers;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistry {
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    public HandlerRegistry() {
        handlers.put("GET_MY_AUCTIONS", new GetMyAuctionsHandler());
        handlers.put("UPDATE_PRODUCT", new UpdateProductHandler());
        // thêm dần: ADD_PRODUCT, DELETE_PRODUCT...
    }

    public ActionHandler get(String action) {
        return handlers.get(action);
    }
}