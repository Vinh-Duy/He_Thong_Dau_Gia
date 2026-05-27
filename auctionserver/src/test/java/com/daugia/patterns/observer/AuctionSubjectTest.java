package com.daugia.patterns.observer;

import com.daugia.models.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Observer Pattern - AuctionSubject
 * 
 * @author Testing Team
 */
public class AuctionSubjectTest {
    
    private AuctionSubject auctionSubject;
    private Auction auction;
    private TestAuctionObserver testObserver;
    
    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setId("A001");
        auction.setProductName("Test Product");
        auction.setStartPrice(1000.0);
        auction.setCurrentHighestBid(0.0);
        auction.setStatus("OPEN");
        
        auctionSubject = new AuctionSubject(auction);
        testObserver = new TestAuctionObserver();
    }
    
    @Test
    void testAttachObserver() {
        // Act
        auctionSubject.attach(testObserver);
        
        // Assert
        assertEquals(1, auctionSubject.getObserverCount(), "Should have 1 observer");
    }
    
    @Test
    void testDetachObserver() {
        // Arrange
        auctionSubject.attach(testObserver);
        
        // Act
        auctionSubject.detach(testObserver);
        
        // Assert
        assertEquals(0, auctionSubject.getObserverCount(), "Should have 0 observers");
    }
    
    @Test
    void testNotifyBidPlaced() {
        // Arrange
        auctionSubject.attach(testObserver);
        
        // Act
        auctionSubject.notifyBidPlaced(1500.0, "user1");
        
        // Assert
        assertTrue(testObserver.isBidPlacedCalled(), "onBidPlaced should be called");
        assertEquals(1500.0, testObserver.getLastBidAmount(), 0.001);
        assertEquals("user1", testObserver.getLastBidder());
    }
    
    @Test
    void testNotifyStatusChanged() {
        // Arrange
        auctionSubject.attach(testObserver);
        
        // Act
        auctionSubject.notifyStatusChanged("OPEN", "CLOSED");
        
        // Assert
        assertTrue(testObserver.isStatusChangedCalled(), "onAuctionStatusChanged should be called");
        assertEquals("OPEN", testObserver.getOldStatus());
        assertEquals("CLOSED", testObserver.getNewStatus());
    }
    
    @Test
    void testNotifyAutoBidTriggered() {
        // Arrange
        auctionSubject.attach(testObserver);
        
        // Act
        auctionSubject.notifyAutoBidTriggered(2000.0, "autoUser");
        
        // Assert
        assertTrue(testObserver.isAutoBidTriggeredCalled(), "onAutoBidTriggered should be called");
        assertEquals(2000.0, testObserver.getLastAutoBidAmount(), 0.001);
        assertEquals("autoUser", testObserver.getLastAutoBidUser());
    }
    
    @Test
    void testMultipleObservers() {
        // Arrange
        TestAuctionObserver observer2 = new TestAuctionObserver();
        auctionSubject.attach(testObserver);
        auctionSubject.attach(observer2);
        
        // Act
        auctionSubject.notifyBidPlaced(2500.0, "user2");
        
        // Assert
        assertTrue(testObserver.isBidPlacedCalled(), "First observer should be notified");
        assertTrue(observer2.isBidPlacedCalled(), "Second observer should be notified");
        assertEquals(2, auctionSubject.getObserverCount(), "Should have 2 observers");
    }
    
    @Test
    void testClearObservers() {
        // Arrange
        auctionSubject.attach(testObserver);
        
        // Act
        auctionSubject.clearObservers();
        
        // Assert
        assertEquals(0, auctionSubject.getObserverCount(), "Should have 0 observers after clear");
    }
    
    /**
     * Test implementation of AuctionObserver for testing
     */
    private static class TestAuctionObserver implements AuctionObserver {
        private boolean bidPlacedCalled = false;
        private boolean statusChangedCalled = false;
        private boolean autoBidTriggeredCalled = false;
        private double lastBidAmount = 0;
        private String lastBidder = "";
        private String oldStatus = "";
        private String newStatus = "";
        private double lastAutoBidAmount = 0;
        private String lastAutoBidUser = "";
        
        @Override
        public void onBidPlaced(Auction auction, double newBid, String bidder) {
            bidPlacedCalled = true;
            lastBidAmount = newBid;
            lastBidder = bidder;
        }
        
        @Override
        public void onAuctionStatusChanged(Auction auction, String oldStatus, String newStatus) {
            statusChangedCalled = true;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
        }
        
        @Override
        public void onAutoBidTriggered(Auction auction, double autoBidAmount, String username) {
            autoBidTriggeredCalled = true;
            lastAutoBidAmount = autoBidAmount;
            lastAutoBidUser = username;
        }
        
        public boolean isBidPlacedCalled() { return bidPlacedCalled; }
        public boolean isStatusChangedCalled() { return statusChangedCalled; }
        public boolean isAutoBidTriggeredCalled() { return autoBidTriggeredCalled; }
        public double getLastBidAmount() { return lastBidAmount; }
        public String getLastBidder() { return lastBidder; }
        public String getOldStatus() { return oldStatus; }
        public String getNewStatus() { return newStatus; }
        public double getLastAutoBidAmount() { return lastAutoBidAmount; }
        public String getLastAutoBidUser() { return lastAutoBidUser; }
    }
}
