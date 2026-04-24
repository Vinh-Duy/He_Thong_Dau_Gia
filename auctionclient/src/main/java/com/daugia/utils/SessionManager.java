package com.daugia.utils;

public class SessionManager {
    private static String currentToken = null;

    // Cất token vào két
    public static void setToken(String token) {
        currentToken = token;
    }

    // Lấy token ra xài
    public static String getToken() {
        return currentToken;
    }
    
    // Check xem đã đăng nhập chưa
    public static boolean isLoggedIn() {
        return currentToken != null;
    }

    private static String username; // Thêm biến này

    public static void setSession(String u, String t) {
        username = u;
        currentToken = t;
    }
    
    public static String getUsername() { return username; }
    
    public static void logout() {
        currentToken = null;
        username = null;
    }
}