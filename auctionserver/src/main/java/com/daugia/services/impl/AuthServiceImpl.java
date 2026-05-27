package com.daugia.services.impl;

import com.daugia.dao.UserDAO;
import com.daugia.models.AuthUserContext;
import com.daugia.models.User;
import com.daugia.services.AuthService;

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