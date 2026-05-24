package com.bidnova.services;

import com.bidnova.models.AuthUserContext;

public interface AuthService {
    AuthUserContext validateToken(String token);
}