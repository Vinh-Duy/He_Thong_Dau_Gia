package com.bidnova.utils;

public class SessionManager {
    private static int userId;
    private static String username = null; // Biến lưu tên người dùng
    private static String currentToken = null;
    
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

    public static void login(int _id, String _username, String _token) {
        userId = _id;
        username = _username;
        currentToken = _token;
    }
    
    public static String getUsername() { 
        return username; 
    }
    
    // Đăng xuất thì xóa trắng cả 2
    public static void logout() {
        currentToken = null;
        username = null;
        userId = 0;
    }
}