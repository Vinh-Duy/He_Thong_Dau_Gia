package com.daugia.network;

public class Request {
    private String action;
    private String payload; 
    private String data;   // Dữ liệu (JSON chữ)
    
    // THÊM CÁI NÀY: Chỗ để nhét chìa khóa vào mỗi lần gửi lệnh
    private String token; 

    public Request(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public String getPayload() { return payload; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

}
