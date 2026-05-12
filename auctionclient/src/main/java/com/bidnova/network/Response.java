package com.bidnova.network;

public class Response {
    private String status;
    private String message;
    private String data;

    public Response() {} // Gson cần constructor rỗng để deserialize

    public String getStatus()  { return status; }
    public String getMessage() { return message; }
    public String getData()    { return data; }
}