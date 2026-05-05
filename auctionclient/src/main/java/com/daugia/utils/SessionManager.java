package com.daugia.utils;

public class SessionManager {
    private static String currentToken = null;
    private static String username = null;
    private static int userId;
    
    public static void setToken(String token) {
        currentToken = token;
    }

    public static String getToken() {
        return currentToken;
    }

    public static int getUserId() {
        return userId;
    }
    
    public static boolean isLoggedIn() {

        return username != null && !username.trim().isEmpty();
    }

    public static void setSession(int id,String u, String t) {
        username = u;
        currentToken = t;
        userId=id;
    }
    
    public static String getUsername() { 
        return username; 
    }
    
    public static void logout() {
        currentToken = null;
        username = null;
        userId = 0;
    }
}