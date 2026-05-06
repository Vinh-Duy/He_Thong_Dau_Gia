package com.daugia.services.impl;

import java.util.List;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;
import com.daugia.services.AuctionService;

public class AuctionServiceImpl implements AuctionService {
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public List<Auction> getAuctionsBySellerId(int sellerId) {
        return auctionDAO.getAuctionsBySellerId(sellerId);
    }

    @Override
    public boolean updateProduct(Auction auction, int requesterUserId, String requesterRole) {
        // TODO: role check chặt hơn ở phase sau
        if (auction == null || auction.getId() == null) return false;
        return auctionDAO.updateAuction(auction);
    }

    @Override
    public boolean deleteProduct(String auctionId, int requesterUserId, String requesterRole) {
        if (auctionId == null || auctionId.isBlank()) return false;
        return auctionDAO.deleteAuction(auctionId);
    }
}
