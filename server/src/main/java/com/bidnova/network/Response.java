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

    @Override
    public String toString() {
        if (data != null) {
            return status + "|" + message + "|" + data;
        }
        return status + "|" + message;
    }
}
