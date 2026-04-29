package com.bidnova.network;

public class Request {
    private String action;
    private String[] params;

    public Request(String action, String... params) {
        this.action = action;
        this.params = params;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(action);
        for (String param : params) {
            sb.append("|").append(param);
        }
        return sb.toString(); // Trả về dạng: ACTION|param1|param2...
    }

    // hàm convert từ chuỗi nhận được từ client thành đối tượng Request
    public static Request parse(String requestStr) {
        String[] parts = requestStr.split("\\|");
        String action = parts[0];
        String[] params = new String[parts.length - 1];
        System.arraycopy(parts, 1, params, 0, params.length);
        return new Request(action, params);
    }
}
