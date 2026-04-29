package com.bidnova.utils;

// ta cần lớp này để lưu thông tin người dùng sau khi đăng nhập thành công và update header, có thể sử dụng Singleton pattern để dễ dàng truy cập từ bất kỳ đâu trong ứng dụng
public class UserSession {
    private static UserSession instance;
    private String username;

    // Singleton pattern - private constructor
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void signin(String username) {
        setUsername(username);
    }

    public void signout() {
        setUsername(null);
    }

    public boolean isSignedIn() {
        return username != null;
    }
}