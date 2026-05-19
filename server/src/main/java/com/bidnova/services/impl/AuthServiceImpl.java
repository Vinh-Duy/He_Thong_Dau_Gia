package com.bidnova.services.impl;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.AuthUserContext;
import com.bidnova.models.User;
import com.bidnova.services.AuthService;

public class AuthServiceImpl implements AuthService {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public AuthUserContext validateToken(String token) {
        if (token == null || token.isBlank()) return null;

        User user = userDAO.findByToken(token);
        if (user == null) return null;

        return new AuthUserContext(user.getId(), user.getUsername(), user.getRole());
    }
}