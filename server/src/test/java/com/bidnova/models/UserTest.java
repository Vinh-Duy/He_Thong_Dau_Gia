package com.bidnova.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void testConstructor_Basic() {
        User user = new User(1, "testuser", "password", "BIDDER");
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("BIDDER", user.getRole());
    }

    @Test
    void testConstructor_Full() {
        User user = new User(1, "testuser", "password", "test@example.com", 
                           "Test User", "1234567890", "Male", "BIDDER");
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("1234567890", user.getPhone());
        assertEquals("Male", user.getGender());
        assertEquals("BIDDER", user.getRole());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User(0, "", "", "");
        user.setId(100);
        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setEmail("new@example.com");
        user.setFullName("New User");
        user.setPhone("9876543210");
        user.setGender("Female");
        user.setRole("ADMIN");

        assertEquals(100, user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("New User", user.getFullName());
        assertEquals("9876543210", user.getPhone());
        assertEquals("Female", user.getGender());
        assertEquals("ADMIN", user.getRole());
    }
}
