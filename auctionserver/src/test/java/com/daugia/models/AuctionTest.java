package com.daugia.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    void testPlaceBid_Success() {
        // Arrange
        String bidder = "user1";
        double bidAmount = 1500.0;
        
        // Act
        boolean result = auction.placeBid(bidder, bidAmount);
        
        // Assert
        assertTrue(result, "Bid should be placed successfully");
        assertEquals(1500.0, auction.getCurrentHighestBid(), 0.001);
        assertEquals("user1", auction.getHighestBidder());
    }
    
    @Test
    void testPlaceBid_Fail_LowerThanCurrent() {
        // Arrange
        auction.setCurrentHighestBid(2000.0);
        
        // Act
        boolean result = auction.placeBid("user2", 1500.0);
        
        // Assert
        assertFalse(result, "Bid should fail when lower than current");
        assertEquals(2000.0, auction.getCurrentHighestBid(), 0.001);
    }
    
    @Test
    void testPlaceBid_Fail_ClosedAuction() {
        // Arrange
        auction.setStatus("CLOSED");
        
        // Act
        boolean result = auction.placeBid("user3", 3000.0);
        
        // Assert
        assertFalse(result, "Bid should fail for closed auction");
    }
    
    @Test
    void testPlaceBid_Fail_EqualToCurrent() {
        // Arrange
        auction.setCurrentHighestBid(1500.0);
        
        // Act
        boolean result = auction.placeBid("user4", 1500.0);
        
        // Assert
        assertFalse(result, "Bid should fail when equal to current");
    }
    
    @Test
    void testAuctionInitialization() {
        // Arrange
        Auction newAuction = new Auction();
        
        // Assert
        assertEquals("OPEN", newAuction.getStatus(), "Default status should be OPEN");
        assertEquals(0.0, newAuction.getCurrentHighestBid(), 0.001);
        assertNull(newAuction.getHighestBidder());
    }
    
    @Test
    void testMultipleBids() {
        // Arrange & Act
        auction.placeBid("user1", 1200.0);
        auction.placeBid("user2", 1500.0);
        boolean finalBid = auction.placeBid("user3", 1300.0); // Lower than current
        
        // Assert
        assertEquals(1500.0, auction.getCurrentHighestBid(), 0.001);
        assertEquals("user2", auction.getHighestBidder());
        assertFalse(finalBid, "Lower bid should fail");
    }
}
