package com.daugia.patterns.observer;

import com.daugia.models.Auction;
import com.daugia.network.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Concrete Observer - Bid Update Broadcasting
 * Gửi real-time updates đến tất cả connected clients
 * 
 * @author Design Patterns Team
 */
public class BidUpdateObserver implements AuctionObserver {
    
    private static final Gson gson = new Gson();
    private static final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();
    
    /**
     * Đăng ký client writer mới để nhận updates
     */
    public static void registerClient(PrintWriter writer) {
        if (writer != null && !clientWriters.contains(writer)) {
            clientWriters.add(writer);
        }
    }
    
    /**
     * Hủy đăng ký client writer
     */
    public static void unregisterClient(PrintWriter writer) {
        clientWriters.remove(writer);
    }
    
    /**
     * Lấy số lượng clients đang kết nối
     */
    public static int getConnectedClientCount() {
        return clientWriters.size();
    }
    
    @Override
    public void onBidPlaced(Auction auction, double newBid, String bidder) {
        // Tạo JSON update message
        JsonObject updateData = new JsonObject();
        updateData.addProperty("auctionId", auction.getId());
        updateData.addProperty("newHighestBid", newBid);
        updateData.addProperty("highestBidder", bidder);
        updateData.addProperty("timestamp", System.currentTimeMillis());
        
        Response response = new Response("SUCCESS", "Bid placed successfully", gson.toJson(updateData));
        
        // Broadcast đến tất cả clients
        broadcastUpdate("BID_UPDATE", response);
    }
    
    @Override
    public void onAuctionStatusChanged(Auction auction, String oldStatus, String newStatus) {
        JsonObject statusData = new JsonObject();
        statusData.addProperty("auctionId", auction.getId());
        statusData.addProperty("oldStatus", oldStatus);
        statusData.addProperty("newStatus", newStatus);
        statusData.addProperty("timestamp", System.currentTimeMillis());
        
        Response response = new Response("SUCCESS", "Auction status changed", gson.toJson(statusData));
        
        broadcastUpdate("STATUS_UPDATE", response);
    }
    
    @Override
    public void onAutoBidTriggered(Auction auction, double autoBidAmount, String username) {
        JsonObject autoBidData = new JsonObject();
        autoBidData.addProperty("auctionId", auction.getId());
        autoBidData.addProperty("autoBidAmount", autoBidAmount);
        autoBidData.addProperty("username", username);
        autoBidData.addProperty("timestamp", System.currentTimeMillis());
        
        Response response = new Response("SUCCESS", "Auto-bid triggered", gson.toJson(autoBidData));
        
        broadcastUpdate("AUTO_BID_UPDATE", response);
    }
    
    /**
     * Broadcast update đến tất cả connected clients
     */
    private void broadcastUpdate(String action, Response response) {
        JsonObject message = new JsonObject();
        message.addProperty("action", action);
        message.addProperty("payload", gson.toJson(response));
        
        String jsonMessage = gson.toJson(message);
        
        // Gửi đến tất cả clients, remove clients đã disconnect
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(jsonMessage);
                writer.flush();
            } catch (Exception e) {
                // Client đã disconnect, remove khỏi list
                clientWriters.remove(writer);
            }
        }
    }
    
    /**
     * Clear tất cả client connections
     */
    public static void clearAllClients() {
        clientWriters.clear();
    }
}
