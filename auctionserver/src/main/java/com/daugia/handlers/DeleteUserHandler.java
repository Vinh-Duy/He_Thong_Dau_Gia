package com.daugia.handlers;

import com.daugia.dao.UserDAO;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeleteUserHandler implements ActionHandler {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject payload = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            int userId = payload.get("userId").getAsInt();

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
