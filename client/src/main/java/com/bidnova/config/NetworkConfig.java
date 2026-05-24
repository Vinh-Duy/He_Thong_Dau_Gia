package com.bidnova.config;

public class NetworkConfig {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    
    public static String getHost() {
        return System.getProperty("auction.server.host", DEFAULT_HOST);
    }
    
    public static int getPort() {
        String portStr = System.getProperty("auction.server.port");
        if (portStr != null) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port in config, using default: " + DEFAULT_PORT);
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
