package com.daugia.services;

import com.daugia.models.AuthUserContext;

public interface AuthService {
    AuthUserContext validateToken(String token);
}