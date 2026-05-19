package com.bidnova.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Xóa cái biến `private static Connection connection = null;` đi
    
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    // Hàm này sẽ trả về một kết nối MỚI mỗi khi được gọi
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Lỗi kết nối Database!");
            e.printStackTrace();
            return null; // Trả về null nếu lỗi
        }
    }
}