package com.daugia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.daugia.database.DatabaseConnection;
import com.daugia.models.Auction;

public class AuctionDAO {
    
    public List<Auction> getAllActiveAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT id, name, start_price, current_highest_bid FROM auctions";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setProductName(rs.getString("name"));
                auction.setStartPrice(rs.getDouble("start_price"));
                auction.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                
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

    public boolean addAuction(String id,String name, String desc, double startPrice, String endTime, int sellerId,String status) {
        String sql = "INSERT INTO auctions (id, name, description, start_price, end_time, seller_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, desc);
            pstmt.setDouble(4, startPrice);
            pstmt.setString(5, endTime);
            pstmt.setInt(6, sellerId);
            pstmt.setString(7, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding auction: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAuction(Auction auc) {
        String sql = "UPDATE auctions SET name = ?, description = ?, start_price = ?, end_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auc.getProductName());
            pstmt.setString(2, auc.getDescription());
            pstmt.setDouble(3, auc.getStartPrice());
            pstmt.setString(4, auc.getEndTime());
            pstmt.setString(5, auc.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating auction: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAuction(String id) {
        String sql = "DELETE FROM auctions WHERE id=?";
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
        String sql = "SELECT id, name, description, start_price, current_highest_bid, end_time, seller_id, status, category " +
                    "FROM auctions WHERE seller_id = ? ORDER BY id DESC";

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
                    a.setEndTime(rs.getString("end_time"));
                    a.setSellerId(rs.getInt("seller_id"));
                    a.setStatus(rs.getString("status"));
                    a.setCategory(rs.getString("category"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting auctions by seller: " + e.getMessage());
        }

        return list;
    }

    public Auction findById(String id) {
        String sql = "SELECT id, name, description, start_price, current_highest_bid, end_time, seller_id, status, category " +
                    "FROM auctions WHERE id = ?";

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
                    a.setEndTime(rs.getString("end_time"));
                    a.setSellerId(rs.getInt("seller_id"));
                    a.setStatus(rs.getString("status"));
                    a.setCategory(rs.getString("category"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding auction by id: " + e.getMessage());
        }
        return null;
    }
}