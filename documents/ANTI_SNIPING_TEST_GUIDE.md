# Anti-Sniping Testing Guide

## Quick Start: Test Anti-Sniping in 5 Minutes

### Prerequisites

- Project compiled: `mvn clean package`
- Server running on port 8888
- Database connected
- 2 client instances

---

## Test Scenario 1: Anti-Sniping Triggers (A003 - 3 min remaining)

### Setup

1. **Start Server**:

    ```bash
    cd /Users/vinhduy/Desktop/HeThongDauGia/auctionserver
    java -cp target/classes com.daugia.ServerMain
    ```

    Expected output:

    ```
    🔄 Seeding test data with endTime for anti-sniping tests...
    ✓ A003 end time: 2026-05-12 12:XX:XX (3 min - Will trigger anti-sniping)
    ```

2. **Start 2 Client Instances**:

    ```bash
    # Terminal 1: Client A (Bidder)
    cd auctionclient
    mvn clean javafx:run

    # Terminal 2: Client B (Another Bidder)
    cd auctionclient
    mvn clean javafx:run
    ```

### Test Flow

| Step | Action                       | Client A            | Client B          | Expected                                                   |
| ---- | ---------------------------- | ------------------- | ----------------- | ---------------------------------------------------------- |
| 1    | Login both clients           | Username: `userA`   | Username: `userB` | Both in HomeView                                           |
| 2    | View HomeView                | See cards           | See cards         | **A003 shows "Còn lại: 3 phút"** (red text)                |
| 3    | Click "Đấu giá ngay" on A003 | Opens detail        | -                 | ItemDetailView for A003                                    |
| 4    | Wait 1-2 min                 | -                   | -                 | A003 countdown updates                                     |
| 5    | When ~2 min left             | Enter 1,010,000,030 | -                 | Place bid                                                  |
| 6    | Place bid                    | Click "ĐẶT GIÁ"     | -                 | **Server logs: "✓ Anti-Sniping triggered: Extended A003"** |
| 7    | Check result                 | -                   | Refresh           | See updated end_time on card                               |
| 8    | Verify extension             | Look at time        | -                 | **Was 2 min, now ~7 min** ⏱                                |

### Expected Outcomes

**Server Console Output**:

```
✓ Anti-Sniping triggered: Extended A003 from 2026-05-12 12:XX:XX to 2026-05-12 12:YY:YY
```

**Client A (Bidder)**:

- Bid placed successfully ✓
- Alert: "⏱ Gia hạn thời gian - Vì có đấu giá trong phút cuối, thời gian phiên đã được gia hạn thêm 5 phút!"

    **Client B (Observer)**:

- After refresh, sees extended time on card
- Time label changes from "Còn lại: 2 phút" → "⏱ Còn lại: 7 phút"

    **Database**:

- `auctions.end_time` for A003 updated with +5 minutes

---

## Test Scenario 2: No Anti-Sniping (A002 - 10 min remaining)

### Setup

Same as Scenario 1, but test A002 (Lamborghini - 10 minutes)

### Test Flow

1. Login both clients
2. View HomeView → A002 shows "⏱ Còn lại: 10 phút" (green - safe)
3. Open A002 detail
4. Place bid when 7+ minutes remaining
5. **Expected**: No alert, no time extension

### Expected Outcomes

**No anti-sniping triggered** (time ≥ 5 min threshold)  
 **Server logs**: No "Anti-Sniping triggered" message  
 **Database**: `end_time` unchanged

---

## Test Scenario 3: Multiple Bids - Progressive Extension

### Setup

- A001 (Lamborghini): 5 minutes remaining
- Multiple bidders ready

### Test Flow

| Bid # | Time                  | User  | Result      | New Time       |
| ----- | --------------------- | ----- | ----------- | -------------- |
| 1     | t0 (4 min left)       | UserA | Extends     | +5 min (9 min) |
| 2     | t0 + 30s (8:30 left)  | UserB | No trigger  | Stays 8:30     |
| 3     | t0 + 90s (7:30 left)  | UserC | No trigger  | Stays 7:30     |
| 4     | t0 + 4:30 (3:30 left) | UserA | **Extends** | +5 min (8:30)  |

### Expected Outcome

Auction keeps extending as long as bids keep coming in last 5 minutes!

---

## Debug Checklist

### Issue: Time not showing on card

- [ ] `HomeViewController` receives `endTime` from server?
- [ ] `calculateMinutesRemaining()` parsing correctly?
- [ ] Check server logs for `getAllActiveAuctions()` payload

### Issue: Anti-sniping not triggered despite bid in last 5 min

- [ ] Check server logs: `"✓ Anti-Sniping triggered"` message?
- [ ] Verify `AntiSnipingService.checkAndExtendIfNeeded()` called?
- [ ] Check if database `end_time` is NULL (skips anti-snipe check)

### Issue: Time extension not visible on client

- [ ] Check `ItemDetailController.handleRealTimeUpdate()` receives `newEndTime`?
- [ ] Verify WebSocket connection active?
- [ ] Check JSON payload includes `"newEndTime"` field?

### Issue: Database not updating

- [ ] Check `AuctionDAO.updateEndTime()` SQL query
- [ ] Verify database `auctions` table has `end_time` column
- [ ] Check for SQL errors in server logs

---

## Monitoring Anti-Sniping Activity

### Server Logs to Watch For

```
🔄 Seeding test data...              ← DB init
✓ Anti-Sniping triggered: Extended   ← Extension happened
Updated end_time for auction         ← DB updated
```

### Client Alerts

```
⏱ Gia hạn thời gian
Vì có đấu giá trong phút cuối,
thời gian phiên đã được gia hạn thêm 5 phút!
```

### Card Display

- **Green** (safe): "⏱ Còn lại: 10 phút"
- **Red** (warning): "Còn lại: 3 phút"
- **Expired**: "⏱ Đã hết hạn"

---

## Performance Notes

- **DB Update**: Only when triggered (not every bid)
- **Network Overhead**: 1 extra field in JSON (`newEndTime`)
- **Thread Safety**: Synchronized on Auction object
- **Minimal Latency**: <50ms calculation + DB update

---

## Success Criteria

After running tests, verify:

| Criterion                             | Status |
| ------------------------------------- | ------ |
| Bids in last 5 min trigger extension  | or ❌  |
| End time extends by exactly 5 minutes | or ❌  |
| All clients notified of extension     | or ❌  |
| Database persists new end_time        | or ❌  |
| Multiple bids keep extending          | or ❌  |
| No extension >5 min remaining         | or ❌  |

---

## Troubleshooting Commands

```bash
# Check if server is running
lsof -i :8888

# View server logs (if redirected to file)
tail -f server.log

# Verify database connection
mysql -u root -p -e "SELECT id, name, end_time FROM auctions;"

# Restart from scratch
mvn clean install
java -cp target/classes com.daugia.ServerMain
```

---

## Expected Card Display

```
┌─────────────────────────┐
│                         │
│    [Product Image]      │
│                         │
├─────────────────────────┤
│ Lamborghini Aventador   │
│ 5,000,000,000 VNĐ       │
│ Còn lại: 3 phút      │  ← RED (Anti-snipe warning)
│ [Đang mở] [Đấu giá]     │
└─────────────────────────┘
```

---

**Last Updated**: May 12, 2026  
**Status**: Ready for Testing  
**Estimated Time**: 5-10 minutes per scenario
