package com.bidnova.network;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ResponseTest {

    @Test
    void testConstructorSuccess() {
        Response response = new Response("SUCCESS", "Success message", null);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Success message", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testConstructorFailure() {
        Response response = new Response("ERROR", "Error message", null);
        assertEquals("ERROR", response.getStatus());
        assertEquals("Error message", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testConstructorWithData() {
        Response response = new Response("SUCCESS", "Data retrieved", "{\"key\":\"value\"}");
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Data retrieved", response.getMessage());
        assertEquals("{\"key\":\"value\"}", response.getData());
    }

    @Test
    void testGetPayload() {
        Response response = new Response("SUCCESS", "Test", "{\"test\":true}");
        assertEquals("{\"test\":true}", response.getPayload());
    }
}
