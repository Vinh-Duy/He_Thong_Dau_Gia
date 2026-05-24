package com.bidnova.handlers;

import org.mindrot.jbcrypt.BCrypt;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Class này là handler để xử lý các yêu cầu đăng ký tài khoản. */
public class RegisterHandler implements ActionHandler {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Xử lý yêu cầu đăng ký tài khoản.
     * 
     * @param request Yêu cầu chứa username, password, email, fullName, phone, gender, role.
     * @param authUser Thông tin người dùng đã xác thực, dùng để kiểm tra quyền và xác định sellerId nếu là seller.
     * @return Phản hồi với status "SUCCESS" nếu đăng ký thành công, hoặc "ERROR" nếu có lỗi. Message sẽ chứa thông tin chi tiết về kết quả.
     */
    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            JsonObject regData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String username = regData.get("username").getAsString();
            
            // Hash password
            String password = regData.get("password").getAsString();
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String email = regData.has("email") ? regData.get("email").getAsString() : "";
            String fullName = regData.has("fullName") ? regData.get("fullName").getAsString() : "";
            String phone = regData.has("phone") ? regData.get("phone").getAsString() : "";
            String gender = regData.has("gender") ? regData.get("gender").getAsString() : "";
            String role = regData.get("role").getAsString();

            boolean isRegistered = userDAO.registerUser(
                    username, hashedPassword, email, fullName, phone, gender, role
            );

            if (isRegistered) {
                return new Response("SUCCESS", "Đăng ký tài khoản thành công!", null);
            } else {
                return new Response("ERROR", "Tên đăng nhập đã tồn tại hoặc lỗi hệ thống!", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "Dữ liệu đăng ký không hợp lệ!", null);
        }
    }
}
