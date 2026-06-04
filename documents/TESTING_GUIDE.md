# 🧪 QUICK TEST REFERENCE - Giá Trần & Bước Giá Tối Thiếu

## ⚡ Quick Start Testing

### Database Setup

```sql
-- Run this to update schema
ALTER TABLE auctions ADD COLUMN price_ceiling DOUBLE NULL DEFAULT NULL;
ALTER TABLE auctions ADD COLUMN min_bid_increment DOUBLE NOT NULL DEFAULT 1000;

-- Update existing auctions
UPDATE auctions SET min_bid_increment = 1000 WHERE min_bid_increment = 0 OR min_bid_increment IS NULL;
```

### Build & Run

```bash
cd /Users/vinhduy/Desktop/HeThongDauGia

# Build server
mvn -f server/pom.xml clean package

# Build client
mvn -f client/pom.xml clean package

# Run server
java -jar server/target/*.jar

# Run client (separate terminal)
mvn -f client/pom.xml javafx:run
```

---

## Manual Test Scenarios

### Scenario A: Min Bid Increment Validation

**Setup:**

- Create auction with:
    - Start price: 100,000,000
    - Min increment: 2,000,000

**Test Cases:**

| Action          | Expected                              | Result |
| --------------- | ------------------------------------- | ------ |
| Bid 101,000,000 | ERROR: "Bước giá tối thiểu 2,000,000" |        |
| Bid 102,000,000 | SUCCESS                               |        |
| Bid 104,000,000 | SUCCESS (4M increment)                |        |

**How to Test:**

1. Open auction detail
2. Try bidding 101M → should get error
3. Try bidding 102M → should succeed

---

### Scenario B: Price Ceiling (Instant Win)

**Setup:**

- Create auction with:
    - Start price: 100,000,000
    - Price ceiling: 150,000,000
    - Min increment: 1,000,000

**Test Cases:**

| Bid Amount  | Current | Expected Result              |
| ----------- | ------- | ---------------------------- |
| 140,000,000 | 100M    | Accepted, auction OPEN       |
| 150,000,000 | 140M    | Accepted, auction FINISHED   |
| 160,000,000 | 140M    | Warning: "Đạt giới hạn trần" |

**How to Test:**

1. Create auction with ceiling = 150M
2. Bid progressively toward ceiling
3. At 150M: should close automatically
4. Verify status = FINISHED in database

---

### Scenario C: AutoBid with Min Increment

**Setup:**

- Auction:
    - Current price: 100M
    - Min increment: 3M
    - Price ceiling: None
- User_B AutoBid:
    - Max bid: 150M
    - Increment: 1M (set by user)

**Execution:**

```
User_A bids 100M
  ↓
AutoBidService:
  - Calculate: 100M + 1M = 101M
  - Check: (101M - 100M = 1M) < 3M? YES
  - Adjust: 100M + 3M = 103M ✓
  - Check: 103M <= 150M (maxBid)? YES
  - Place: 103M for User_B

Result: Current price = 103M (not 101M)
```

**How to Test:**

1. User_A: bid 100M
2. User_B: set AutoBid (max=150M, increment=1M)
3. User_A: bid 100M (trigger AutoBid)
4. Expected: AutoBid placed at 103M (adjusted from 101M)
5. Check server logs: Should show adjustment message

---

### Scenario D: AutoBid Reaches Ceiling

**Setup:**

- Auction:
    - Current: 145M
    - Ceiling: 150M
    - Min increment: 1M
- User_B AutoBid:
    - Max bid: 200M (higher than ceiling)
    - Increment: 5M

**Execution:**

```
User_A bids 145M
  ↓
AutoBidService:
  - Calculate: 145M + 5M = 150M
  - Check: 150M >= 150M (ceiling)? YES!
  - Set bid: 150M (ceiling value)
  - Place bid: 150M
  - Close auction: status = FINISHED
  - Deactivate AutoBid

Result: User_B wins at 150M, auction closed
```

**How to Test:**

1. Create auction with ceiling = 150M
2. User_B sets AutoBid: max=200M, increment=5M
3. User_A bids 145M
4. Expected: AutoBid triggers at 150M, auction closes
5. Check database: status should be FINISHED
6. Check response: ceilingReached = true, action = "AUCTION_FINISHED"

---

### Scenario E: Approaching Ceiling Warning

**Setup:**

- Ceiling: 150M
- Current: 100M

**Test:**

```
User tries to bid 145M (90% of ceiling)
Expected: Show warning - "Giá của bạn gần giới hạn trần!"

User tries to bid 150M (= ceiling)
Expected: Show alert - "Giá của bạn đạt giới hạn trần - đấu giá sẽ kết thúc!"
```

---

## 🔍 What to Check in Logs

### Server Console

```
When bid triggers ceiling:
Auction ABC123 FINISHED - Price ceiling reached!

When AutoBid adjusts:
📊 Adjusted bid from 101000000.0 to 103000000.0 (min increment: 2000000.0)

When AutoBid hits ceiling:
Auto-bid at ceiling: user_B bid 150000000.0 - Auction finished!
```

### Database

```sql
-- Verify auction closed
SELECT id, status, current_highest_bid, price_ceiling
FROM auctions
WHERE id = 'auction_id_here';

-- Result:
-- status should be FINISHED
-- current_highest_bid should be >= price_ceiling
```

---

## 🐛 Common Issues & Fixes

### Issue 1: "Min Increment Not Working"

**Check:**

1. Did you run ALTER TABLE? (database schema updated)
2. Is min_bid_increment value > 0 in database?
3. Check PlaceBidHandler logs for validation

**Fix:**

```sql
SELECT id, min_bid_increment FROM auctions WHERE id = 'test_auction';
-- Should show min_bid_increment = 1000 (or custom value)

UPDATE auctions SET min_bid_increment = 1000 WHERE id = 'test_auction';
```

### Issue 2: "Ceiling Not Closing Auction"

**Check:**

1. Is price_ceiling set in database?
2. Is bid amount actually >= ceiling?
3. Check PlaceBidHandler logs

**Fix:**

```sql
SELECT id, price_ceiling, current_highest_bid, status
FROM auctions WHERE id = 'test_auction';

-- Update ceiling if not set
UPDATE auctions SET price_ceiling = 150000000 WHERE id = 'test_auction';
```

### Issue 3: "AutoBid Not Adjusting"

**Check:**

1. Is AutoBidService receiving the auction object with minBidIncrement?
2. Check logs for "Adjusted bid" message
3. Verify getMinBidIncrement() returns correct value

**Fix:**

```java
// Add debug log in AutoBidService
System.out.println("Min increment for auction: " + auction.getMinBidIncrement());
System.out.println("Calculated next bid: " + nextBidAmount);
System.out.println("Required increment: " + minimumRequiredIncrement);
```

---

## 📊 Test Data Creation

### SQL: Create Test Auction

```sql
INSERT INTO auctions (
    id, name, product_name, description,
    start_price, current_highest_bid,
    start_time, end_time,
    status, category, seller_id,
    price_ceiling, min_bid_increment
) VALUES (
    'TEST_CEILING_001',
    'Test Ceiling',
    'Test Product',
    'Test Description',
    100000000,      -- start_price: 100M
    100000000,      -- current_highest_bid: 100M
    NOW(),
    DATE_ADD(NOW(), INTERVAL 1 DAY),
    'OPEN',
    'Test',
    1,
    150000000,      -- price_ceiling: 150M
    2000000         -- min_bid_increment: 2M
);
```

### SQL: Create Test User with AutoBid

```sql
-- Assuming user exists with username 'testuser'
INSERT INTO auto_bids (
    auction_id, username, max_bid, increment, is_active, created_at
) VALUES (
    'TEST_CEILING_001',
    'testuser',
    180000000,      -- max_bid: 180M
    5000000,        -- increment: 5M
    true,
    UNIX_TIMESTAMP() * 1000
);
```

---

## Checklist Before Declaring "Complete"

- [ ] Database schema updated (price_ceiling, min_bid_increment columns added)
- [ ] Server builds without errors: `mvn -f server/pom.xml clean package`
- [ ] Client builds without errors: `mvn -f client/pom.xml clean package`
- [ ] Min bid increment validation works (reject bid < current + min)
- [ ] Price ceiling closes auction when bid >= ceiling
- [ ] AutoBid auto-adjusts when increment < min requirement
- [ ] AutoBid closes auction when next bid >= ceiling
- [ ] Client shows warning when approaching ceiling (90%)
- [ ] Client shows alert when bid = ceiling
- [ ] Server logs show correct messages for all scenarios
- [ ] Database reflects correct status (FINISHED) after ceiling reached

---

## Production Checklist

Before deploying to production:

- [ ] All test scenarios pass
- [ ] Code review completed
- [ ] Database migration script created & tested
- [ ] Backup existing data
- [ ] Documentation updated for users
- [ ] Edge cases handled (null ceiling, zero increment)
- [ ] Performance tested with high volume
- [ ] Security validated (no SQL injection, etc.)

---

**Happy Testing! 🎉**

If you encounter any issues, check the logs and test data queries above!
