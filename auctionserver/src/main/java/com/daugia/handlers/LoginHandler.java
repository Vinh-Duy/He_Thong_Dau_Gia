package com.daugia.handlers;

import com.daugia.dao.UserDAO;
import com.daugia.models.AuthUserContext;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoginHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            JsonObject loginData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String username = loginData.get("username").getAsString();
            String password = loginData.get("password").getAsString();

            User userDaLogin = userDAO.checkLogin(username, password);
            if (userDaLogin != null) {
                String userJsonPayload = gson.toJson(userDaLogin);
                return new Response("SUCCESS", "Đăng nhập thành công", userJsonPayload);
            } else {
                return new Response("ERROR", "Sai tài khoản hoặc mật khẩu", null);
            }

        } catch (Exception e) {
            return new Response("ERROR", "Dữ liệu đăng nhập không hợp lệ", null);
        }
    }
}
