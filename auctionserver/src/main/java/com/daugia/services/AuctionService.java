package com.daugia.services;

import java.util.List;

import com.daugia.models.Auction;

public interface AuctionService {
    List<Auction> getAuctionsBySellerId(int sellerId);
    boolean updateProduct(Auction auction, int requesterUserId, String requesterRole);
    boolean deleteProduct(String auctionId, int requesterUserId, String requesterRole);
}
