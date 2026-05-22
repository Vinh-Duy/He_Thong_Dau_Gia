package com.bidnova.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Nhớ import thư viện này để tạo Token

import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.User;

/** Class này phụ trách làm việc với database. */
public class UserDAO {
    private Connection conn;

    public UserDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    /** Lấy user theo tên người dùng. */
    public User findByUsername(String username) {
        // Lấy thông tin user theo tên người dùng
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
                
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("token")
                    );
                }
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
            System.out.println("Lỗi khi lưu token vào DB!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(String username, String password, String email, String fullName, String phone, String gender, String role) {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO users (username, password, email, full_name, phone, gender, role, token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.setString(6, gender);
            pstmt.setString(7, role);
            pstmt.setString(8, token);

            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (java.sql.SQLException e) {
            System.out.println("Lỗi khi đăng ký (Có thể do trùng Username): " + e.getMessage());
            return false;
        }
    }

    // 🔥 HÀM ĐÃ ĐƯỢC FIX ĐỂ LẤY DỮ LIỆU THẬT LÊN CHO BẢNG ADMIN 🔥
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Tạo đối tượng User bằng Constructor đầy đủ
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("full_name"), // Check lại tên cột trong DB của bác
                    rs.getString("phone"),
                    rs.getString("gender"),
                    rs.getString("role"),
                    rs.getString("token")
                );
                list.add(user);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách User: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    // Hàm mới: Dùng để lưu Token vào Database sau khi login thành công
    public void updateToken(String username, String token) {
        String sql = "UPDATE Users SET token = ? WHERE username = ?";
        // Lấy kết nối DB (Bác check xem file DB của bác tên gì thì sửa lại cho đúng nhé)
        try (java.sql.Connection conn = com.bidnova.database.DatabaseConnection.getConnection();
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

    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("token")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tìm user theo id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public User findByToken(String token) {
        String sql = "SELECT * FROM users WHERE token = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("token")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}