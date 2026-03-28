package com.daugia.network;

public class Request {
    private String action; // Hành động (VD: "LOGIN", "BID", "GET_ITEMS")
    private String payload; // Dữ liệu đi kèm (VD: chuỗi JSON của user/pass)

    public Request(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public String getPayload() { return payload; }
}