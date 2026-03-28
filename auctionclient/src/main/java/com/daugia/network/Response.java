package com.daugia.network;

public class Response {
    private String status; // "SUCCESS" hoặc "ERROR"
    private String message; // Thông báo lỗi hoặc thành công
    private String data;    // Dữ liệu trả về (VD: thông tin User)

    public Response(String status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getData() { return data; }
}