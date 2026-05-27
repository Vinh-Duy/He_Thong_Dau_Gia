package com.daugia.handlers;

/**
 * XỬ LÝ VIỆC XÓA TÀI KHOẢN NGƯỜI DÙNG (ADMIN).
 *
 * Luồng xử lý:
 * 1. Admin chọn 1 user trong bảng, bấm "Xóa người dùng".
 * 2. Client gửi request action="DELETE_USER" với {userId}.
 * 3. Server kiểm tra admin đã đăng nhập chưa (authUser != null).
 * 4. Gọi UserDAO.deleteUser(userId) để xóa khỏi database.
 * 5. Trả về SUCCESS hoặc ERROR.
 */

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
