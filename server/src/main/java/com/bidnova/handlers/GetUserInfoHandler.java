package com.bidnova.handlers;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.User;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.Gson;

public class GetUserInfoHandler implements ActionHandler {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            User user = userDAO.findById(authUser.getUserId());
            if (user == null) {
                return new Response("ERROR", "User not found", null);
            }

            return new Response("SUCCESS", "User info", gson.toJson(user));
        } catch (Exception e) {
            return new Response("ERROR", "GET_USER_INFO error: " + e.getMessage(), null);
        }
    }
}