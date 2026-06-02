package com.bidnova.config;

public class NetworkConfig {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    
    public static String getHost() {
        // Ưu tiên: System Property → Environment Variable → Default
        String fromProperty = System.getProperty("auction.server.host");
        if (fromProperty != null && !fromProperty.isEmpty()) {
            return fromProperty;
        }
        
        String fromEnv = System.getenv("AUCTION_SERVER_HOST");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        
        return DEFAULT_HOST;
    }
    
    public static int getPort() {
        // Ưu tiên: System Property → Environment Variable → Default
        String fromProperty = System.getProperty("auction.server.port");
        if (fromProperty != null && !fromProperty.isEmpty()) {
            try {
                return Integer.parseInt(fromProperty);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port in property, using default: " + DEFAULT_PORT);
            }
        }
        
        String fromEnv = System.getenv("AUCTION_SERVER_PORT");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            try {
                return Integer.parseInt(fromEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port in env variable, using default: " + DEFAULT_PORT);
            }
        }
        
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
