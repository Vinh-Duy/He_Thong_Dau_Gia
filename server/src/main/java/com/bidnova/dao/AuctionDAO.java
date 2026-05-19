package com.bidnova.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.Auction;

public class AuctionDAO {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDateTime.parse(str, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Auction> getAllActiveAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setProductName(rs.getString("name"));
                auction.setDescription(rs.getString("description"));
                auction.setStartPrice(rs.getDouble("start_price"));
                auction.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                auction.setStartTime(parseDateTime(rs.getString("start_time")));
                auction.setEndTime(parseDateTime(rs.getString("end_time")));
                auction.setCategory(rs.getString("category"));
                auction.setStatus(rs.getString("status"));
                auction.setSellerId(rs.getInt("seller_id"));
                auction.setImageUrl(rs.getString("image_url"));
                
                list.add(auction);
            }
        } catch (SQLException e) {
            System.err.println("Error loading active auctions: " + e.getMessage());
        }
        
        return list;
    }

    public void updateHighestBid(String auctionId, double newPrice) {
        String sql = "UPDATE auctions SET current_highest_bid = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, auctionId);
            pstmt.executeUpdate(); 
        } catch (SQLException e) {
            System.err.println("Error updating highest bid: " + e.getMessage());
        }
    }

    public boolean addAuction(String id, String name, String desc, double startPrice, LocalDateTime startTime, LocalDateTime endTime, String status, String category, int sellerId, String imageUrl) {
        String sql = "INSERT INTO auctions (id, name, description, start_price, start_time, end_time, status, category, seller_id, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, desc);
            pstmt.setDouble(4, startPrice);
            pstmt.setTimestamp(5, startTime != null ? Timestamp.valueOf(startTime) : null);
            pstmt.setTimestamp(6, endTime != null ? Timestamp.valueOf(endTime) : null);
            pstmt.setString(7, status);
            pstmt.setString(8, category);
            pstmt.setInt(9, sellerId);
            pstmt.setString(10, imageUrl);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding auction: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAuction(Auction auc) {
        String sql = "UPDATE auctions SET name = ?, description = ?, start_price = ?, start_time = ?, end_time = ?, status = ?, category = ?, seller_id = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auc.getProductName());
            pstmt.setString(2, auc.getDescription());
            pstmt.setDouble(3, auc.getStartPrice());
            pstmt.setTimestamp(4, auc.getStartTime() != null ? Timestamp.valueOf(auc.getStartTime()) : null);
            pstmt.setTimestamp(5, auc.getEndTime() != null ? Timestamp.valueOf(auc.getEndTime()) : null);
            pstmt.setString(6, auc.getStatus());
            pstmt.setString(7, auc.getCategory());
            pstmt.setInt(8, auc.getSellerId());
            pstmt.setString(9, auc.getImageUrl());
            pstmt.setString(10, auc.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating auction: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAuction(String id) {
        String sql = "DELETE FROM auctions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting auction: " + e.getMessage());
            return false;
        }
    }

    public List<Auction> getAuctionsBySellerId(int sellerId) {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE seller_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sellerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction a = new Auction();
                    a.setId(rs.getString("id"));
                    a.setProductName(rs.getString("name"));
                    a.setDescription(rs.getString("description"));
                    a.setStartPrice(rs.getDouble("start_price"));
                    a.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                    a.setStartTime(parseDateTime(rs.getString("start_time")));
                    a.setEndTime(parseDateTime(rs.getString("end_time")));
                    a.setSellerId(rs.getInt("seller_id"));
                    a.setStatus(rs.getString("status"));
                    a.setCategory(rs.getString("category"));
                    a.setImageUrl(rs.getString("image_url"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting auctions by seller: " + e.getMessage());
        }

        return list;
    }

    public Auction findById(String id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Auction a = new Auction();
                    a.setId(rs.getString("id"));
                    a.setProductName(rs.getString("name"));
                    a.setDescription(rs.getString("description"));
                    a.setStartPrice(rs.getDouble("start_price"));
                    a.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                    a.setStartTime(parseDateTime(rs.getString("start_time")));
                    a.setEndTime(parseDateTime(rs.getString("end_time")));
                    a.setSellerId(rs.getInt("seller_id"));
                    a.setStatus(rs.getString("status"));
                    a.setCategory(rs.getString("category"));
                    a.setImageUrl(rs.getString("image_url"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding auction by id: " + e.getMessage());
        }
        return null;
    }
}