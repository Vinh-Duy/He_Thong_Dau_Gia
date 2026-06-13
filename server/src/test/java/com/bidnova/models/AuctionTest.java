package com.bidnova.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for Auction Model
 * 
 * @author Testing Team
 */
public class AuctionTest {
    
    private Auction auction;
    
    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setId("A001");
        auction.setProductName("Test Product");
        auction.setStartPrice(1000.0);
        auction.setCurrentHighestBid(0.0);
        auction.setStatus("OPEN");
    }
    
    @Test
    void testAuctionInitialization() {
        assertEquals("OPEN", auction.getStatus(), "Default status should be OPEN");
        assertEquals(0.0, auction.getCurrentHighestBid(), 0.001);
        assertEquals(1000.0, auction.getMinBidIncrement(), 0.001);
        assertNull(auction.getHighestBidder());
    }
    
    @Test
    void testIsBidAtCeiling() {
        // Arrange & Act
        auction.setPriceCeiling(5000.0);

        // Act & Assert
        assertFalse(auction.isBidAtCeiling(4000.0), "Giá thấp hơn trần phải trả về false");
        assertTrue(auction.isBidAtCeiling(5000.0), "Giá bằng trần phải trả về true");
        assertTrue(auction.isBidAtCeiling(6000.0), "Giá vượt trần phải trả về true");

        auction.setPriceCeiling(null);
        assertFalse(auction.isBidAtCeiling(10000.0), "Không có giá trần thì luôn trả về false");
    }

    @Test
    void testIsBidIncrementValid() {
        // Arrange
        auction.setCurrentHighestBid(2000.0);
        auction.setMinBidIncrement(500.0);

        // Act & Assert
        assertTrue(auction.isBidIncrementValid(2500.0), "Đúng bước giá tối thiểu phải hợp lệ");
        assertTrue(auction.isBidIncrementValid(3000.0), "Bước giá lớn hơn tối thiểu phải hợp lệ");
        assertFalse(auction.isBidIncrementValid(2400.0), "Bước giá nhỏ hơn tối thiểu phải không hợp lệ");
        assertFalse(auction.isBidIncrementValid(1500.0), "Giá đặt thấp hơn giá hiện tại phải không hợp lệ");
    }

    @Test
    void testSettersAndGetters() {
        auction.setCategory("Vehicle");
        auction.setDescription("Xe hơi cực đẹp");
        auction.setSellerId(123);
        auction.setImageUrl("http://image.com/car.png");
        auction.setHighestBidder("vinhduy");

        assertEquals("Vehicle", auction.getCategory());
        assertEquals("Xe hơi cực đẹp", auction.getDescription());
        assertEquals(123, auction.getSellerId());
        assertEquals("http://image.com/car.png", auction.getImageUrl());
        assertEquals("vinhduy", auction.getHighestBidder());
    }
}
