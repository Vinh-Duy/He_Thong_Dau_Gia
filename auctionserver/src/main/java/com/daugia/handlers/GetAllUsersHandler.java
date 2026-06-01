package com.daugia.handlers;

import com.daugia.dao.UserDAO;
import com.daugia.models.AuthUserContext;
import com.daugia.models.User;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.Gson;

import java.util.List;

public class GetAllUsersHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            String role = authUser.getRole();
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return new Response("ERROR", "Forbidden: Chỉ ADMIN được xem danh sách users", null);
            }

            List<User> danhSachUser = userDAO.getAllUsers();
            String payloadUsers = gson.toJson(danhSachUser);
            return new Response("SUCCESS", "Lấy danh sách người dùng thành công", payloadUsers);

        } catch (Exception e) {
            return new Response("ERROR", "Lỗi khi lấy danh sách user: " + e.getMessage(), null);
        }
    }
}
