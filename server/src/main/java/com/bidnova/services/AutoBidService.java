package com.bidnova.services;

import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.dao.BidHistoryDAO;
import com.bidnova.dao.UserDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AutoBid;
import com.bidnova.models.BidHistory;
import com.bidnova.models.User;

/**
 * Service to handle automatic bidding logic
 */
public class AutoBidService {
    private final AutoBidDAO autoBidDAO = new AutoBidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidHistoryDAO bidHistoryDAO = new BidHistoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    /**
     * Execute auto bids when a new bid is placed
     * Checks all active auto-bids for the auction and places bids if conditions are met
     */
    public void executeAutoBids(String auctionId, double currentHighestBid) {
        try {
            List<AutoBid> autoBids = autoBidDAO.getActiveAutoBids(auctionId);
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) return;
            
            // Sort by creation time (FIFO) - first person to set auto-bid gets priority
            for (AutoBid autoBid : autoBids) {
                String currentLeader = auction.getHighestBidder();
                if (!isAutoBidValid(autoBid, currentHighestBid, currentLeader)) {
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
    private boolean isAutoBidValid(AutoBid autoBid, double currentBid, String currentLeader) {
        // Auto-bid must be active
        if (!autoBid.isActive()) {
            return false;
        }

        // Auto-bid max must be higher than current bid
        if (autoBid.getMaxBid() <= currentBid) {
            return false;
        }

        // Không tự đấu giá đè lên chính mình
        if (autoBid.getUsername().equals(currentLeader)) {
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
                if (!"OPEN".equals(auction.getStatus())) {
                    return;
                }

                // Place the bid
                if (bidAmount > auction.getCurrentHighestBid()) {
                    auction.setCurrentHighestBid(bidAmount);
                    auction.setHighestBidder(username);
                    
                    // Update database
                    auctionDAO.updateHighestBid(auctionId, bidAmount);
                    
                    // Ghi lại lịch sử đấu giá cho Auto-bid (Sử dụng trim để tránh lỗi khoảng trắng)
                    User user = userDAO.findByUsername(username.trim());
                    if (user != null) {
                        BidHistory history = new BidHistory(
                            auctionId,
                            user.getId(),
                            username.trim(),
                            bidAmount,
                            LocalDateTime.now()
                        );
                        bidHistoryDAO.addBid(history);
                    }
                    
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
