package com.bidnova.handlers;

import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;

public interface ActionHandler {
    Response handle(Request request, AuthUserContext authUser);
}