package com.bidnova.services.impl;

import java.util.List;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.services.AuctionService;

public class AuctionServiceImpl implements AuctionService {
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    public List<Auction> getAuctionsBySellerId(int sellerId) {
        return auctionDAO.getAuctionsBySellerId(sellerId);
    }

    @Override
    public boolean canModify(String auctionId, int requesterUserId, String requesterRole) {
        Auction existing = auctionDAO.findById(auctionId);
        if (existing == null) return false;

        if ("ADMIN".equalsIgnoreCase(requesterRole)) return true;

        return "SELLER".equalsIgnoreCase(requesterRole)
            && existing.getSellerId() == requesterUserId;
    }

    @Override
    public boolean updateProduct(Auction auction, int requesterUserId, String requesterRole) {
        if (auction == null || auction.getId() == null) return false;
        if (!canModify(auction.getId(), requesterUserId, requesterRole)) return false;
        return auctionDAO.updateAuction(auction);
    }

    @Override
    public boolean deleteProduct(String auctionId, int requesterUserId, String requesterRole) {
        if (auctionId == null || auctionId.isBlank()) return false;
        if (!canModify(auctionId, requesterUserId, requesterRole)) return false;
        return auctionDAO.deleteAuction(auctionId);
    }
}