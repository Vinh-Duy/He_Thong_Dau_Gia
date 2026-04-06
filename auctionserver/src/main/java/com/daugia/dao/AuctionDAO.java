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
        String sql = "SELECT id, name, start_price, current_highest_bid FROM auctions WHERE status = 'OPEN'";
        
        System.out.println("=> [DB DEBUG] Đang kết nối Database để lấy danh sách...");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            System.out.println("=> [DB DEBUG] Kết nối thành công! Đang quét dữ liệu...");
             
            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setName(rs.getString("name"));
                auction.setStartingPrice(rs.getDouble("start_price"));
                auction.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                
                list.add(auction);
                System.out.println("=> [DB DEBUG] Tìm thấy: " + auction.getName());
            }
        } catch (Exception e) {
            System.err.println("=> [DB DEBUG] LỖI RỒI! CHI TIẾT LỖI BÊN DƯỚI:");
            e.printStackTrace(); // In ra dòng màu đỏ để biết lỗi gì
        }
        
        System.out.println("=> [DB DEBUG] Tổng số hàng lấy được: " + list.size());
        return list;
    }

    public void updateHighestBid(String auctionId, double newPrice) {
        String sql = "UPDATE auctions SET current_highest_bid = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, auctionId);
            pstmt.executeUpdate(); 
            System.out.println("Đã cập nhật giá mới vào DB!");
        } catch (SQLException e) {
            System.err.println("=> [DB DEBUG] LỖI CẬP NHẬT GIÁ:");
            e.printStackTrace();
        }
    }
}