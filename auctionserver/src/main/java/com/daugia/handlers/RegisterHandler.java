package com.daugia.handlers;

import com.daugia.dao.UserDAO;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RegisterHandler implements ActionHandler {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            JsonObject regData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String newUsername = regData.get("username").getAsString();
            String newPassword = regData.get("password").getAsString();
            String email = regData.has("email") ? regData.get("email").getAsString() : "";
            String fullName = regData.has("fullName") ? regData.get("fullName").getAsString() : "";
            String phone = regData.has("phone") ? regData.get("phone").getAsString() : "";
            String gender = regData.has("gender") ? regData.get("gender").getAsString() : "";
            String role = regData.get("role").getAsString();

            boolean isRegistered = userDAO.registerUser(
                    newUsername, newPassword, email, fullName, phone, gender, role
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
