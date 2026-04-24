package com.daugia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.daugia.database.DatabaseConnection;
import com.daugia.models.User;

public class UserDAO {
    private Connection conn;

    public UserDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            return false; 
        }
    }

    // Hàm mới: Dùng để lưu Token vào Database sau khi login thành công
    public void updateToken(String username, String token) {
        String sql = "UPDATE Users SET token = ? WHERE username = ?";
        // Lấy kết nối DB (Bác check xem file DB của bác tên gì thì sửa lại cho đúng nhé)
        try (java.sql.Connection conn = com.daugia.database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Đã lưu Token thành công cho user: " + username);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu token vào DB!");
            e.printStackTrace();
        }
    }

    // Thêm hàm này vào UserDAO.java
    public boolean registerUser(String username, String password, String email, String fullName, String phone, String gender) {
        // Cụm SQL Insert cơ bản (Bác nhớ sửa tên cột cho khớp với Database của bác nhé)
        String sql = "INSERT INTO users (username, password, email, full_name, phone,gender, role) VALUES (?, ?, ?, ?, ?, ?,'USER')";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.setString(6, gender);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Nếu insert thành công sẽ trả về true
            
        } catch (java.sql.SQLException e) {
            System.out.println("Lỗi khi đăng ký (Có thể do trùng Username): " + e.getMessage());
            return false;
        }
    }

    
}