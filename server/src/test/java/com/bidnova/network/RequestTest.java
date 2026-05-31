package com.bidnova.network;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RequestTest {

    @Test
    void testConstructorAndGetters() {
        Request request = new Request("LOGIN", "test-payload");
        assertEquals("LOGIN", request.getAction());
        assertEquals("test-payload", request.getPayload());
        assertNull(request.getToken());
    }

    @Test
    void testSetToken() {
        Request request = new Request("BID", "place-bid");
        request.setToken("test-token-123");

        assertEquals("test-token-123", request.getToken());
    }

    @Test
    void testConstructorWithPayload() {
        Request request = new Request("REGISTER", "{\"username\":\"test\",\"password\":\"123\"}");
        assertEquals("REGISTER", request.getAction());
        assertEquals("{\"username\":\"test\",\"password\":\"123\"}", request.getPayload());
    }
}
