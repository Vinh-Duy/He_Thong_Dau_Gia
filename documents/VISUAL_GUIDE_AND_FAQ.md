# 📊 VISUAL GUIDE: Giá Trần & Bước Giá Tối Thiếu

## 1️⃣ VISUAL FLOW - PLACING BID WITH NEW VALIDATION

```
┌─────────────────────────────────────────────────────────────┐
│  User Interface (Client)                                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Current Price:  100,000,000 đ                       │  │
│  │  Min Increment:  1,000,000 đ                         │  │
│  │  Price Ceiling:  150,000,000 đ (if set)             │  │
│  │  ─────────────────────────────────────────────────   │  │
│  │  Your Bid: [105000000]                               │  │
│  │  [Place Bid]                                         │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
                    🔍 CLIENT VALIDATION
                            ↓
            ┌───────────────────────────────────┐
            │ Is 105M > 100M?  ✓ YES            │
            │ Is (105M-100M=5M) ≥ 1M?  ✓ YES    │
            │ Does 105M exist?  ✓ YES            │
            └───────────────────────────────────┘
                            ↓
                    Send PLACE_BID Request
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Server Validation (PlaceBidHandler)                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ✓ 1. Auction OPEN?                                   │  │
│  │ ✓ 2. Not expired?                                    │  │
│  │ ✓ 3. 105M > 100M (currentHighestBid)?               │  │
│  │ ✓ 4. (105M - 100M = 5M) ≥ 1M (minIncrement)?       │  │
│  │ ✓ 5. 105M < 150M (priceCeiling)?  → PASS            │  │
│  │                                                       │  │
│  │ Result: ALL CHECKS PASS ✓                            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
            📝 Update currentHighestBid = 105M
            📝 Update highestBidder = username
            📝 Record in bidhistory
                            ↓
            ⚙️  AutoBidService.executeAutoBids()
                            ↓
        Check all active AutoBids for this auction
                            ↓
        ┌──────────────────────────────────────┐
        │ For each AutoBid:                    │
        │ ├─ nextBid = 105M + increment       │
        │ ├─ Validate min increment           │
        │ ├─ Check vs maxBid                  │
        │ ├─ Check vs ceiling                 │
        │ └─ Place bid or deactivate          │
        └──────────────────────────────────────┘
                            ↓
         Response SUCCESS to Client
        📢 Broadcast BID_UPDATE to all users
```

---

## 2️⃣ SCENARIO EXAMPLES

### Scenario 1: Normal Bid (All Good)

```
Current Price:     100,000,000 đ
Min Increment:       1,000,000 đ  (1M)
Price Ceiling:     150,000,000 đ  (150M)

User Input: 105,000,000 đ

Validation:
  ✓ 105M > 100M?  YES (higher than current)
  ✓ (105M - 100M = 5M) ≥ 1M?  YES (meets min increment)
  ✓ 105M < 150M?  YES (below ceiling)

Result: 🟢 BID ACCEPTED
  New current price: 105M
  Proceed to AutoBid check
```

### ❌ Scenario 2: Bid Too Low (Below Min Increment)

```
Current Price:     100,000,000 đ
Min Increment:       1,000,000 đ  (1M)

User Input: 100,500,000 đ

Validation:
  ✓ 100.5M > 100M?  YES
  ❌ (100.5M - 100M = 0.5M) ≥ 1M?  NO!

Result: BID REJECTED
  Error: "Bước giá tối thiểu là 1,000,000 đ.
           Giá tối thiểu yêu cầu: 101,000,000 đ"
```

### Scenario 3: Bid Reaches Ceiling

```
Current Price:     140,000,000 đ
Price Ceiling:     150,000,000 đ

User Input: 150,000,000 đ

Validation:
  ✓ 150M > 140M?  YES
  ✓ (150M - 140M = 10M) ≥ minIncrement?  YES

Result: CEILING REACHED!
  ✓ BID ACCEPTED: 150,000,000 đ
  AUCTION CLOSED IMMEDIATELY
  👑 Winner: This user

  Database Update:
    - status = "FINISHED"
    - currentHighestBid = 150M
    - highestBidder = this user
```

### Scenario 4: AutoBid Triggers (Respecting Min Increment)

```
Current Price:     100,000,000 đ
Min Increment:       2,000,000 đ
Price Ceiling:     200,000,000 đ

AutoBid for user_B:
  - maxBid: 150,000,000 đ
  - increment: 1,000,000 đ (set by user_B)

User_A places bid: 105,000,000 đ

AutoBidService logic:
  1. Current = 105M
  2. Next = 105M + 1M = 106M
  3. Check: (106M - 105M = 1M) ≥ 2M?  NO!
  4. Adjust: nextBid = 105M + 2M = 107M
  5. Check: 107M ≤ maxBid (150M)?  YES
  6. Check: 107M < ceiling (200M)?  YES
  7. Place bid: 107M for user_B

Result: 🟢 AutoBid ADJUSTED TO 107M
  Current price: 107,000,000 đ
  User_B is now leading
```

### Scenario 5: AutoBid Hits Ceiling

```
Current Price:     140,000,000 đ
Min Increment:       1,000,000 đ
Price Ceiling:     150,000,000 đ

AutoBid for user_B:
  - maxBid: 200,000,000 đ (higher than ceiling)
  - increment: 5,000,000 đ

User_A places bid: 140,000,000 đ

AutoBidService logic:
  1. Current = 140M
  2. Next = 140M + 5M = 145M
  3. Check: (145M - 140M = 5M) ≥ 1M?  YES
  4. Check: 145M ≤ maxBid (200M)?  YES
  5. Check: 145M < ceiling (150M)?  YES
  6. Place bid: 145M for user_B

Next round (imagine next user bid):
  Current = 145M
  Next = 145M + 5M = 150M  ← HITS CEILING!

  AutoBidService:
    ✓ Place bid at ceiling: 150M
    Close auction immediately
    👑 user_B wins!
```

---

## 3️⃣ DETAILED VALIDATION FLOWCHART

```
                    User Places Bid
                         │
                         ↓
            ┌─────────────────────────────┐
            │  Check #1: Auction Status   │
            │  Is status = "OPEN"?        │
            └─────────────────────────────┘
                         │
              ┌──────────┴──────────┐
              ✓ YES                 ✗ NO
              │                      │
              ↓                  ❌ REJECT
        (Continue)                 │
              │
              ↓
      ┌─────────────────────────────┐
      │  Check #2: Expiration       │
      │  Is now < endTime?          │
      └─────────────────────────────┘
              │
    ┌─────────┴──────────┐
    ✓ YES               ✗ NO
    │                    │
    ↓                ❌ REJECT
(Continue)              │
    │
    ↓
 ┌─────────────────────────────────┐
 │  Check #3: Amount Validity      │
 │  Is bidAmount >                 │
 │     currentHighestBid?          │
 └─────────────────────────────────┘
          │
┌─────────┴──────────┐
✓ YES               ✗ NO
│                    │
↓                ❌ REJECT
(Continue)          │
│
↓
┌──────────────────────────────────────────────┐
│  Check #4: MIN BID INCREMENT             │
│  Is (bidAmount - currentBid) ≥              │
│     minBidIncrement?                        │
└──────────────────────────────────────────────┘
          │
┌─────────┴──────────┐
✓ YES               ✗ NO
│                    │
↓                ❌ REJECT with error:
(Continue)       "Bước giá tối thiểu..."
│
↓
┌──────────────────────────────────────────────┐
│  Check #5: PRICE CEILING                 │
│  Is bidAmount >= priceCeiling?              │
│  (if priceCeiling is set)                   │
└──────────────────────────────────────────────┘
          │
    ┌─────┴─────┐
    YES         NO
    │           │
    ↓           ↓
CEILING   ✓ NORMAL
REACHED     BID
    │           │
    ↓           ↓
Close       Update DB
Auction     ↓
    │       AutoBid
    ↓       Check
    └───────┬───────┘
            │
            ↓
         SUCCESS
        📢 Broadcast
```

---

## 4️⃣ FAQ & EDGE CASES

### Q1: Nếu `priceCeiling = NULL` thì sao?

```
A: Không có giới hạn trần, đấu giá diễn ra bình thường cho đến hết thời gian.
   - Bỏ qua check ceiling
   - Chỉ validate minBidIncrement
```

### Q2: Nếu `minBidIncrement = 0` thì sao?

```
A: Cho phép bất kỳ giá > current (nguy hiểm: có thể đặt +1 đồng)

   Recommend:
   - Set default = 1,000,000 (1 triệu)
   - Seller nên validate khi tạo auction
```

### Q3: Nếu 2 người cùng lúc place bid?

```
A: MySQL sử dụng locks (trong PlaceBidHandler có synchronized)

   Person A: bid 105M (arrives first)
   Person B: bid 106M (arrives second)

   Luồng:
   1. A's bid locked & processed → current = 105M
   2. B's bid locked & processed → current = 106M
   3. AutoBid for A triggered if maxBid > 105M

   Result: B wins (higher bid)
```

### Q4: Nếu AutoBid increment quá nhỏ < minBidIncrement?

```
A: AutoBidService tự adjust:

   Example:
   - minBidIncrement = 2M
   - User set increment = 0.5M
   - Current = 100M

   AutoBid tính:
   next = 100M + 0.5M = 100.5M  ❌

   Adjust:
   next = 100M + 2M = 102M  ✓ (auto-adjusted)
```

### Q5: Nếu AutoBid trigger khi đạt ceiling?

```
A: Đấu giá kết thúc ngay lập tức:

   Example:
   - ceiling = 150M
   - current = 149M
   - AutoBid increment = 5M
   - maxBid = 200M (higher than ceiling)

   Khi user_A bid 149M:
   → AutoBid calculates: 149M + 5M = 154M
   → Detect: 154M >= ceiling (150M)
   → Set bid = 150M (ceiling value)
   → Close auction
   → user_B wins at 150M
```

### Q6: Có thể thay đổi giá trần sau khi tạo auction không?

```
A: Có, bằng cách:
   1. Seller click "Edit Auction"
   2. Update price_ceiling in database
   3. AuctionManager reload từ DB

   SQL: UPDATE auctions SET price_ceiling = ? WHERE id = ?
```

### Q7: Giới hạn giá trần có liên quan tới giá bắt đầu không?

```
A: Không bắt buộc, nhưng recommend:
   - priceCeiling >= startPrice (nếu không vô nghĩa)
   - priceCeiling > currentHighestBid (phải)

   Validation:
   IF priceCeiling < currentHighestBid THEN
     Reject update with error "Ceiling quá thấp"
```

### Q8: Nếu user set maxBid < priceCeiling?

```
A: AutoBid sẽ dừng trước khi đạt ceiling:

   Example:
   - ceiling = 150M
   - User A maxBid = 140M

   Luồng:
   1. Current = 100M
   2. AutoBid places at 105M
   3. Current = 105M
   4. AutoBid tries: 110M
   5. Check: 110M <= 140M?  YES
   6. Place: 110M
   ...
   7. Current = 140M (= maxBid)
   8. AutoBid deactivates (maxBid reached)

   Result: Auction continues, nhưng User A không bid nữa
           (Ceiling chưa đạt)
```

### Q9: Nếu bán hàng mà quên set giá trần?

```
A: Mặc định priceCeiling = NULL (vô giới hạn)

   Seller có thể:
   1. Edit auction lúc đang chạy
   2. Set ceiling bất cứ lúc nào
   3. Hoặc để NULL (diễn ra bình thường)
```

### Q10: MinBidIncrement có impact đến AutoBid không?

```
A: Có, AutoBidService sẽ:
   1. Tính nextBid = current + autoBid.increment
   2. Kiểm tra: (nextBid - current) >= minBidIncrement
   3. Nếu KHÔNG, auto-adjust nextBid
   4. Tiếp tục với nextBid đã adjust

   User không thấy vấn đề, hệ thống xử lý sau hậu trường.
```

---

## 5️⃣ IMPLEMENTATION CHECKLIST

- [ ] **Database**
    - [ ] Add `price_ceiling` column to auctions
    - [ ] Add `min_bid_increment` column to auctions
    - [ ] Update sample data with default values

- [ ] **Backend Model**
    - [ ] Update `Auction.java` with 2 new fields
    - [ ] Add getter/setter methods
    - [ ] Add helper methods: `isBidAtCeiling()`, `isBidIncrementValid()`

- [ ] **Backend DAO**
    - [ ] Add `getPriceCeiling()` method
    - [ ] Add `updatePriceCeiling()` method
    - [ ] Add `getMinBidIncrement()` method
    - [ ] Add `updateMinBidIncrement()` method
    - [ ] Update auction loading to include new fields

- [ ] **Backend Handlers**
    - [ ] Update `PlaceBidHandler.java` with validation
    - [ ] Add ceiling reached check
    - [ ] Add min increment validation

- [ ] **Backend Services**
    - [ ] Update `AutoBidService.executeAutoBids()`
    - [ ] Auto-adjust bid if < minBidIncrement
    - [ ] Check ceiling before placing AutoBid

- [ ] **Frontend**
    - [ ] Update `AuctionDetailController.java` with client-side validation
    - [ ] Display min increment requirement
    - [ ] Display ceiling (if set)
    - [ ] Show warning when approaching ceiling

- [ ] **Testing**
    - [ ] Unit tests for Auction model
    - [ ] Integration tests for PlaceBidHandler
    - [ ] AutoBid logic tests
    - [ ] Ceiling edge case tests

---

**Ready to code? Let me know which file to update first!**
