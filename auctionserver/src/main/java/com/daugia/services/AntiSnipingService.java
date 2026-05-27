package com.daugia.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.daugia.dao.AuctionDAO;
import com.daugia.models.Auction;

/**
 * Anti-Sniping Service - Tự động gia hạn thời gian khi có bid trong phút cuối
 * 
 * Logic:
 * - Nếu bid được đặt trong 5 phút cuối → gia hạn thêm 5 phút
 * - Cập nhật endTime trong memory (AuctionManager) và database (AuctionDAO)
 * - Return newEndTime để gửi cho clients
 * 
 * @author Anti-Sniping Team
 */
public class AntiSnipingService {
    private final AuctionDAO auctionDAO = new AuctionDAO();
    
    // Constant: Gia hạn 5 phút nếu bid trong 5 phút cuối
    private static final long ANTI_SNIPE_THRESHOLD_MINUTES = 1;
    private static final long ANTI_SNIPE_EXTENSION_MINUTES = 5;
    
    private static final DateTimeFormatter[] END_TIME_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };
    
    /**
     * Check if anti-sniping should be triggered and extend time if needed
     * 
     * @param auctionId - Auction ID
     * @param auction - Current Auction object
     * @return newEndTime if extended, null otherwise
     */
    public String checkAndExtendIfNeeded(String auctionId, Auction auction) {
        try {
            String currentEndTime = auction.getEndTime();
            if (currentEndTime == null || currentEndTime.isBlank()) {
                return null; // No end time set, can't trigger anti-sniping
            }
            
            LocalDateTime endDateTime = parseEndTime(currentEndTime);
            if (endDateTime == null) {
                return null; // Can't parse end time
            }
            
            LocalDateTime now = LocalDateTime.now();
            long minutesUntilEnd = java.time.temporal.ChronoUnit.MINUTES.between(now, endDateTime);
            
            // If bid placed within last 5 minutes, extend by 5 minutes
            if (minutesUntilEnd >= 0 && minutesUntilEnd <= ANTI_SNIPE_THRESHOLD_MINUTES) {
                LocalDateTime newEndTime = endDateTime.plusMinutes(ANTI_SNIPE_EXTENSION_MINUTES);
                String newEndTimeString = formatDateTime(newEndTime);
                
                // Update in memory
                auction.setEndTime(newEndTimeString);
                
                // Update in database
                auctionDAO.updateEndTime(auctionId, newEndTimeString);
                
                // Update in AuctionManager
                AuctionManager.getInstance().getAuction(auctionId).setEndTime(newEndTimeString);
                
                System.out.println("✓ Anti-Sniping triggered: Extended " + auctionId + 
                    " from " + currentEndTime + " to " + newEndTimeString);
                
                return newEndTimeString;
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Error in checkAndExtendIfNeeded: " + e.getMessage());
            return null;
        }
    }
    
    private LocalDateTime parseEndTime(String endTimeRaw) {
        for (DateTimeFormatter fmt : END_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(endTimeRaw, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
