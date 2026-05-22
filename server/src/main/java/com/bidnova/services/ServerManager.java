package com.bidnova.services; // Bác nhớ check lại tên package cho chuẩn nhé

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.bidnova.dao.UserDAO;
import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.User;
import com.bidnova.network.Response;
import com.bidnova.utils.JwtUtil;

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
                // 2. Tạo JWT Token thực thụ
                String jwtToken = JwtUtil.generateToken(user);
                
                // Đóng gói dữ liệu trả về: gồm User và Token riêng biệt
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", jwtToken);
                
                return new Response("SUCCESS", "Đăng nhập thành công", data);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("ERROR", "Lỗi máy chủ", null);
        }
        
        return new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
    }
}