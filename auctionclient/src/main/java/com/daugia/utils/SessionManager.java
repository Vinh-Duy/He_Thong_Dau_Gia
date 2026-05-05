package com.daugia.utils;

public class SessionManager {
    private static String currentToken = null;
    private static String username = null; // Biến lưu tên người dùng
    private static int userId;
    
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
        return username != null && !username.trim().isEmpty();
    }

    // Cất cả tên và token vào két
    public static void setSession(int id,String u, String t) {
        username = u;
        currentToken = t;
        userId=id;
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