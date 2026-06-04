# 💻 CODE EXAMPLES: Giá Trần & Bước Giá Tối Thiếu

## PART A: DATABASE CHANGES

### SQL Script to Add Columns

```sql
-- Add price ceiling support
ALTER TABLE auctions ADD COLUMN price_ceiling DOUBLE NULL DEFAULT NULL COMMENT 'Giá tối đa - khi đạt giá này đấu giá kết thúc';

-- Add minimum bid increment support
ALTER TABLE auctions ADD COLUMN min_bid_increment DOUBLE NOT NULL DEFAULT 1000 COMMENT 'Bước giá tối thiếu (VD: 1tr)';

-- Update existing auctions with default values
UPDATE auctions SET
  price_ceiling = GREATEST(start_price * 1.5, current_highest_bid * 1.2),
  min_bid_increment = 1000
WHERE price_ceiling IS NULL;
```

---

## PART B: JAVA MODEL CHANGES

### 1. Auction.java (Server Model)

```java
package com.bidnova.models;

import java.time.LocalDateTime;

public class Auction {
    private String id;
    private String productName;
    private double startPrice;
    private double currentHighestBid;
    private String highestBidder;
    private String status;
    private String category;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int sellerId;
    private String imageUrl;

    // NEW FIELDS
    private Double priceCeiling;          // Giá trần - null = vô giới hạn
    private double minBidIncrement = 1000; // Bước giá tối thiếu

    public Auction() {
        this.status = "OPEN";
        this.minBidIncrement = 1000; // Default 1 triệu
    }

    // ... existing getters/setters ...

    // NEW GETTERS & SETTERS
    public Double getPriceCeiling() {
        return priceCeiling;
    }

    public void setPriceCeiling(Double priceCeiling) {
        this.priceCeiling = priceCeiling;
    }

    public double getMinBidIncrement() {
        return minBidIncrement;
    }

    public void setMinBidIncrement(double minBidIncrement) {
        this.minBidIncrement = minBidIncrement;
    }

    // Helper method: Check if bid reaches ceiling
    public boolean isBidAtCeiling(double bidAmount) {
        if (priceCeiling == null) return false;
        return bidAmount >= priceCeiling;
    }

    // Helper method: Validate if bid respects min increment
    public boolean isBidIncrementValid(double bidAmount) {
        double increment = bidAmount - currentHighestBid;
        return increment >= minBidIncrement;
    }

    public synchronized boolean placeBid(String username, double bidAmount) {
        if (!"OPEN".equals(status)) return false;

        if (bidAmount > currentHighestBid) {
            currentHighestBid = bidAmount;
            highestBidder = username;
            return true;
        }
        return false;
    }
}
```

---

## PART C: DAO CHANGES

### 2. AuctionDAO.java (New Methods)

```java
package com.bidnova.dao;

import java.sql.*;
import com.bidnova.models.Auction;

public class AuctionDAO {
    // ... existing methods ...

    // NEW: Get price ceiling
    public Double getPriceCeiling(String auctionId) {
        String sql = "SELECT price_ceiling FROM auctions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getObject("price_ceiling", Double.class);
            }
        } catch (SQLException e) {
            System.err.println("Error getting price ceiling: " + e.getMessage());
        }
        return null;
    }

    // NEW: Update price ceiling
    public void updatePriceCeiling(String auctionId, Double priceCeiling) {
        String sql = "UPDATE auctions SET price_ceiling = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (priceCeiling == null) {
                pstmt.setNull(1, Types.DOUBLE);
            } else {
                pstmt.setDouble(1, priceCeiling);
            }
            pstmt.setString(2, auctionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating price ceiling: " + e.getMessage());
        }
    }

    // NEW: Get minimum bid increment
    public double getMinBidIncrement(String auctionId) {
        String sql = "SELECT min_bid_increment FROM auctions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double minIncrement = rs.getDouble("min_bid_increment");
                return minIncrement > 0 ? minIncrement : 1000; // Default 1tr
            }
        } catch (SQLException e) {
            System.err.println("Error getting min bid increment: " + e.getMessage());
        }
        return 1000; // Default
    }

    // NEW: Update minimum bid increment
    public void updateMinBidIncrement(String auctionId, double minBidIncrement) {
        String sql = "UPDATE auctions SET min_bid_increment = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, minBidIncrement);
            pstmt.setString(2, auctionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating min bid increment: " + e.getMessage());
        }
    }

    // NEW: Load full auction with new fields
    public Auction getAuctionFull(String auctionId) {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setProductName(rs.getString("product_name"));
                auction.setStartPrice(rs.getDouble("start_price"));
                auction.setCurrentHighestBid(rs.getDouble("current_highest_bid"));
                auction.setHighestBidder(rs.getString("highest_bidder"));
                auction.setStatus(rs.getString("status"));
                auction.setCategory(rs.getString("category"));
                auction.setDescription(rs.getString("description"));
                auction.setSellerId(rs.getInt("seller_id"));
                auction.setImageUrl(rs.getString("image_url"));

                // NEW FIELDS
                Object ceilingObj = rs.getObject("price_ceiling");
                if (ceilingObj != null) {
                    auction.setPriceCeiling(rs.getDouble("price_ceiling"));
                }
                auction.setMinBidIncrement(rs.getDouble("min_bid_increment"));

                return auction;
            }
        } catch (SQLException e) {
            System.err.println("Error getting auction: " + e.getMessage());
        }
        return null;
    }
}
```

---

## PART D: BID HANDLER - CORE LOGIC

### 3. PlaceBidHandler.java (Updated)

```java
package com.bidnova.handlers;

import java.time.LocalDateTime;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuctionManager;
import com.bidnova.services.AutoBidService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlaceBidHandler implements ActionHandler {
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AutoBidService autoBidService = new AutoBidService();

    @Override
    public Response handle(Request request, AuthUserContext authUser) {
        try {
            if (authUser == null) {
                return new Response("ERROR", "Unauthorized", null);
            }

            JsonObject bidData = JsonParser.parseString(request.getPayload()).getAsJsonObject();
            String auctionId = bidData.get("auctionId").getAsString();
            double bidAmount = bidData.get("amount").getAsDouble();

            Auction currentAuction = AuctionManager.getInstance().getAuction(auctionId);
            if (currentAuction == null) {
                return new Response("ERROR", "Phiên đấu giá không tồn tại!", null);
            }

            synchronized (currentAuction) {
                // 1️⃣ Check auction status
                String status = currentAuction.getStatus();
                if (status != null && !status.equalsIgnoreCase("OPEN")) {
                    return new Response("ERROR", "Phiên đấu giá đã đóng", null);
                }

                // 2️⃣ Check expiration
                if (isExpired(currentAuction.getEndTime())) {
                    currentAuction.setStatus("FINISHED");
                    return new Response("ERROR", "Phiên đấu giá đã hết thời gian", null);
                }

                // 3️⃣ Check if bid is higher than current
                if (bidAmount <= currentAuction.getCurrentHighestBid()) {
                    return new Response(
                        "ERROR",
                        "Giá đặt phải cao hơn giá hiện tại (" + currentAuction.getCurrentHighestBid() + ")",
                        null
                    );
                }

                // 4️⃣ NEW: Validate minimum bid increment
                double bidIncrement = bidAmount - currentAuction.getCurrentHighestBid();
                if (bidIncrement < currentAuction.getMinBidIncrement()) {
                    double minRequiredBid = currentAuction.getCurrentHighestBid() + currentAuction.getMinBidIncrement();
                    return new Response(
                        "ERROR",
                        String.format("Bước giá tối thiểu là %.0f. Giá tối thiểu yêu cầu: %.0f",
                            currentAuction.getMinBidIncrement(),
                            minRequiredBid),
                        null
                    );
                }

                // Update bid amount
                currentAuction.setCurrentHighestBid(bidAmount);
                currentAuction.setHighestBidder(authUser.getUsername());
                auctionDAO.updateHighestBid(auctionId, bidAmount);

                // Record bid history
                BidHistory history = new BidHistory(
                    auctionId,
                    authUser.getUserId(),
                    authUser.getUsername(),
                    bidAmount,
                    LocalDateTime.now()
                );
                bidHistoryDAO.addBid(history);

                // Anti-sniping logic
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endTime = currentAuction.getEndTime();
                boolean isExtended = false;
                if (endTime != null) {
                    java.time.Duration timeRemaining = java.time.Duration.between(now, endTime);
                    long secondsRemaining = timeRemaining.getSeconds();
                    if (secondsRemaining > 0 && secondsRemaining <= 300) {
                        LocalDateTime newEndTime = endTime.plusMinutes(5);
                        currentAuction.setEndTime(newEndTime);
                        auctionDAO.updateEndTime(auctionId, newEndTime);
                        isExtended = true;
                    }
                }

                // Execute auto-bids
                autoBidService.executeAutoBids(auctionId, bidAmount);

                // NEW: Check if price ceiling reached
                boolean ceilingReached = false;
                if (currentAuction.isBidAtCeiling(currentAuction.getCurrentHighestBid())) {
                    currentAuction.setStatus("FINISHED");
                    auctionDAO.updateStatus(auctionId, "FINISHED");
                    ceilingReached = true;
                    System.out.println("Auction " + auctionId + " FINISHED - Price ceiling reached!");
                }

                double finalHighestBid = currentAuction.getCurrentHighestBid();

                JsonObject successData = new JsonObject();
                successData.addProperty("auctionId", auctionId);
                successData.addProperty("newHighestBid", finalHighestBid);
                successData.addProperty("isExtended", isExtended);
                successData.addProperty("ceilingReached", ceilingReached); // NEW
                if (currentAuction.getEndTime() != null) {
                    successData.addProperty("newEndTime", currentAuction.getEndTime().toString());
                }

                JsonObject event = new JsonObject();
                event.addProperty("action", ceilingReached ? "AUCTION_FINISHED" : "BID_UPDATE");
                event.addProperty("payload", gson.toJson(successData));

                return new Response("SUCCESS", "Đặt giá thành công", gson.toJson(successData));
            }

        } catch (Exception e) {
            return new Response("ERROR", "PLACE_BID error: " + e.getMessage(), null);
        }
    }

    private boolean isExpired(LocalDateTime endTime) {
        if (endTime == null) return false;
        return LocalDateTime.now().isAfter(endTime);
    }
}
```

---

## PART E: AUTO-BID SERVICE - UPDATED

### 4. AutoBidService.java (Updated executeAutoBids)

```java
package com.bidnova.services;

import java.util.List;
import com.bidnova.dao.AutoBidDAO;
import com.bidnova.dao.AuctionDAO;
import com.bidnova.models.Auction;
import com.bidnova.models.AutoBid;

public class AutoBidService {
    private final AutoBidDAO autoBidDAO = new AutoBidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    public void executeAutoBids(String auctionId, double currentHighestBid) {
        try {
            List<AutoBid> autoBids = autoBidDAO.getActiveAutoBids(auctionId);
            Auction auction = auctionManager.getAuction(auctionId);

            if (auction == null) return;

            for (AutoBid autoBid : autoBids) {
                String currentLeader = auction.getHighestBidder();

                // Skip if not valid
                if (!isAutoBidValid(autoBid, currentHighestBid, currentLeader)) {
                    continue;
                }

                // Calculate next bid amount
                double nextBidAmount = currentHighestBid + autoBid.getIncrement();

                // NEW: Validate minimum bid increment
                double minimumRequiredIncrement = auction.getMinBidIncrement();
                if ((nextBidAmount - currentHighestBid) < minimumRequiredIncrement) {
                    // Adjust to minimum increment
                    nextBidAmount = currentHighestBid + minimumRequiredIncrement;
                    System.out.println("📊 Adjusted bid from " +
                        (currentHighestBid + autoBid.getIncrement()) +
                        " to " + nextBidAmount + " (min increment: " + minimumRequiredIncrement + ")");
                }

                // Check if next bid is within max limit
                if (nextBidAmount <= autoBid.getMaxBid()) {

                    // NEW: Check if bid reaches price ceiling
                    if (auction.getPriceCeiling() != null && nextBidAmount >= auction.getPriceCeiling()) {
                        // Bid reaches ceiling - place bid and close auction
                        nextBidAmount = auction.getPriceCeiling();
                        placeAutoBidOnAuction(auctionId, autoBid.getUsername(), nextBidAmount);

                        // Close auction
                        auction.setStatus("FINISHED");
                        auctionDAO.updateStatus(auctionId, "FINISHED");
                        autoBidDAO.deactivateAutoBid(autoBid.getId());

                        System.out.println("Auto-bid at ceiling: " + autoBid.getUsername() +
                            " bid " + nextBidAmount + " - Auction finished!");
                    } else {
                        // Normal auto-bid
                        placeAutoBidOnAuction(auctionId, autoBid.getUsername(), nextBidAmount);
                        System.out.println("✓ Auto-bid placed: " + autoBid.getUsername() + " bid " + nextBidAmount);
                    }

                    // Update current highest bid for next auto-bid check
                    currentHighestBid = nextBidAmount;
                } else {
                    // Max bid reached - deactivate this auto-bid
                    autoBidDAO.deactivateAutoBid(autoBid.getId());
                    System.out.println("⊘ Auto-bid deactivated: " + autoBid.getUsername() + " (max bid exceeded)");
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing auto-bids: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isAutoBidValid(AutoBid autoBid, double currentBid, String currentLeader) {
        if (!autoBid.isActive()) return false;
        if (autoBid.getMaxBid() <= currentBid) return false;
        if (autoBid.getUsername().equals(currentLeader)) return false;
        return true;
    }

    private synchronized void placeAutoBidOnAuction(String auctionId, String username, double bidAmount) {
        try {
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) return;

            synchronized (auction) {
                if (!"OPEN".equals(auction.getStatus())) return;

                if (bidAmount > auction.getCurrentHighestBid()) {
                    auction.setCurrentHighestBid(bidAmount);
                    auction.setHighestBidder(username);
                    auctionDAO.updateHighestBid(auctionId, bidAmount);

                    // Record in bid history
                    // ... (existing code)
                }
            }
        } catch (Exception e) {
            System.err.println("Error placing auto-bid: " + e.getMessage());
        }
    }
}
```

---

## PART F: CLIENT-SIDE VALIDATION

### 5. AuctionDetailController.java (Client - Partial)

```java
@FXML
private void handlePlaceBid() {
    String username = SessionManager.getUsername();
    if (username == null || username.isEmpty()) {
        showAlert("Cảnh báo", "Vui lòng đăng nhập!");
        return;
    }

    String bidText = txtBidInput.getText().trim();
    if (bidText.isEmpty()) {
        lblBidError.setText("Vui lòng nhập giá đấu!");
        lblBidError.setVisible(true);
        return;
    }

    try {
        double bidAmount = Double.parseDouble(bidText);

        // 1. Check higher than current
        if (bidAmount <= currentPriceValue) {
            lblBidError.setText("Giá đấu phải cao hơn giá hiện tại!");
            lblBidError.setVisible(true);
            return;
        }

        // 2. NEW: Check minimum bid increment
        double bidIncrement = bidAmount - currentPriceValue;
        double minBidIncrement = currentAuction.getMinBidIncrement();

        if (bidIncrement < minBidIncrement) {
            double minRequiredBid = currentPriceValue + minBidIncrement;
            lblBidError.setText(
                String.format("Bước giá tối thiểu: %.0f. Giá tối thiểu: %.0f",
                    minBidIncrement, minRequiredBid)
            );
            lblBidError.setVisible(true);
            return;
        }

        // 3. NEW: Warn if approaching price ceiling
        if (currentAuction.getPriceCeiling() != null) {
            if (bidAmount >= currentAuction.getPriceCeiling()) {
                showAlert("Thông báo", "Giá của bạn đạt giới hạn trần - đấu giá sẽ kết thúc!");
            } else if (bidAmount > currentAuction.getPriceCeiling() * 0.9) {
                showAlert("Cảnh báo", "Giá của bạn gần giới hạn trần!");
            }
        }

        // Send bid request
        JsonObject payload = new JsonObject();
        payload.addProperty("auctionId", currentAuction.getId());
        payload.addProperty("amount", bidAmount);

        new Thread(() -> {
            Request request = new Request("PLACE_BID", payload.toString(), SessionManager.getToken());
            Response response = NetworkClient.getInstance().sendRequest(request);

            Platform.runLater(() -> {
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    showAlert("Thành công", "Đặt giá thành công!");
                    txtBidInput.clear();
                    lblBidError.setVisible(false);
                    currentPriceValue = bidAmount;
                    lblCurrentBid.setText(formatVietnameseCurrency((long)bidAmount));
                } else {
                    String errorMsg = response != null ? response.getMessage() : "Lỗi không xác định";
                    lblBidError.setText(errorMsg);
                    lblBidError.setVisible(true);
                }
            });
        }).start();

    } catch (NumberFormatException e) {
        lblBidError.setText("Giá đấu phải là số!");
        lblBidError.setVisible(true);
    }
}
```

---

## SUMMARY TABLE

| Feature                | Where                   | What                               |
| ---------------------- | ----------------------- | ---------------------------------- |
| **Giá Trần**           | DB `price_ceiling`      | Khi bid ≥ ceiling → Finish auction |
| **Bước Giá Tối Thiếu** | DB `min_bid_increment`  | bid - current ≥ minIncrement       |
| **Validation**         | PlaceBidHandler         | Check both conditions              |
| **AutoBid Logic**      | AutoBidService          | Adjust bid + check ceiling         |
| **Client Validation**  | AuctionDetailController | Show error before sending          |

---

Ready to implement? Choose a file to start!
