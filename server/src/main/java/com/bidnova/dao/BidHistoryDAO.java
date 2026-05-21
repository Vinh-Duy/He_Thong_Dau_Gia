package com.bidnova.dao;

import com.bidnova.database.DatabaseConnection;
import com.bidnova.models.BidHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) để quản lý các bản ghi lịch sử đấu giá.
 */
public class BidHistoryDAO {

    /**
     * Thêm một bản ghi lịch sử đấu giá mới vào cơ sở dữ liệu.
     *
     * @param bidHistory Đối tượng BidHistory chứa thông tin đấu giá.
     * @return true nếu thêm thành công, false nếu có lỗi.
     */
    public boolean addBid(BidHistory bidHistory) {
        String sql = "INSERT INTO BidHistory (auction_id, user_id, username, bid_amount, bid_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bidHistory.getAuctionId());
            stmt.setInt(2, bidHistory.getUserId());
            stmt.setString(3, bidHistory.getUsername());
            stmt.setDouble(4, bidHistory.getBidAmount());
            stmt.setTimestamp(5, Timestamp.valueOf(bidHistory.getBidTime()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lịch sử đấu giá: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tất cả lịch sử đấu giá cho một phiên đấu giá cụ thể.
     *
     * @param auctionId ID của phiên đấu giá.
     * @return Danh sách các bản ghi BidHistory, sắp xếp theo thời gian đấu giá tăng dần.
     */
    public List<BidHistory> getBidHistoryForAuction(String auctionId) {
        List<BidHistory> bidHistories = new ArrayList<>();
        String sql = "SELECT id, auction_id, user_id, username, bid_amount, bid_time FROM BidHistory WHERE auction_id = ? ORDER BY bid_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bidHistories.add(new BidHistory(
                            rs.getInt("id"), rs.getString("auction_id"), rs.getInt("user_id"),
                            rs.getString("username"), rs.getDouble("bid_amount"), rs.getTimestamp("bid_time").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử đấu giá cho auction " + auctionId + ": " + e.getMessage());
        }
        return bidHistories;
    }
}