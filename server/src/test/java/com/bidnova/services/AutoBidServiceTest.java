package com.bidnova.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;

import com.bidnova.dao.*;
import com.bidnova.models.*;
import java.util.Arrays;

class AutoBidServiceTest {

    @Mock private AutoBidDAO autoBidDAO;
    @Mock private AuctionDAO auctionDAO;
    @Mock private UserDAO userDAO;
    @Mock private BidHistoryDAO bidHistoryDAO;
    @Mock private AuctionManager auctionManager;

    private AutoBidService autoBidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        autoBidService = new AutoBidService(autoBidDAO, auctionDAO, bidHistoryDAO, userDAO, auctionManager);
    }

    @Test
    void testExecuteAutoBids_NormalBid() {
        String auctionId = "AUC001";
        double currentBid = 10000.0;

        // Setup Mock Auction
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("OPEN");
        auction.setCurrentHighestBid(currentBid);
        auction.setMinBidIncrement(1000.0);

        // Setup Mock AutoBid
        AutoBid autoBid = new AutoBid();
        autoBid.setId(1);
        autoBid.setUserId(10);
        autoBid.setIncrement(2000.0);
        autoBid.setMaxBid(50000.0);
        autoBid.setActive(true);
        autoBid.setAuctionId(auctionId);

        // Setup Mock User
        User user = new User(10, "bidder1", "password", "BIDDER");

        when(autoBidDAO.getActiveAutoBids(auctionId)).thenReturn(Arrays.asList(autoBid));
        when(userDAO.findById(10)).thenReturn(user);
        when(userDAO.findByUsername("bidder1")).thenReturn(user);
        when(auctionManager.getAuction(auctionId)).thenReturn(auction);

        autoBidService.executeAutoBids(auctionId, currentBid);
    }

    @Test
    void testExecuteAutoBids_ReachCeilingPrice() {
        String auctionId = "AUC002";
        double ceilingPrice = 100000.0;
        double currentBid = 90000.0;

        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("OPEN");
        auction.setCurrentHighestBid(currentBid);
        auction.setPriceCeiling(ceilingPrice);
        auction.setMinBidIncrement(1000.0);

        AutoBid autoBid = new AutoBid();
        autoBid.setId(2);
        autoBid.setUserId(20);
        autoBid.setIncrement(20000.0); // 90k + 20k = 110k (Exceeds ceiling)
        autoBid.setMaxBid(200000.0);
        autoBid.setActive(true);

        User user = new User(20, "rich_guy", "password", "BIDDER");

        when(autoBidDAO.getActiveAutoBids(auctionId)).thenReturn(Arrays.asList(autoBid));
        when(userDAO.findById(ArgumentMatchers.anyInt())).thenReturn(user);
        when(userDAO.findByUsername(ArgumentMatchers.anyString())).thenReturn(user);
        when(auctionManager.getAuction(auctionId)).thenReturn(auction);

        autoBidService.executeAutoBids(auctionId, currentBid);

        // Kiểm tra xem auction có bị đóng khi chạm trần không
        verify(auctionDAO).updateStatus(auctionId, "FINISHED");
        // Kiểm tra xem số tiền thầu có được set đúng bằng giá trần không
        verify(auctionDAO).updateHighestBid(auctionId, ceilingPrice);
        // Kiểm tra xem auto-bid đã bị vô hiệu hóa chưa
        verify(autoBidDAO).deactivateAutoBid(ArgumentMatchers.anyInt());
    }

    @Test
    void testDeactivateAutoBid_Success() {
        AutoBid ab = new AutoBid();
        ab.setId(5);
        when(autoBidDAO.findByUserAndAuction(1, "A1")).thenReturn(ab);
        when(autoBidDAO.deactivateAutoBid(5)).thenReturn(true);

        assertTrue(autoBidService.deactivateAutoBid(1, "A1"));
    }

    @Test
    void testDeactivateAutoBid_NotFound() {
        when(autoBidDAO.findByUserAndAuction(1, "A1")).thenReturn(null);

        assertFalse(autoBidService.deactivateAutoBid(1, "A1"));
    }
}