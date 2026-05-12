package com.daugia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.daugia.database.DatabaseConnection;
import com.daugia.models.Auction;
import com.daugia.models.Item;
import com.daugia.models.ItemFactory;

public class AuctionDAO {
    private Connection conn;

    public AuctionDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    /**
     * Load tất cả phiên đấu giá từ DB.
     * JOIN items để lấy thông tin sản phẩm, dùng ItemFactory tạo đúng loại Item.
     * ServerMain gọi hàm này 1 lần khi khởi động.
     */
    public List<Auction> loadAllAuctions() {
        List<Auction> auctions = new ArrayList<>();

        // JOIN auctions + items để lấy đủ thông tin trong 1 query
        String sql = "SELECT a.id AS auction_id, a.status, " +
                     "i.id AS item_id, i.name, i.description, " +
                     "i.category, i.starting_price, i.image_path " +
                     "FROM auctions a " +
                     "JOIN items i ON a.item_id = i.id";
        try {
            PreparedStatement ps   = conn.prepareStatement(sql);
            ResultSet         rs   = ps.executeQuery();

            while (rs.next()) {
                int    itemId       = rs.getInt("item_id");
                String name         = rs.getString("name");
                String description  = rs.getString("description");
                String category     = rs.getString("category");
                double startPrice   = rs.getDouble("starting_price");

                // ItemFactory tạo đúng loại Item theo category
                // extraParams truyền vào tối thiểu vì DB chưa có bảng riêng cho từng loại
                // — dùng giá trị mặc định tạm thời, nhóm có thể mở rộng sau
                Item item = ItemFactory.createItemFromDB(
                    itemId, name, description, startPrice, category
                );

                int    auctionId = rs.getInt("auction_id");
                String status    = rs.getString("status");

                auctions.add(new Auction(auctionId, item, status));
            }
        } catch (Exception e) {
            System.out.println("Lỗi load auctions từ DB: " + e.getMessage());
            e.printStackTrace();
        }
        return auctions;
    }

    /**
     * Lấy danh sách auctions dạng JSON-ready để gửi cho client (GET_ALL_AUCTIONS).
     * Trả về thông tin item + giá hiện tại để hiển thị trên màn hình category.
     */
    public List<AuctionDTO> getAllAuctionDTOs() {
        List<AuctionDTO> list = new ArrayList<>();
        String sql = "SELECT a.id AS auction_id, a.status, a.start_time, a.end_time, " +
                     "i.id AS item_id, i.name, i.category, " +
                     "i.starting_price, i.image_path " +
                     "FROM auctions a JOIN items i ON a.item_id = i.id";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet         rs = ps.executeQuery();
            while (rs.next()) {
                AuctionDTO dto = new AuctionDTO();
                dto.auctionId    = rs.getInt("auction_id");
                dto.itemId       = rs.getInt("item_id");
                dto.name         = rs.getString("name");
                dto.category     = rs.getString("category");
                dto.startingPrice= rs.getLong("starting_price");
                dto.imagePath    = rs.getString("image_path");
                dto.status       = rs.getString("status");
                dto.startTime    = rs.getString("start_time");
                dto.endTime      = rs.getString("end_time");
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // DTO đơn giản — Gson sẽ serialize thành JSON gửi cho client
    public static class AuctionDTO {
        public int    auctionId;
        public int    itemId;
        public String name;
        public String category;
        public long   startingPrice;
        public String imagePath;
        public String status;
        public String startTime;
        public String endTime;
    }
}