package com.bidnova.services; // Bác nhớ check lại tên package cho chuẩn nhé

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import com.bidnova.dao.UserDAO;
import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.User;
import com.bidnova.network.Response;

public class ServerManager {
    private final UserDAO userDAO = new UserDAO();

    public Response handleLogin(String username, String password) {
        System.out.println("Client đang yêu cầu đăng nhập với tài khoản: " + username);
        
        // Gọi ống nước nối xuống DB
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            if (conn == null) {
                System.out.println("Lỗi: Không kết nối được DB!");
                return new Response("ERROR", "Lỗi máy chủ Database", null);
            }
            
            // 1. Kiểm tra tài khoản
            User user = userDAO.findByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                // 2. Pass đúng! Tạo chìa khóa (Token) mới
                String newToken = UUID.randomUUID().toString();
                
                // 3. LƯU CHÌA KHÓA VÀO BẢNG USERS TRONG DATABASE
                String updateSql = "UPDATE Users SET token = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newToken);
                updateStmt.setString(2, username);
                int rowAffected = updateStmt.executeUpdate();
                
                System.out.println("Đã cập nhật Token vào DB! Số dòng thay đổi: " + rowAffected);
                
                // 4. Gán token mới vào object user và trả về toàn bộ thông tin
                user.setToken(newToken);
                return new Response("SUCCESS", "Đăng nhập thành công", user);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("ERROR", "Lỗi máy chủ", null);
        }
        
        return new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
    }
}