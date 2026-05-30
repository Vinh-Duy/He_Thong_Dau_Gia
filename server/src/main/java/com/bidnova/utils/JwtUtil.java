package com.bidnova.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import com.bidnova.models.User;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    // Cấu hình ignoreIfMissing để không bị crash nếu thiếu file .env
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    
    // Cung cấp một key mặc định hoặc kiểm tra null để tránh lỗi getBytes()
    private static final String SECRET = dotenv.get("JWT_SECRET", "default_secret_key_for_dev_only_1234567890");
    
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày tính bằng ms

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}