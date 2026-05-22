package com.bidnova.utils;

import com.bidnova.models.User;

public class SessionManager {
    private static int userId;
    private static String username = null;
    private static String currentToken = null;
    private static String email = null;
    private static String fullName = null;
    private static String phone = null;
    private static String gender = null;
    private static String role = null;

    // Cất token vào két
    public static void setToken(String token) {
        currentToken = token;
    }

    // Lấy token ra xài
    public static String getToken() {
        return currentToken;
    }

    public static int getUserId() {
        return userId;
    }
    
    // SỬA LẠI CHỖ NÀY: Check xem đã đăng nhập chưa dựa vào username
    public static boolean isLoggedIn() {
        // Nếu username có dữ liệu (không null và không rỗng) nghĩa là đã đăng nhập
        return username != null && !username.trim().isEmpty() && userId > 0;
    }

    public static void login(User user) {
        userId = user.getId();
        username = user.getUsername();
        currentToken = user.getToken();
        email = user.getEmail();
        fullName = user.getFullName();
        phone = user.getPhone();
        gender = user.getGender();
        role = user.getRole();
    }
    
    public static String getUsername() { 
        return username; 
    }
    
    public static String getEmail() { return email; }
    public static String getFullName() { return fullName; }
    public static String getPhone() { return phone; }
    public static String getGender() { return gender; }
    public static String getRole() { return role; }

    // Đăng xuất thì xóa trắng cả 2
    public static void logout() {
        currentToken = null;
        username = null;
        userId = 0;
        email = null;
        fullName = null;
        phone = null;
        gender = null;
        role = null;
    }
}