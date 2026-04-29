package com.bidnova.network;

public class Response {
    private String status; // "SUCCESS" hoặc "FAILED"
    private String message; // Thông điệp chi tiết
    private String data; // Dữ liệu trả về (nếu có)

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(String status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Parse response từ string format: "STATUS|message|data" hoặc "STATUS|message"
     */
    public static Response parse(String responseStr) {
        if (responseStr == null || responseStr.isEmpty()) {
            return new Response("FAILED", "Không nhận được phản hồi từ server");
        }
        
        String[] parts = responseStr.split("\\|", 3); // Split tối đa 3 phần
        String status = parts[0];
        String message = parts.length > 1 ? parts[1] : "";
        String data = parts.length > 2 ? parts[2] : null;
        
        return new Response(status, message, data);
    }

    @Override
    public String toString() {
        if (data != null) {
            return status + "|" + message + "|" + data;
        }
        return status + "|" + message;
    }
}
