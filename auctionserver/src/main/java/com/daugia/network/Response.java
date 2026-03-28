package com.daugia.network;

public class Response {
    private String status;
    private String message;
    private String data;

    public Response(String status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getData() { return data; }
}