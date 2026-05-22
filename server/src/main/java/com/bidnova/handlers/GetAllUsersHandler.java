package com.bidnova.handlers;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.User;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for getting all users.
 */
public class GetAllUsersHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Handles the get all users request.
     *
     * @param request The request containing no payload.
     * @param authUser The authenticated user context.
     * @return The response containing the list of users.
     */
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
            // Lọc bỏ các tài khoản admin khỏi danh sách hiển thị
            // tránh trường hợp admin xóa nhầm admin khác hoặc chính mình
            List<User> filtered = new ArrayList<>();
            for (User user : danhSachUser) {
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    filtered.add(user);
                }
            }
            String payloadUsers = gson.toJson(filtered);
            return new Response("SUCCESS", "Lấy danh sách người dùng thành công", payloadUsers);

        } catch (Exception e) {
            return new Response("ERROR", "Lỗi khi lấy danh sách user: " + e.getMessage(), null);
        }
    }
}