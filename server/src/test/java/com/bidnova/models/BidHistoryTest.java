package com.bidnova.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class BidHistoryTest {

    @Test
    void testConstructorWithoutId() {
        LocalDateTime now = LocalDateTime.now();
        BidHistory bidHistory = new BidHistory("AUC001", 10, "testuser", 50000.0, now);

        assertEquals("AUC001", bidHistory.getAuctionId());
        assertEquals(10, bidHistory.getUserId());
        assertEquals("testuser", bidHistory.getUsername());
        assertEquals(50000.0, bidHistory.getBidAmount());
        assertEquals(now, bidHistory.getBidTime());
    }

    @Test
    void testConstructorWithId() {
        LocalDateTime now = LocalDateTime.now();
        BidHistory bidHistory = new BidHistory(1, "AUC001", 10, "testuser", 50000.0, now);

        assertEquals(1, bidHistory.getId());
        assertEquals("AUC001", bidHistory.getAuctionId());
        assertEquals(10, bidHistory.getUserId());
        assertEquals("testuser", bidHistory.getUsername());
        assertEquals(50000.0, bidHistory.getBidAmount());
        assertEquals(now, bidHistory.getBidTime());
    }

    @Test
    void testGetters() {
        LocalDateTime now = LocalDateTime.now();
        BidHistory bidHistory = new BidHistory("AUC001", 10, "testuser", 50000.0, now);

        assertEquals("AUC001", bidHistory.getAuctionId());
        assertEquals(10, bidHistory.getUserId());
        assertEquals("testuser", bidHistory.getUsername());
        assertEquals(50000.0, bidHistory.getBidAmount());
        assertEquals(now, bidHistory.getBidTime());
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        BidHistory bidHistory = new BidHistory(1, "AUC001", 10, "testuser", 50000.0, now);

        String toString = bidHistory.toString();
        assertTrue(toString.contains("AUC001"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("50000.0"));
    }
}
