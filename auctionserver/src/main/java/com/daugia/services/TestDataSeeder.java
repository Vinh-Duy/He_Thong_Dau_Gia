package com.daugia.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;

/**
 * Test Data Seeder - Tạo dữ liệu test với endTime để test anti-sniping
 * 
 * Usage: Gọi TestDataSeeder.seedTestData() từ DatabaseInitializer
 */
public class TestDataSeeder {
    
    private static final AuctionDAO auctionDAO = new AuctionDAO();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void seedTestData() {
        System.out.println("🔄 Seeding test data with endTime for anti-sniping tests...");
        
        try {
            // Check if test data already exists
            List<Auction> existing = auctionDAO.getAllActiveAuctions();
            boolean hasEndTime = existing != null && !existing.isEmpty() && existing.get(0).getEndTime() != null;
            
            if (hasEndTime) {
                System.out.println("✓ Test data with endTime already exists. Skipping seed.");
                return;
            }
            
            // Update existing auctions with endTime values
            updateAuctionEndTimes();
            
            System.out.println("✓ Test data seeded successfully!");
            
        } catch (Exception e) {
            System.err.println("⚠ Error seeding test data: " + e.getMessage());
        }
    }
    
    private static void updateAuctionEndTimes() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // A001 - Lamborghini: 5 minutes from now (trigger anti-sniping)
            String endTime1 = now.plusMinutes(5).format(formatter);
            auctionDAO.updateEndTime("A001", endTime1);
            System.out.println("  ✓ A001 end time: " + endTime1 + " (5 min - Anti-Sniping threshold)");
            
            // A002 - Villa: 10 minutes from now (no anti-sniping)
            String endTime2 = now.plusMinutes(10).format(formatter);
            auctionDAO.updateEndTime("A002", endTime2);
            System.out.println("  ✓ A002 end time: " + endTime2 + " (10 min - Safe)");
            
            // A003 - Painting: 3 minutes from now (definitely trigger anti-sniping)
            String endTime3 = now.plusMinutes(3).format(formatter);
            auctionDAO.updateEndTime("A003", endTime3);
            System.out.println("  ✓ A003 end time: " + endTime3 + " (3 min - Will trigger anti-sniping)");
            
        } catch (Exception e) {
            System.err.println("Error updating auction end times: " + e.getMessage());
        }
    }
}
