package com.daugia.services; // Bác nhớ check lại tên package cho chuẩn nhé

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import com.daugia.database.DatabaseConnection;
import com.daugia.network.Response; // Import cái file nối DB của bác vào

public class ServerManager {

    public Response handleLogin(String username, String password) {
        System.out.println("Client đang yêu cầu đăng nhập với tài khoản: " + username);
        
        // Gọi ống nước nối xuống DB
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            if (conn == null) {
                System.out.println("Lỗi: Không kết nối được DB!");
                return new Response("ERROR", "Lỗi máy chủ Database", null);
            }
            
            // 1. Soi xem DB có tài khoản này không
            String checkSql = "SELECT id FROM Users WHERE username = ? AND password = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            checkStmt.setString(2, password);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // 2. Pass đúng! Tạo chìa khóa (Token) mới
                String newToken = UUID.randomUUID().toString();
                
                // 3. LƯU CHÌA KHÓA VÀO BẢNG USERS TRONG DATABASE
                String updateSql = "UPDATE Users SET token = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newToken);
                updateStmt.setString(2, username);
                int rowAffected = updateStmt.executeUpdate();
                
                System.out.println("Đã cập nhật Token vào DB! Số dòng thay đổi: " + rowAffected);
                
                // 4. Trả kết quả về cho Client
                return new Response("SUCCESS", "Đăng nhập thành công", newToken);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("ERROR", "Lỗi máy chủ", null);
        }
        
        return new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
    }
}