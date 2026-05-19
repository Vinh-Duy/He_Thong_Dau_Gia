package com.bidnova.services;

import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import java.util.List;

public class DatabaseInitializer {
    
    public static void initializeActiveAuctions() {
        System.out.println("Đang kết nối Database để nạp dữ liệu...");
        
        AuctionDAO dao = new AuctionDAO();
        List<Auction> activeAuctions = dao.getAllActiveAuctions();
        
        if (activeAuctions != null && !activeAuctions.isEmpty()) {
            for (Auction a : activeAuctions) {
                AuctionManager.getInstance().addAuction(a);
            }
            System.out.println("Đã tải " + activeAuctions.size() + " phiên đấu giá vào bộ nhớ thành công!");
        } else {
            System.out.println("Không có dữ liệu đấu giá nào hoặc kết nối DB thất bại!");
        }
    }
}
