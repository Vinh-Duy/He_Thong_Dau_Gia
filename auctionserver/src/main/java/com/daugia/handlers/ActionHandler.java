package com.daugia.handlers;

import com.daugia.network.Request;
import com.daugia.network.Response;

public interface ActionHandler {
    Response handle(Request request);
}