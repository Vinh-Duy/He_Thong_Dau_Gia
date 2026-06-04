package com.bidnova.config;

public class NetworkConfig {
    private static final String DEFAULT_HOST = "127.0.0.1";  // Local development
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    
    public static String getHost() {
        // Ưu tiên: System Property → Environment Variable → Default
        String fromProperty = System.getProperty("auction.server.host");
        if (fromProperty != null && !fromProperty.isEmpty()) {
            System.out.println("📍 Using host from property: " + fromProperty);
            return fromProperty;
        }
        
        String fromEnv = System.getenv("AUCTION_SERVER_HOST");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            System.out.println("📍 Using host from env var: " + fromEnv);
            return fromEnv;
        }
        
        System.out.println("📍 Using default host: " + DEFAULT_HOST);
        return DEFAULT_HOST;
    }
    
    public static int getPort() {
        // Ưu tiên: System Property → Environment Variable → Default
        String fromProperty = System.getProperty("auction.server.port");
        if (fromProperty != null && !fromProperty.isEmpty()) {
            try {
                int port = Integer.parseInt(fromProperty);
                System.out.println("📍 Using port from property: " + port);
                return port;
            } catch (NumberFormatException e) {
                System.err.println("Invalid port in property, using default: " + DEFAULT_PORT);
            }
        }
        
        String fromEnv = System.getenv("AUCTION_SERVER_PORT");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            try {
                int port = Integer.parseInt(fromEnv);
                System.out.println("📍 Using port from env var: " + port);
                return port;
            } catch (NumberFormatException e) {
                System.err.println("Invalid port in env variable, using default: " + DEFAULT_PORT);
            }
        }
        
        System.out.println("📍 Using default port: " + DEFAULT_PORT);
        return DEFAULT_PORT;
    }
    
    public static int getTimeoutSeconds() {
        String timeoutStr = System.getProperty("auction.server.timeout");
        if (timeoutStr != null) {
            try {
                return Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid timeout in config, using default: " + DEFAULT_TIMEOUT_SECONDS);
            }
        }
        return DEFAULT_TIMEOUT_SECONDS;
    }
}
