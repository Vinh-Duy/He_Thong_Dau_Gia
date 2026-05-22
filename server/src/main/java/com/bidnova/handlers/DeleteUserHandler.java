package com.bidnova.handlers;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.User;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handler for deleting a user.
 */
public class DeleteUserHandler implements ActionHandler {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Handles the delete user request.
     *
     * @param request The request containing the user ID to delete.
     * @param authUser The authenticated user context.
     * @return The response indicating the result of the operation.
     */
    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject payload = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            int userId = payload.get("userId").getAsInt();

            // Không cho xóa chính mình
            if (authUser.getUserId() == userId) {
                return new Response("ERROR", "Không thể tự xóa tài khoản của chính mình!", null);
            }

            // Kiểm tra user cần xóa có phải admin không
            User targetUser = userDAO.findById(userId);
            if (targetUser != null && "ADMIN".equalsIgnoreCase(targetUser.getRole())) {
                return new Response("ERROR", "Không thể xóa tài khoản admin!", null);
            }

            boolean success = userDAO.deleteUser(userId);
            if (success) {
                return new Response("SUCCESS", "Đã xóa người dùng", null);
            } else {
                return new Response("ERROR", "Không thể xóa người dùng", null);
            }
        } catch (Exception e) {
            return new Response("ERROR", "Lỗi xóa user: " + e.getMessage(), null);
        }
    }
}
