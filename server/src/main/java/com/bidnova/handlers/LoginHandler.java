package com.bidnova.handlers;

import org.mindrot.jbcrypt.BCrypt;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.User;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.JwtUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

/** Class này là handler để xử lý các yêu cầu đăng nhập. */
public class LoginHandler implements ActionHandler {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Xử lý yêu cầu đăng nhập.
     * 
     * @param request Yêu cầu chứa username và password.
     * @param authUser Thông tin người dùng đã xác thực, dùng để kiểm tra quyền và xác định sellerId nếu là seller.
     * @return Phản hồi với status "SUCCESS" nếu đăng nhập thành công, hoặc "ERROR" nếu có lỗi. Message sẽ chứa thông tin chi tiết về kết quả.
     */
    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            JsonObject loginData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String username = loginData.get("username").getAsString();
            String password = loginData.get("password").getAsString();

            // 1. Tìm user theo tên người dùng
            User user = userDAO.findByUsername(username);

            // 2. Kiểm tra mật khẩu
            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                // Nếu đăng nhập thành công, tạo JWT Token
                String token = JwtUtil.generateToken(user);
                
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", token);

                return new Response("SUCCESS", "Đăng nhập thành công", data);
            } else {
                // Sai tài khoản hoặc mật khẩu (lúc này nên báo chung để bảo mật)
                return new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "Dữ liệu đăng nhập không hợp lệ", null);
        }
    }
}
