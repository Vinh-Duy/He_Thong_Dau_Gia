package com.bidnova.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;

import com.bidnova.models.User;
import com.bidnova.utils.DatabaseConnection;

public class UserDAO {
    // Hàm đăng ký người dùng mới khi họ đăng ký tài khoản
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, email, full_name, phone, gender, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()); // Mã hóa mật khẩu trước khi lưu vào database

                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, hashedPassword); // Lưu mật khẩu đã được mã hóa vào database
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getFullName());
                pstmt.setString(5, user.getPhone());
                pstmt.setString(6, user.getGender());
                pstmt.setString(7, user.getRole());
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0; // Trả về true nếu có ít nhất 1 dòng bị ảnh hưởng
        } catch (Exception e) {
            System.out.println("Lỗi khi đăng ký người dùng!");
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi
        }
    }

    // Hàm đăng nhập
    // tư tưởng: khi người dùng đăng nhập, chúng ta sẽ lấy thông tin người dùng từ database dựa trên username, sau đó so sánh mật khẩu đã mã hóa trong database với mật khẩu mà người dùng nhập vào (sau khi đã mã hóa) bằng cách sử dụng BCrypt.checkpw()
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery(); // Sử dụng var để tự động xác định kiểu ResultSet
            // Nếu tìm thấy người dùng với username và password đúng, trả về đối tượng User
            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("password");

                // Sử dụng BCrypt để so sánh mật khẩu đã mã hóa trong database với mật khẩu mà người dùng nhập vào (sau khi đã mã hóa)
                if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    // KHÔNG setPassword vào object trả về để bảo mật
                    user.setRole(rs.getString("role"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setGender(rs.getString("gender"));
                    return user;
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi đăng nhập!");
            e.printStackTrace();
        }
        return null; // Trả về null nếu đăng nhập thất bại
    }
}
