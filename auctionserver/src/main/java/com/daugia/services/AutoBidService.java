package com.daugia.services;

import java.util.List;

import com.daugia.dao.AuctionDAO;
import com.daugia.dao.AutoBidDAO;
import com.daugia.models.Auction;
import com.daugia.models.AutoBid;

/**
 * Service to handle automatic bidding logic
 */
public class AutoBidService {
    private final AutoBidDAO autoBidDAO = new AutoBidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    /**
     * Execute auto bids when a new bid is placed
     * Checks all active auto-bids for the auction and places bids if conditions are met
     */
    public void executeAutoBids(String auctionId, double currentHighestBid) {
        try {
            List<AutoBid> autoBids = autoBidDAO.getActiveAutoBids(auctionId);
            
            // Sort by creation time (FIFO) - first person to set auto-bid gets priority
            for (AutoBid autoBid : autoBids) {
                if (!isAutoBidValid(autoBid, currentHighestBid)) {
                    continue;
                }

                // Calculate next bid amount
                double nextBidAmount = currentHighestBid + autoBid.getIncrement();

                // Check if next bid is within max limit
                if (nextBidAmount <= autoBid.getMaxBid()) {
                    // Place the auto bid
                    placeAutoBidOnAuction(auctionId, autoBid.getUsername(), nextBidAmount);
                    System.out.println("✓ Auto-bid placed: " + autoBid.getUsername() + " bid " + nextBidAmount);
                    
                    // Update current highest bid for next auto-bid check
                    currentHighestBid = nextBidAmount;
                } else {
                    // Max bid reached - deactivate this auto-bid
                    autoBidDAO.deactivateAutoBid(autoBid.getId());
                    System.out.println("⊘ Auto-bid deactivated (max bid reached): " + autoBid.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing auto-bids: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validate if auto-bid should be executed
     */
    private boolean isAutoBidValid(AutoBid autoBid, double currentBid) {
        // Auto-bid must be active
        if (!autoBid.isActive()) {
            return false;
        }

        // Auto-bid max must be higher than current bid
        if (autoBid.getMaxBid() <= currentBid) {
            return false;
        }

        return true;
    }

    /**
     * Place an automatic bid on the auction
     */
    private synchronized void placeAutoBidOnAuction(String auctionId, String username, double bidAmount) {
        try {
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) {
                System.err.println("Auction not found: " + auctionId);
                return;
            }

            synchronized (auction) {
                // Verify auction is still active
                if (!"RUNNING".equals(auction.getStatus()) && !"OPEN".equals(auction.getStatus())) {
                    return;
                }

                // Place the bid
                if (bidAmount > auction.getCurrentHighestBid()) {
                    auction.setCurrentHighestBid(bidAmount);
                    auction.setHighestBidder(username);
                    
                    // Update database
                    auctionDAO.updateHighestBid(auctionId, bidAmount);
                    
                    System.out.println("Auto-bid executed: " + username + " -> " + bidAmount);
                } else {
                    System.out.println("Auto-bid skipped (bid amount not higher): " + bidAmount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error placing auto-bid: " + e.getMessage());
        }
    }

    /**
     * Deactivate auto-bid for a user on an auction
     */
    public boolean deactivateAutoBid(String username, String auctionId) {
        try {
            AutoBid autoBid = autoBidDAO.findByUserAndAuction(username, auctionId);
            if (autoBid != null) {
                return autoBidDAO.deactivateAutoBid(autoBid.getId());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deactivating auto-bid: " + e.getMessage());
            return false;
        }
    }
}
