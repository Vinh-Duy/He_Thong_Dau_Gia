package com.daugia.services;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.daugia.models.Auction;

public class AuctionManager {
    private static AuctionManager instance;

    // auctionId → Auction object
    private ConcurrentHashMap<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();

    // ── REALTIME: auctionId → danh sách output stream của client đang xem phiên đó ──
    // Đây là "phòng" (room). Khi có bid mới, broadcastToRoom() gửi JSON tới
    // tất cả PrintWriter trong list này → TẤT CẢ client nhận được ngay lập tức.
    // CopyOnWriteArrayList: an toàn khi nhiều thread đọc/ghi đồng thời
    private ConcurrentHashMap<Integer, CopyOnWriteArrayList<PrintWriter>> rooms =
            new ConcurrentHashMap<>();

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) instance = new AuctionManager();
        return instance;
    }

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getAuctionId(), auction);
    }

    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }

    // ── REALTIME STEP 1: client vào phòng ────────────────────────────────────
    // Khi client gửi JOIN_AUCTION, ClientHandler gọi joinRoom(auctionId, out).
    // "out" là PrintWriter của socket client đó.
    // Từ giờ server biết client này cần nhận broadcast của phiên auctionId.
    public void joinRoom(int auctionId, PrintWriter out) {
        rooms.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(out);
    }

    // Gọi khi client ngắt kết nối — xóa khỏi phòng, không gửi broadcast vào socket chết
    public void leaveRoom(int auctionId, PrintWriter out) {
        CopyOnWriteArrayList<PrintWriter> room = rooms.get(auctionId);
        if (room != null) room.remove(out);
    }

    // ── REALTIME STEP 2: broadcast tới tất cả client trong phòng ─────────────
    // Được gọi ngay sau khi bid hợp lệ (PLACE_BID thành công).
    // Duyệt qua tất cả PrintWriter trong room → gửi JSON BidMessage.
    // Mỗi client đang chạy listenFromServer() sẽ nhận được dòng JSON này ngay lập tức.
    public void broadcastToRoom(int auctionId, String json) {
        CopyOnWriteArrayList<PrintWriter> room = rooms.get(auctionId);
        if (room == null) return;
        for (PrintWriter writer : room) {
            writer.println(json); // autoFlush=true → gửi ngay, không cần flush thủ công
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
}