package com.bidnova.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.AutoBid;

public class AutoBidDAO {
    
    public boolean createAutoBid(AutoBid autoBid) {
        String sql = "INSERT INTO auto_bids (auction_id, username, max_bid, increment, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, autoBid.getAuctionId());
            pstmt.setString(2, autoBid.getUsername());
            pstmt.setDouble(3, autoBid.getMaxBid());
            pstmt.setDouble(4, autoBid.getIncrement());
            pstmt.setBoolean(5, autoBid.isActive());
            pstmt.setLong(6, autoBid.getCreatedAt());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating auto-bid: " + e.getMessage());
            return false;
        }
    }
    
    public List<AutoBid> getActiveAutoBids(String auctionId) {
        List<AutoBid> autoBids = new ArrayList<>();
        String sql = "SELECT * FROM auto_bids WHERE auction_id = ? AND is_active = true ORDER BY created_at ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, auctionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AutoBid autoBid = new AutoBid();
                    autoBid.setId(rs.getInt("id"));
                    autoBid.setAuctionId(rs.getString("auction_id"));
                    autoBid.setUsername(rs.getString("username"));
                    autoBid.setMaxBid(rs.getDouble("max_bid"));
                    autoBid.setIncrement(rs.getDouble("increment"));
                    autoBid.setActive(rs.getBoolean("is_active"));
                    autoBid.setCreatedAt(rs.getLong("created_at"));
                    
                    autoBids.add(autoBid);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting active auto-bids: " + e.getMessage());
        }
        
        return autoBids;
    }
    
    public boolean deactivateAutoBid(int autoBidId) {
        String sql = "UPDATE auto_bids SET is_active = false WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, autoBidId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating auto-bid: " + e.getMessage());
            return false;
        }
    }
    
    public AutoBid findByUserAndAuction(String username, String auctionId) {
        String sql = "SELECT * FROM auto_bids WHERE username = ? AND auction_id = ? AND is_active = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, auctionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AutoBid autoBid = new AutoBid();
                    autoBid.setId(rs.getInt("id"));
                    autoBid.setAuctionId(rs.getString("auction_id"));
                    autoBid.setUsername(rs.getString("username"));
                    autoBid.setMaxBid(rs.getDouble("max_bid"));
                    autoBid.setIncrement(rs.getDouble("increment"));
                    autoBid.setActive(rs.getBoolean("is_active"));
                    autoBid.setCreatedAt(rs.getLong("created_at"));
                    
                    return autoBid;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding auto-bid: " + e.getMessage());
        }
        
        return null;
    }
}
