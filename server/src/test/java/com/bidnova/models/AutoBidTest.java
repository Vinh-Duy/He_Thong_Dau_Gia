package com.bidnova.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AutoBidTest {

    @Test
    void testConstructor() {
        AutoBid autoBid = new AutoBid();
        assertTrue(autoBid.isActive());
        assertNotNull(autoBid.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        AutoBid autoBid = new AutoBid();
        autoBid.setId(1);
        autoBid.setUserId(10);
        autoBid.setAuctionId("AUC001");
        autoBid.setMaxBid(100000.0);
        autoBid.setIncrement(5000.0);
        autoBid.setActive(false);

        assertEquals(1, autoBid.getId());
        assertEquals(10, autoBid.getUserId());
        assertEquals("AUC001", autoBid.getAuctionId());
        assertEquals(100000.0, autoBid.getMaxBid());
        assertEquals(5000.0, autoBid.getIncrement());
        assertFalse(autoBid.isActive());
    }

    @Test
    void testSetActive() {
        AutoBid autoBid = new AutoBid();
        autoBid.setActive(true);
        assertTrue(autoBid.isActive());

        autoBid.setActive(false);
        assertFalse(autoBid.isActive());
    }
}
