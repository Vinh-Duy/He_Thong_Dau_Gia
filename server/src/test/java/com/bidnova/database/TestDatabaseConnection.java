package com.bidnova.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test database connection using H2 in-memory database
 */
public class TestDatabaseConnection {
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
            if (conn == null) {
                throw new SQLException("Failed to get H2 connection");
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("Error getting H2 connection: " + e.getMessage());
            throw e;
        }
    }
    
    public static void setupTestDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create auctions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(255),
                    description TEXT,
                    start_price DOUBLE,
                    current_highest_bid DOUBLE DEFAULT 0,
                    start_time TIMESTAMP,
                    end_time TIMESTAMP,
                    status VARCHAR(50),
                    category VARCHAR(100),
                    seller_id INT,
                    image_url VARCHAR(500),
                    price_ceiling DOUBLE,
                    min_bid_increment DOUBLE DEFAULT 1000
                )
                """);
            
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY,
                    username VARCHAR(100),
                    password VARCHAR(255),
                    email VARCHAR(255),
                    full_name VARCHAR(255),
                    phone VARCHAR(20),
                    gender VARCHAR(10),
                    role VARCHAR(50)
                )
                """);
            
            // Create auto_bids table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS auto_bids (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT,
                    auction_id VARCHAR(50),
                    max_bid DOUBLE,
                    increment DOUBLE DEFAULT 1000,
                    active BOOLEAN DEFAULT TRUE,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (auction_id) REFERENCES auctions(id)
                )
                """);
            
            // Create bid_history table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bid_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    auction_id VARCHAR(50),
                    user_id INT,
                    bid_amount DOUBLE,
                    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (auction_id) REFERENCES auctions(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);
            
            System.out.println("Test database setup completed");
            
        } catch (SQLException e) {
            System.err.println("Error setting up test database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void cleanupTestDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("DROP TABLE IF EXISTS bid_history");
            stmt.execute("DROP TABLE IF EXISTS auto_bids");
            stmt.execute("DROP TABLE IF EXISTS auctions");
            stmt.execute("DROP TABLE IF EXISTS users");
            
            System.out.println("Test database cleanup completed");
            
        } catch (SQLException e) {
            System.err.println("Error cleaning up test database: " + e.getMessage());
        }
    }
}
