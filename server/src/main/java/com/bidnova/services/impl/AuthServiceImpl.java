package com.bidnova.services.impl;

import com.bidnova.models.AuthUserContext;
import com.bidnova.services.AuthService;
import com.bidnova.utils.JwtUtil;
import io.jsonwebtoken.Claims;

public class AuthServiceImpl implements AuthService {
    // Không cần UserDAO nữa vì JWT là stateless, thông tin nằm trong token
    // private final UserDAO userDAO = new UserDAO();

    @Override
    public AuthUserContext validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Claims claims = JwtUtil.validateToken(token);
            if (claims == null) {
                return null; // Token không hợp lệ hoặc đã hết hạn
            }
            // Lấy thông tin từ Claims
            int userId = claims.get("userId", Integer.class);
            String username = claims.getSubject(); // Subject thường là username
            String role = claims.get("role", String.class);
            return new AuthUserContext(userId, username, role);
        } catch (Exception e) {
            System.err.println("Lỗi khi xác thực JWT: " + e.getMessage());
            return null; // Trả về null nếu có lỗi trong quá trình giải mã/xác thực
        }
    }
}