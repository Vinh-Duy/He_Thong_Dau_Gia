# 📋 PLAN: Thêm Giá Trần & Bước Giá Tối Thiếu

## 1️⃣ TỔNG QUAN HỆ THỐNG HIỆN TẠI

### Cấu Trúc Luồng Đặt Giá:

```
Client (AuctionDetailController)
    ↓
PLACE_BID request
    ↓
Server (PlaceBidHandler)
    ├─ ✓ Kiểm tra đấu giá còn mở không (OPEN)
    ├─ ✓ Kiểm tra hết hạn không
    ├─ ✓ Kiểm tra giá > giá hiện tại không
    └─ ✓ Cập nhật DB & gọi AutoBidService
    ↓
AutoBidService.executeAutoBids()
    ├─ Lấy tất cả AutoBid đang active
    ├─ Tính nextBid = currentBid + increment
    ├─ Nếu nextBid ≤ maxBid → Đặt giá tự động
    └─ Nếu nextBid > maxBid → Huỷ AutoBid
```

### Database Schema Hiện Tại:

```sql
auctions:
  ├─ id (VARCHAR)
  ├─ product_name
  ├─ start_price
  ├─ current_highest_bid (giá hiện tại)
  ├─ highest_bidder
  ├─ status (OPEN/FINISHED)
  ├─ end_time
  └─ ...other fields

auto_bids:
  ├─ id
  ├─ auction_id
  ├─ username
  ├─ max_bid (giá tối đa user sẵn sàng)
  ├─ increment (bước tăng giá)
  └─ is_active
```

---

## 2️⃣ FEATURE 1: GIÁ TRẦN (Price Ceiling)

### Yêu Cầu:

- **Định nghĩa**: Giá tối đa của cuộc đấu giá. Khi ai đó đặt giá ≥ giá trần, **đấu giá kết thúc ngay lập tức**, người đặt giá đó trở thành người thắng.
- **Ví dụ**: Bạn bán chiếc xe với start_price=100tr, giá trần=150tr. Ai đó đặt 150tr thì việc này xong, không cần chờ hết thời gian.

### 📝 Các Files Cần Sửa:

#### A. Database (`db_setup.sql`)

```sql
ALTER TABLE auctions ADD COLUMN price_ceiling DOUBLE NULL DEFAULT NULL;
```

#### B. Model: Auction.java (server)

```java
private double priceCeiling;  // Thêm field này

// Getter & Setter
public double getPriceCeiling() { return priceCeiling; }
public void setPriceCeiling(double priceCeiling) { this.priceCeiling = priceCeiling; }
```

#### C. DAO: AuctionDAO.java

- **updatePriceCeiling()** - cập nhật giá trần
- **getPriceCeiling()** - lấy giá trần từ DB

#### D. Handler: PlaceBidHandler.java

```java
// Sau khi validate giá > currentHighestBid:
if (bidAmount >= currentAuction.getPriceCeiling()) {
    // BID ĐẠT GIỚI HẠN TRẦN → KẾT THÚC ĐẤU GIÁ
    currentAuction.setCurrentHighestBid(bidAmount);
    currentAuction.setHighestBidder(authUser.getUsername());
    currentAuction.setStatus("FINISHED");  // ← Kết thúc ngay
    auctionDAO.updateHighestBid(auctionId, bidAmount);
    auctionDAO.updateStatus(auctionId, "FINISHED");

    return new Response("SUCCESS", "Đặt giá thành công! Đấu giá đã kết thúc do đạt giá trần", ...);
}
```

#### E. Controller: AuctionDetailController.java (Client)

```java
// Hiển thị giá trần trên giao diện
lblPriceCeiling.setText(formatCurrency(auction.getPriceCeiling()));
```

---

## 3️⃣ FEATURE 2: BƯỚC GIÁ TỐI THIẾU (Minimum Bid Increment)

### Yêu Cầu:

- **Định nghĩa**: Giá cuối cùng phải cao hơn giá hiện tại ít nhất `minBidIncrement` đó.
- **Mục đích**: Tránh autobid đặt giá chỉ +1 đồng (gây lãng phí).
- **Ví dụ**: minBidIncrement=1tr, currentBid=100tr
    - User đặt 101tr (chỉ +1tr, không hợp lệ)
    - User đặt 101tr? ❌
    - User đặt 101tr ✓ (chính xác bước giá)

### 📝 Các Files Cần Sửa:

#### A. Database (`db_setup.sql`)

```sql
ALTER TABLE auctions ADD COLUMN min_bid_increment DOUBLE NOT NULL DEFAULT 1000;
```

#### B. Model: Auction.java (server)

```java
private double minBidIncrement = 1000;  // Default 1tr

public double getMinBidIncrement() { return minBidIncrement; }
public void setMinBidIncrement(double minBidIncrement) { this.minBidIncrement = minBidIncrement; }
```

#### C. DAO: AuctionDAO.java

- **updateMinBidIncrement()**
- **getMinBidIncrement()**

#### D. Handler: PlaceBidHandler.java

```java
// Validate bước giá tối thiếu
double minRequiredBid = currentAuction.getCurrentHighestBid() + currentAuction.getMinBidIncrement();

if (bidAmount < minRequiredBid) {
    return new Response(
        "ERROR",
        "Giá đặt phải cao hơn ít nhất " + currentAuction.getMinBidIncrement() +
        " (tối thiểu: " + minRequiredBid + ")",
        null
    );
}
```

#### E. Service: AutoBidService.java

```java
public void executeAutoBids(String auctionId, double currentHighestBid) {
    ...
    double nextBidAmount = currentHighestBid + autoBid.getIncrement();

    // Validate bước giá tối thiếu
    if (nextBidAmount - currentHighestBid < auction.getMinBidIncrement()) {
        nextBidAmount = currentHighestBid + auction.getMinBidIncrement();
    }

    if (nextBidAmount <= autoBid.getMaxBid()) {
        placeAutoBidOnAuction(auctionId, autoBid.getUsername(), nextBidAmount);
    }
    ...
}
```

#### F. Controller: AuctionDetailController.java (Client)

```java
// Validation trước khi gửi request
private void handlePlaceBid() {
    double minRequiredBid = currentPriceValue + currentAuction.getMinBidIncrement();

    if (bidAmount < minRequiredBid) {
        showAlert("Lỗi", "Bước giá tối thiếu: " + currentAuction.getMinBidIncrement());
        return;
    }

    // Gửi request
    sendBidRequest(bidAmount);
}
```

---

## 4️⃣ SUMMARY CÁC FILE CẦN SỬA

### Server-side (Backend):

| File                     | Thay đổi                                                           |
| ------------------------ | ------------------------------------------------------------------ |
| `db_setup.sql`           | Thêm 2 columns: `price_ceiling`, `min_bid_increment`               |
| `Auction.java` (model)   | Thêm 2 fields + getters/setters                                    |
| `AuctionDAO.java`        | 4 new methods: get/update priceCeiling, get/update minBidIncrement |
| `PlaceBidHandler.java`   | Validate ceiling & min increment                                   |
| `AutoBidService.java`    | Tính toán lại nextBid respecting minBidIncrement                   |
| `SetAuctionHandler.java` | Cho phép seller set giá trần & bước giá khi tạo đấu giá            |

### Client-side (GUI):

| File                           | Thay đổi                                        |
| ------------------------------ | ----------------------------------------------- |
| `AuctionDetailController.java` | Hiển thị giá trần & validate bước giá           |
| `AuctionDetailView.fxml`       | Thêm Label/TextField cho ceiling & minIncrement |
| `AddProductView.fxml`          | Input fields cho seller set giá trần & bước giá |

---

## 5️⃣ LUỒNG VALIDATION SAU KHI IMPLEMENT

```
User Đặt Giá 105tr
    ↓
PlaceBidHandler kiểm tra:
  ├─ Đấu giá OPEN? ✓
  ├─ Hết hạn? ✓
  ├─ bidAmount (105) > currentHighestBid (100)? ✓
  ├─ (105 - 100 = 5) >= minBidIncrement (2)? ✓
  ├─ bidAmount (105) >= priceCeiling (150)? → Cho phép
  └─ Cập nhật DB, gọi AutoBidService
    ↓
AutoBidService:
  ├─ Lấy tất cả AutoBid của cuộc này
  ├─ Tính: nextBid = 105 + autoBid.increment
  ├─ Validate: (nextBid - 105) >= minBidIncrement?
  ├─ Validate: nextBid <= autoBid.maxBid?
  ├─ Validate: nextBid >= priceCeiling? → Đóng nếu đạt
  └─ Đặt giá hoặc huỷ AutoBid
```

---

## 6️⃣ EDGE CASES & LƯU Ý

| Case                                    | Xử lý                                    |
| --------------------------------------- | ---------------------------------------- |
| priceCeiling = null                     | Coi như không có giới hạn                |
| minBidIncrement = 0                     | Cho phép bất kỳ giá > current            |
| minBidIncrement > (maxBid - currentBid) | AutoBid không thể tăng được → Deactivate |
| Bid chính xác = priceCeiling            | Status = FINISHED, người đó thắng        |
| Nhiều AutoBid, tất cả đạt ceiling       | Người nào trigger trước, người đó thắng  |

---

## 7️⃣ RECOMMENDED IMPLEMENTATION ORDER

1.  **Database schema** (`db_setup.sql`)
2.  **Models** (`Auction.java`)
3.  **DAOs** (`AuctionDAO.java`)
4.  **Server Logic** (`PlaceBidHandler.java`, `AutoBidService.java`)
5.  **Client UI** (`AuctionDetailController.java`, FXML files)
6.  **Test cases** (Unit & Integration tests)

---

**Ready to code? Let me know which feature you want to start with!**
