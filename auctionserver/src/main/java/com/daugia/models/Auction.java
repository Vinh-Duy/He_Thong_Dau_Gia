package com.daugia.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Auction {
    private int    auctionId;
    private Item   item;
    private double currentHighestBid;
    private String highestBidder;
    private String status;

    // Lưu toàn bộ lịch sử bid hợp lệ.
    // Dùng khi client mới JOIN_AUCTION — server gửi list này để client vẽ lại chart từ đầu.
    private List<BidMessage> bidHistory = new ArrayList<>();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public Auction(int auctionId, Item item, String status) {
        this.auctionId          = auctionId;
        this.item               = item;
        this.currentHighestBid  = item.getStartingPrice(); // giá khởi điểm từ DB
        this.highestBidder      = null;
        this.status             = status;
    }

    public int    getAuctionId()          { return auctionId; }
    public Item   getItem()               { return item; }
    public double getCurrentHighestBid()  { return currentHighestBid; }
    public String getHighestBidder()      { return highestBidder; }
    public String getStatus()             { return status; }
    public void   setStatus(String s)     { this.status = s; }

    // Trả về bản sao chỉ đọc — bên ngoài không thể sửa list này trực tiếp
    public List<BidMessage> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }

    // synchronized: đảm bảo tại một thời điểm chỉ 1 thread xử lý bid
    // (tránh 2 người đặt cùng lúc đều thắng)
    public synchronized boolean placeBid(String username, double amount) {
        if (!"RUNNING".equals(status)) return false;
        if (amount <= currentHighestBid)  return false;

        currentHighestBid = amount;
        highestBidder     = username;

        // ── GHI VÀO LỊCH SỬ ──────────────────────────────────────────
        // Đây là nơi server ghi nhận mỗi bid hợp lệ vào bidHistory.
        // Sau đó AuctionManager sẽ broadcast BidMessage này tới tất cả client,
        // và client sẽ dùng nó để vẽ thêm điểm mới lên biểu đồ (realtime).
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        bidHistory.add(new BidMessage("NEW_BID", auctionId, username, amount, timestamp));
        // ─────────────────────────────────────────────────────────────

        return true;
    }

    // Lấy BidMessage vừa được thêm vào (phần tử cuối) để broadcast
    public BidMessage getLatestBid() {
        if (bidHistory.isEmpty()) return null;
        return bidHistory.get(bidHistory.size() - 1);
    }
}