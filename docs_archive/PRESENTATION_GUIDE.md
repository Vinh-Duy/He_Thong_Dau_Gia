# 📊 BidNova - Hệ Thống Đấu Giá Trực Tuyến
## Hướng Dẫn Thuyết Trình & Hiểu Sâu Mã Nguồn

---

## 📑 Mục Lục
1. [Tổng Quan Dự Án](#tổng-quan)
2. [Kiến Trúc Hệ Thống](#kiến-trúc)
3. [Cấu Trúc Thư Mục](#cấu-trúc-thư-mục)
4. [Công Nghệ Sử Dụng](#công-nghệ)
5. [Quy Trình Vận Hành Chính](#quy-trình-chính)
6. [Chi Tiết Các Chức Năng](#chi-tiết-chức-năng)
7. [Luồng Dữ Liệu & Giao Tiếp](#luồng-dữ-liệu)
8. [Các Thành Phần Chính](#các-thành-phần-chính)
9. [Bản Đồ Cơ Sở Dữ Liệu](#cơ-sở-dữ-liệu)
10. [Các Mẫu Thiết Kế](#design-patterns)
11. [Bảo Mật & Xác Thực](#bảo-mật)
12. [Xử Lý Luồng Đơn Vị](#threading)

---

## 🎯 Tổng Quan

### BidNova là gì?
BidNova là một **hệ thống đấu giá trực tuyến hiện đại**, cho phép:
- **Người bán (Seller):** Tạo phiên đấu giá cho sản phẩm (Xe, BĐS, Đồ cổ, Tài sản công)
- **Người mua (Bidder):** Đặt giá thủ công hoặc tự động với cập nhật giá thực tế
- **Quản trị viên (Admin):** Quản lý người dùng, phiên đấu giá, giao dịch

### Tính Năng Chính
✅ Xác thực người dùng (Đăng ký/Đăng nhập)  
✅ Tạo & Quản lý phiên đấu giá  
✅ Đặt giá thủ công với validation  
✅ **Đặt giá tự động (AutoBid)** - tăng giá tự động đến mức tối đa  
✅ **Giá trần (Price Ceiling)** - tự động kết thúc phiên  
✅ **Bước giá tối thiểu (Min Bid Increment)**  
✅ **Cập nhật thời gian thực** - tất cả client nhìn thấy ngay lập tức  
✅ **Lịch sử đấu giá** - audit trail đầy đủ  
✅ **Chống snipe** - mở rộng 5 phút nếu có giá cuối cùng  
✅ Multi-category sản phẩm  
✅ Phân quyền theo role (User/Seller/Admin)

---

## 🏗️ Kiến Trúc Hệ Thống

### Mô Hình Tổng Thể
```
┌─────────────────────────────────────────┐
│         CLIENT (JavaFX Desktop)         │
│  ┌──────────────────────────────────┐   │
│  │  Presentation Layer (FXML Views) │   │
│  │  - Admin Dashboard               │   │
│  │  - Auth Screen                   │   │
│  │  - Bidder Interface              │   │
│  │  - Seller Panel                  │   │
│  └──────────────────────────────────┘   │
│           ↕ (Socket Connection)         │
│  ┌──────────────────────────────────┐   │
│  │  Network Layer (JSON Protocol)   │   │
│  │  - NetworkClient (Singleton)     │   │
│  │  - Request/Response Serializer   │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
           ↕ TCP:8888
┌─────────────────────────────────────────┐
│      SERVER (Multi-threaded Socket)     │
│  ┌──────────────────────────────────┐   │
│  │  Request Dispatcher Layer        │   │
│  │  - ClientHandler (per-connection)│   │
│  │  - JWT Validation                │   │
│  │  - HandlerRegistry               │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Business Logic Layer (19+ Handlers) │
│  │  - PlaceBidHandler               │   │
│  │  - AutoBidService                │   │
│  │  - ItemFactory                   │   │
│  │  - Session Management            │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Data Access Layer (4 DAOs)      │   │
│  │  - UserDAO / AuctionDAO          │   │
│  │  - AutoBidDAO / BidHistoryDAO    │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Real-time Broadcast System      │   │
│  │  - ClientHandler.broadcastAll()  │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
           ↕ JDBC
┌─────────────────────────────────────────┐
│       MySQL Database (4 Tables)         │
│  - users                                │
│  - auctions                             │
│  - auto_bids                            │
│  - bidhistory                           │
└─────────────────────────────────────────┘
```

### Lớp Kiến Trúc (Layers)
| Lớp | Thành Phần | Trách Nhiệm |
|-----|-----------|-----------|
| **Presentation** | JavaFX Controllers + FXML | Giao diện người dùng, xử lý sự kiện |
| **Network** | JSON Protocol (TCP) | Giao tiếp Client-Server |
| **Business Logic** | 19+ Handlers, Services | Xử lý nghiệp vụ: đấu giá, auto-bid, validation |
| **Data Access** | 4 DAOs | Truy vấn DB, encapsulation SQL |
| **Database** | MySQL | Lưu trữ persistent data |

---

## 📁 Cấu Trúc Thư Mục

### Server
```
server/
├── src/main/java/com/bidnova/
│   ├── handlers/           (19 handlers - xử lý từng action)
│   │   ├── LoginHandler, RegisterHandler
│   │   ├── PlaceBidHandler ⭐ (xử lý đặt giá)
│   │   ├── SetAutoBidHandler, DeactivateAutoBidHandler
│   │   ├── AddProductHandler, UpdateProductHandler
│   │   ├── GetAllAuctionsHandler, GetAuctionByIdHandler
│   │   └── ... (12+ handlers khác)
│   ├── services/           (Business logic)
│   │   ├── AutoBidService ⭐ (tự động tăng giá)
│   │   ├── ItemFactory (tạo sản phẩm)
│   │   ├── AuctionService
│   │   └── ... (services khác)
│   ├── dao/                (Database access)
│   │   ├── UserDAO
│   │   ├── AuctionDAO
│   │   ├── AutoBidDAO
│   │   └── BidHistoryDAO
│   ├── models/             (Entity classes)
│   │   ├── User, Auction, AutoBid, BidHistory
│   │   ├── Vehicle, RealEstate, ArtCollectible, StateProperty
│   │   ├── Request, Response (JSON DTOs)
│   │   └── AuthUserContext
│   ├── network/            (Network layer)
│   │   ├── ServerMain (entry point, listen :8888)
│   │   ├── ClientHandler (per-client request handler)
│   │   ├── HandlerRegistry (mapping action → handler)
│   │   └── ActionHandler (interface)
│   └── utils/              (Utilities)
│       ├── JwtUtil (token generation/validation)
│       ├── PasswordUtil (BCrypt hashing)
│       └── DatabaseUtil (connection pool)
└── pom.xml
```

### Client
```
client/
├── src/main/java/com/bidnova/
│   ├── controllers/        (JavaFX Controllers - MVC)
│   │   ├── AuthController (Đăng nhập/Đăng ký)
│   │   ├── BidderController ⭐ (Giao diện người mua)
│   │   ├── SellerController (Giao diện người bán)
│   │   ├── AdminController (Bảng điều khiển)
│   │   └── ... (controllers khác)
│   ├── models/             (Client-side entities)
│   │   └── Ánh xạ từ server
│   ├── network/            (Client-side networking)
│   │   ├── NetworkClient (Singleton - Socket connection)
│   │   ├── Request/Response serializers
│   │   └── Broadcast listener
│   ├── config/             (Configuration)
│   │   ├── SessionManager (JWT token + user state)
│   │   └── Config constants
│   └── Main.java           (Entry point - JavaFX)
│
├── src/main/resources/
│   ├── views/              (FXML Layout files)
│   │   ├── auth/
│   │   │   ├── login.fxml
│   │   │   └── register.fxml
│   │   ├── bidder/         (Bidder UI)
│   │   │   ├── dashboard.fxml
│   │   │   ├── auction-detail.fxml
│   │   │   └── place-bid.fxml
│   │   ├── seller/         (Seller UI)
│   │   │   ├── my-auctions.fxml
│   │   │   ├── create-auction.fxml
│   │   │   └── product-form.fxml
│   │   ├── admin/          (Admin UI)
│   │   │   ├── user-management.fxml
│   │   │   └── auction-management.fxml
│   │   └── common/         (Shared components)
│   │
│   └── css/
│       ├── base-style.css  (Global styles)
│       ├── auth/, bidder/, seller/, admin/ (Role-specific styles)
│       └── components/     (Component styles)
│
└── pom.xml
```

---

## 🛠️ Công Nghệ Sử Dụng

| Thành Phần | Công Nghệ | Phiên Bản | Mục Đích |
|-----------|-----------|---------|---------|
| **Ngôn ngữ** | Java JDK | 25 | Lập trình chính |
| **Build Tool** | Maven | 4.0.0 | Quản lý dependencies & build |
| **Backend Framework** | Core Java Socket | Built-in | Multi-threaded server |
| **Frontend Framework** | JavaFX | 25 | UI desktop |
| **Database** | MySQL | 8.0+ | Persistent storage |
| **JDBC Driver** | MySQL Connector | 8.0.33 | Database connection |
| **JSON Serialization** | Gson | 2.10.1 | Request/Response serialization |
| **Password Hashing** | jbcrypt | 0.4 | Bcrypt password security |
| **Authentication Token** | JJWT | 0.11.5 | JWT token generation/validation |
| **Icon Library** | FontAwesomeFX | 4.7.0 | Icons UI |
| **Testing Framework** | JUnit 5 | 5.10.1 | Unit testing |
| **Mocking Library** | Mockito | 5.2.0 | Mock objects in tests |

---

## 🔄 Quy Trình Vận Hành Chính

### 1️⃣ Khởi Động Hệ Thống

**Server Startup:**
```
1. ServerMain.main() executed
2. ServerSocket listen on port 8888
3. Main thread loop: accept() client connections
4. Per-client: spawn new ClientHandler thread
5. ScheduledExecutor: scan expired auctions every 30 seconds
6. Ready to accept requests
```

**Client Startup:**
```
1. Main.main() → launch JavaFX
2. Load auth.fxml (login screen)
3. NetworkClient singleton initialized
4. Try connect to server:8888
5. Wait for user login
```

### 2️⃣ Chu Kỳ Yêu Cầu-Phản Hồi (Request-Response Cycle)

```
CLIENT                                SERVER
  │                                     │
  │  1. User Action (e.g., Place Bid)  │
  ├─────────────────────────────────→  │
  │  2. Serialize to JSON Request       │
  │  3. Send via Socket                 │
  │                                     │  4. ClientHandler receives
  │                                     │  5. Deserialize JSON
  │                                     │  6. Validate JWT token
  │                                     │  7. Get handler from registry
  │                                     │  8. Handler.handle()
  │                                     │  9. May update DB
  │                                     │  10. May broadcast to all clients
  │  ← ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ← 11. Send Response JSON
  │  12. Deserialize Response           │
  │  13. Update UI                      │
  │  14. Refresh display                │
  │                                     │
  │  (ALL OTHER CLIENTS)                │
  ├─ ← ─ ← Broadcast if PlaceBid ← ─ ─ │ 
  │  Update their displays too          │
  │                                     │
```

### 3️⃣ Giao Tiếp JSON Protocol

**Request Format:**
```json
{
  "action": "PLACE_BID",
  "payload": {
    "auctionId": 5,
    "bidAmount": 50000000
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response Format:**
```json
{
  "status": "SUCCESS",
  "message": "Bid placed successfully!",
  "data": {
    "newHighestBid": 50000000,
    "newHighestBidder": "john_bidder",
    "auctionStatus": "OPEN"
  }
}
```

**Broadcast Event (to all clients):**
```json
{
  "action": "BID_UPDATED",
  "data": {
    "auctionId": 5,
    "currentHighestBid": 50000000,
    "highestBidder": "john_bidder",
    "updateTime": "2026-06-02T14:30:45Z"
  }
}
```

---

## 🎯 Chi Tiết Các Chức Năng

### A. Đăng Ký & Đăng Nhập (Authentication)

**Quy Trình Đăng Ký:**
```
1. User fills: username, password, email, role, personal info
2. RegisterHandler.handle():
   ├─ Check username not exist
   ├─ Hash password with BCrypt
   ├─ Insert into users table
   └─ Return SUCCESS + login redirect
```

**Quy Trình Đăng Nhập:**
```
1. User enters: username, password
2. LoginHandler.handle():
   ├─ Find user by username in DB
   ├─ Compare password via BCrypt.check()
   ├─ If match:
   │  ├─ Generate JWT token (signed with secret)
   │  ├─ Store token in SessionManager (client)
   │  ├─ Redirect to dashboard (based on role)
   │  └─ Return SUCCESS + token + userInfo
   └─ Else: return UNAUTHORIZED
3. All subsequent requests include token in header
```

**Token Structure (JWT):**
```
Header: {"alg": "HS256", "typ": "JWT"}
Payload: {"username": "john_bidder", "role": "BIDDER", "exp": 1719950400}
Signature: HMAC_SHA256(Header + Payload, SECRET_KEY)
```

### B. Tạo Phiên Đấu Giá (Create Auction)

**Quy Trình:**
```
1. Seller click "Create Auction"
2. Fill form:
   ├─ Product details (name, description)
   ├─ Product type (Vehicle, RealEstate, ArtCollectible, StateProperty)
   ├─ Start price
   ├─ Price ceiling (maximum allow bid)
   ├─ Min bid increment (step size)
   ├─ Start time, End time
   └─ Product-specific fields
3. Submit → AddProductHandler + CreateAuctionHandler:
   ├─ Validate: seller_id = token.user_id
   ├─ Validate: startPrice > 0, ceiling > startPrice
   ├─ ItemFactory.createItem() → right product subclass
   ├─ Insert into auctions table
   ├─ Status = OPEN
   └─ Return SUCCESS + auctionId
4. Broadcast to all clients: NEW_AUCTION_CREATED
5. Display in "Available Auctions" list
```

**Database Insert:**
```sql
INSERT INTO auctions 
(name, description, start_price, current_highest_bid, 
 price_ceiling, min_bid_increment, category, seller_id, status)
VALUES 
('Xe Toyota Vios', 'Xe tư nhân sạch sẽ', 100000000, 100000000, 
 200000000, 5000000, 'VEHICLE', 3, 'OPEN')
```

### C. Đặt Giá Thủ Công (Place Bid) ⭐ Tính Năng Chính

**Quy Trình Chi Tiết:**
```
CLIENT SIDE:
1. Bidder views auction detail page
2. Sees current price + min increment
3. Proposes bid amount (must > current + min increment)
4. Click "Place Bid" button
5. NetworkClient.sendRequest("PLACE_BID", {auctionId, bidAmount}, token)
6. Show loading spinner

SERVER SIDE (PlaceBidHandler):
7. Validate token → get user_id
8. Check bid validations:
   ├─ Auction exists? YES
   ├─ Auction status = OPEN? YES
   ├─ Bidder not = seller? YES
   ├─ Bid > current highest? YES
   ├─ Bid >= current + min_increment? YES
   ├─ Bid <= price_ceiling? YES
   └─ All pass → continue
9. Update database:
   UPDATE auctions SET 
     current_highest_bid = NEW_BID,
     highest_bidder = BIDDER_USERNAME
   WHERE id = AUCTION_ID
10. Record in bidhistory:
   INSERT INTO bidhistory (auction_id, user_id, bid_amount, bid_time)
11. ⭐ TRIGGER AUTO-BIDS:
    AutoBidService.executeAutoBids(auctionId):
    ├─ Get all active auto-bids for this auction
    ├─ Sort by creation time (FIFO)
    ├─ For each auto-bid:
    │  ├─ Check: nextBid = current + increment
    │  ├─ If nextBid <= maxBid:
    │  │  ├─ Place auto-bid (same as PlaceBid, record as auto)
    │  │  ├─ Update auto_bid record with last_executed_time
    │  │  └─ Continue loop
    │  ├─ Else if nextBid >= priceCeiling:
    │  │  ├─ Place bid at ceiling
    │  │  ├─ Close auction (status = FINISHED)
    │  │  ├─ Deactivate auto-bid (is_active = false)
    │  │  └─ Break loop
    │  └─ Else: break loop
12. ClientHandler.broadcastAll() → send to ALL connected clients:
    {action: "BID_UPDATED", auctionId, newBid, highestBidder}
13. Return SUCCESS response to requesting client

CLIENT SIDE (ALL CLIENTS):
14. NetworkClient receive broadcast
15. If auction matches current viewed:
    ├─ Update display: price = new price
    ├─ Update leader label = new bidder
    ├─ Show animation/notification
    ├─ Auto-enable "Place Bid" if bidder is not highest
    └─ Auto-disable "Place Bid" if bidder is highest
16. If auction not currently viewed:
    └─ Queue update for when user navigates to it
```

**Validation Logic (Code Snippet Style):**
```
PlaceBidHandler.validate(bid, auction):
  if (bid <= auction.currentHighestBid)
    throw BidTooLowException()
  if (bid - auction.currentHighestBid < auction.minIncrement)
    throw IncrementTooSmallException()
  if (bid > auction.priceCeiling)
    throw BidExceedsCeilingException()
  if (auction.status != OPEN)
    throw AuctionNotOpenException()
  return OK
```

### D. Đặt Giá Tự Động (AutoBid) ⭐ Chức Năng Nâng Cao

**Khái Niệm:**
```
Bidder A: "I want to win. Let me bid up to 150M with step 5M"
→ System: "OK, if anyone bids, I'll auto-raise to next step for you"

Manual Bid:
  Current: 100M → Bid: 110M (meets increment)
  
AutoBid Activation:
  Bidder A rule: max=150M, increment=5M
  → Next auto bid = 110M + 5M = 115M
  → Check: 115M <= 150M? YES → Place 115M
  → Next: 115M + 5M = 120M
  → Continue until someone outbids or ceiling reached
```

**Quy Trình Chi Tiết:**

**1. Set AutoBid (Bidder side):**
```
Bidder click "Enable Auto Bid"
→ Dialog: "Max amount? Increment?"
→ Input: max=150M, increment=5M
→ SetAutoBidHandler.handle():
   ├─ Validate: max > current price
   ├─ Validate: increment > 0
   ├─ Insert into auto_bids table:
   │  (auction_id, user_id, max_bid, increment, is_active=true)
   ├─ Return SUCCESS
   └─ Show: "AutoBid enabled - will bid up to 150M"
```

**2. Execute AutoBid (Server side - when manual bid placed):**
```
PlaceBidHandler places manual bid → AUTOMATIC triggers:

AutoBidService.executeAutoBids(auctionId):
  1. SELECT all auto_bids WHERE 
       auction_id=X AND is_active=true 
     ORDER BY created_at ASC (FIFO)
  
  2. For each active auto-bid:
     ├─ current = auction.currentHighestBid
     ├─ nextAutoPrice = current + auto_bid.increment
     ├─ IF nextAutoPrice <= auto_bid.maxBid:
     │  ├─ PlaceBid(auctionId, auto_bid.user_id, nextAutoPrice)
     │  ├─ Record to bidhistory with type="AUTO"
     │  ├─ Broadcast: "User X auto-bid: Y"
     │  └─ Continue to next auto-bid
     │
     ├─ ELSE IF nextAutoPrice >= auction.priceCeiling:
     │  ├─ PlaceBid(auctionId, auto_bid.user_id, ceiling)
     │  ├─ UPDATE auctions SET status='FINISHED', end_time=NOW()
     │  ├─ UPDATE auto_bids SET is_active=false
     │  ├─ Broadcast: "Auction ended! Ceiling reached!"
     │  └─ BREAK (stop processing auto-bids)
     │
     └─ ELSE (nextAutoPrice > maxBid AND < ceiling):
        └─ Skip this auto-bid, try next one

  3. Return (may have executed 0, 1, or multiple auto-bids)
```

**3. Price Ceiling Trigger:**
```
If AutoBid execution reaches ceiling:
  ├─ Auction status = FINISHED
  ├─ End time = NOW (override scheduled end)
  ├─ All active auto-bids = DEACTIVATED
  ├─ Broadcast to all clients
  └─ Seller cannot extend
```

**4. View AutoBid Status:**
```
GetAutoBidHandler.handle(auctionId, userToken):
  ├─ Check if user has active auto-bid on this auction
  ├─ If yes:
  │  └─ Return: {maxBid, increment, isActive}
  └─ If no:
     └─ Return: {hasAutoBid: false}
```

**5. Deactivate AutoBid:**
```
DeactivateAutoBidHandler.handle(auctionId, userToken):
  ├─ UPDATE auto_bids SET is_active=false
  │   WHERE auction_id=X AND user_id=Y
  ├─ Return SUCCESS
  └─ Broadcast: "User X disabled auto-bid"
```

### E. Chống Snipe (Anti-Sniping)

**Khái Niệm:**
```
Sniping: Bidder places last-second bid to win without response time

Prevention: If bid placed within last 5 minutes → extend auction 5 more minutes
```

**Quy Trình:**
```
Current time: 14:25 (auction ends 14:30 = 5 min left)
Bidder places bid → Server logic:
  ├─ timeLeft = endTime - NOW
  ├─ IF timeLeft < 5 minutes:
  │  ├─ newEndTime = NOW + 5 minutes (extend 5 more)
  │  ├─ UPDATE auctions SET end_time = newEndTime
  │  ├─ Broadcast: "Bid received! Auction extended to 14:35"
  │  └─ All clients update countdown
  └─ Continue normal bid processing
```

---

## 🔗 Luồng Dữ Liệu & Giao Tiếp

### Sơ Đồ Giao Tiếp Thời Gian Thực

```
SCENARIO: User A places bid on Auction #5
Connected Clients: A, B, C (all viewing Auction #5)
                   D, E, F (viewing other auctions)

1. User A: Click "Place Bid: 50M"
   ├─ Client A → Server: {"action": "PLACE_BID", "auctionId": 5, "amount": 50M}
   
2. Server processes (PlaceBidHandler)
   ├─ Validate bid
   ├─ Update DB
   ├─ Check auto-bids
   ├─ ClientHandler.broadcastAll()
   │  └─ Send to ALL 6 connected clients:
   │     {"action": "BID_UPDATE", "auctionId": 5, "newBid": 50M, ...}
   
3. Response reaches all clients:
   ├─ Client A: "Your bid accepted!" + update UI + disable form
   ├─ Client B: "Price updated: 50M from User_A" + update display
   ├─ Client C: "Price updated: 50M from User_A" + update display
   ├─ Client D: Queue update (not viewing auction 5 currently)
   ├─ Client E: Queue update
   └─ Client F: Queue update

Result: INSTANT real-time update - no polling, no delay!
```

### JSON Flow Example

**Request 1 - User A Places Bid:**
```json
→ [CLIENT A]
{
  "action": "PLACE_BID",
  "payload": {
    "auctionId": 5,
    "bidAmount": 50000000
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

← [SERVER RESPONSE]
{
  "status": "SUCCESS",
  "message": "Bid placed successfully!",
  "data": {
    "auctionId": 5,
    "newHighestBid": 50000000,
    "highestBidder": "user_a",
    "autoExecuted": true,
    "executedAutoBids": [
      {"bidder": "user_b", "amount": 55000000}
    ]
  }
}

← [BROADCAST TO ALL CLIENTS]
{
  "type": "BROADCAST",
  "action": "BID_UPDATED",
  "data": {
    "auctionId": 5,
    "currentHighestBid": 55000000,
    "highestBidder": "user_b",
    "bidCount": 3,
    "updatedAt": "2026-06-02T14:35:22Z"
  }
}
```

---

## 🔧 Các Thành Phần Chính

### Server Components

#### 1. ServerMain.java
```java
public class ServerMain {
  public static void main(String[] args) {
    ServerSocket serverSocket = new ServerSocket(8888);
    
    // Accept connections infinitely
    while (true) {
      Socket clientSocket = serverSocket.accept();
      ClientHandler handler = new ClientHandler(clientSocket);
      
      // Each client gets dedicated thread
      new Thread(handler).start();
    }
  }
}
```
**Trách Nhiệm:**
- Listen on port 8888
- Accept client connections
- Spawn ClientHandler thread per connection

#### 2. ClientHandler.java (⭐ Key Component)
```java
public class ClientHandler implements Runnable {
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private static Set<PrintWriter> clientWriters; // Broadcast hub
  
  @Override
  public void run() {
    while (true) {
      String jsonLine = in.readLine(); // Read JSON request
      
      // Parse Request
      Request request = gson.fromJson(jsonLine, Request.class);
      
      // Validate JWT token
      String username = JwtUtil.validateAndExtractUsername(request.token);
      
      // Route to appropriate handler
      ActionHandler handler = HandlerRegistry.getHandler(request.action);
      Response response = handler.handle(request, username);
      
      // Send response to requesting client
      out.println(gson.toJson(response));
      
      // Broadcast if needed
      if (request.action.equals("PLACE_BID")) {
        broadcastAll(buildBroadcastJson(request));
      }
    }
  }
  
  // Static method - sends message to ALL connected clients
  public static synchronized void broadcastAll(String jsonMessage) {
    for (PrintWriter writer : clientWriters) {
      writer.println(jsonMessage);
      writer.flush();
    }
  }
}
```
**Trách Nhiệm:**
- Read JSON requests from client socket
- Validate JWT tokens
- Route to correct handler
- Send responses
- Broadcast updates to ALL clients

#### 3. PlaceBidHandler.java (⭐ Core Logic)
```java
public class PlaceBidHandler implements ActionHandler {
  
  @Override
  public Response handle(Request request, String username) {
    // 1. Parse payload
    int auctionId = request.payload.get("auctionId");
    long bidAmount = request.payload.get("bidAmount");
    
    // 2. Get user & auction
    User bidder = userDAO.findByUsername(username);
    Auction auction = auctionDAO.getAuctionById(auctionId);
    
    // 3. Validate
    if (bidAmount <= auction.currentHighestBid)
      throw new BidTooLowException();
    
    if ((bidAmount - auction.currentHighestBid) < auction.minIncrement)
      throw new IncrementTooSmallException();
    
    if (bidAmount > auction.priceCeiling)
      throw new BidExceedsCeilingException();
    
    // 4. Update DB
    auction.setCurrentHighestBid(bidAmount);
    auction.setHighestBidder(username);
    auctionDAO.update(auction);
    
    // 5. Record history
    BidHistory history = new BidHistory(auctionId, bidder.id, bidAmount);
    bidHistoryDAO.insert(history);
    
    // 6. ⭐ EXECUTE AUTO-BIDS
    AutoBidService.executeAutoBids(auctionId);
    
    // 7. Response
    return new Response("SUCCESS", "Bid placed!", 
      {newBid: bidAmount, highestBidder: username});
  }
}
```

#### 4. AutoBidService.java (⭐ Auto Bid Logic)
```java
public class AutoBidService {
  
  public static void executeAutoBids(int auctionId) {
    Auction auction = auctionDAO.getAuctionById(auctionId);
    List<AutoBid> activeBids = autoBidDAO.getActiveByAuction(auctionId);
    
    // Sort by creation time (FIFO)
    Collections.sort(activeBids, (a, b) -> 
      a.createdAt.compareTo(b.createdAt));
    
    for (AutoBid autoBid : activeBids) {
      long nextBidPrice = auction.currentHighestBid + autoBid.increment;
      
      // Check if can bid within limit
      if (nextBidPrice <= autoBid.maxBid) {
        // Execute auto-bid
        auction.currentHighestBid = nextBidPrice;
        auction.highestBidder = autoBid.userUsername;
        auctionDAO.update(auction);
        
        // Record as auto-bid
        BidHistory history = new BidHistory(auctionId, autoBid.userId, 
          nextBidPrice, BidType.AUTO);
        bidHistoryDAO.insert(history);
      }
      // Check if exceeded ceiling
      else if (nextBidPrice >= auction.priceCeiling) {
        // Place at ceiling & close auction
        auction.currentHighestBid = auction.priceCeiling;
        auction.status = AuctionStatus.FINISHED;
        auction.endTime = LocalDateTime.now();
        auctionDAO.update(auction);
        
        // Deactivate auto-bid
        autoBid.isActive = false;
        autoBidDAO.update(autoBid);
        
        break; // Stop processing
      }
    }
  }
}
```

#### 5. HandlerRegistry.java (⭐ Router)
```java
public class HandlerRegistry {
  private static Map<String, ActionHandler> handlers = new HashMap<>();
  
  static {
    handlers.put("LOGIN", new LoginHandler());
    handlers.put("PLACE_BID", new PlaceBidHandler());
    handlers.put("SET_AUTO_BID", new SetAutoBidHandler());
    handlers.put("GET_AUCTION", new GetAuctionByIdHandler());
    handlers.put("CREATE_AUCTION", new CreateAuctionHandler());
    // ... 14+ more handlers
  }
  
  public static ActionHandler getHandler(String action) {
    ActionHandler handler = handlers.get(action);
    if (handler == null)
      throw new UnknownActionException(action);
    return handler;
  }
}
```

### Client Components

#### 1. NetworkClient.java (⭐ Singleton)
```java
public class NetworkClient {
  private static NetworkClient instance; // Singleton
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  
  private NetworkClient() {
    // Singleton: private constructor
  }
  
  public static synchronized NetworkClient getInstance() {
    if (instance == null) {
      instance = new NetworkClient();
      instance.connect("localhost", 8888);
      instance.startListeningForBroadcasts();
    }
    return instance;
  }
  
  public Response sendRequest(Request request) {
    // Serialize & send
    String json = gson.toJson(request);
    out.println(json);
    out.flush();
    
    // Wait for response
    String responseJson = in.readLine();
    return gson.fromJson(responseJson, Response.class);
  }
  
  private void startListeningForBroadcasts() {
    new Thread(() -> {
      while (true) {
        String broadcastJson = in.readLine();
        // Parse & dispatch to UI
        handleBroadcast(gson.fromJson(broadcastJson, BroadcastEvent.class));
      }
    }).start();
  }
  
  private void handleBroadcast(BroadcastEvent event) {
    if (event.action.equals("BID_UPDATED")) {
      // Update all open auction views
      BidderController.updateAuctionDisplay(event.data);
    }
  }
}
```

#### 2. BidderController.java (⭐ Main UI)
```java
@FXML
private Label priceLabel;

@FXML
private TextField bidInput;

@FXML
private Button placeBidButton;

@FXML
private void handlePlaceBid() {
  long bidAmount = Long.parseLong(bidInput.getText());
  
  // Show loading
  placeBidButton.setDisable(true);
  placeBidButton.setText("Processing...");
  
  // Send to server
  new Thread(() -> {
    try {
      Request request = new Request("PLACE_BID", 
        {auctionId, bidAmount}, 
        SessionManager.getToken());
      
      Response response = NetworkClient.getInstance()
        .sendRequest(request);
      
      // Update UI on main thread
      Platform.runLater(() -> {
        if (response.status.equals("SUCCESS")) {
          showAlert("Success!", "Bid accepted!");
          bidInput.clear();
        } else {
          showAlert("Error!", response.message);
        }
        placeBidButton.setDisable(false);
        placeBidButton.setText("Place Bid");
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }).start();
}

// Receive real-time broadcast from server
public static void updateAuctionDisplay(Map<String, Object> data) {
  Platform.runLater(() -> {
    int auctionId = (int) data.get("auctionId");
    long newPrice = (long) data.get("currentHighestBid");
    String bidder = (String) data.get("highestBidder");
    
    if (currentAuctionId == auctionId) {
      priceLabel.setText(formatCurrency(newPrice));
      bidderLabel.setText("Leader: " + bidder);
      
      // Animation
      priceLabel.setStyle("-fx-text-fill: #ff6b6b;");
      // ... animation code
    }
  });
}
```

#### 3. SessionManager.java (Token Storage)
```java
public class SessionManager {
  private static String authToken;
  private static String currentUsername;
  private static UserRole userRole;
  
  public static void login(String token, String username, UserRole role) {
    authToken = token;
    currentUsername = username;
    userRole = role;
  }
  
  public static String getToken() {
    return authToken;
  }
  
  public static String getUsername() {
    return currentUsername;
  }
  
  public static UserRole getRole() {
    return userRole;
  }
}
```

---

## 💾 Cơ Sở Dữ Liệu

### Schema Overview

```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,  -- BCrypt hash
  role ENUM('USER', 'SELLER', 'ADMIN') NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  gender VARCHAR(10),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auctions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  start_price BIGINT NOT NULL,
  current_highest_bid BIGINT NOT NULL,
  highest_bidder VARCHAR(50),
  seller_id INT NOT NULL,
  category VARCHAR(50) NOT NULL,  -- VEHICLE, REALESTATE, etc
  price_ceiling BIGINT,  -- ⭐ Maximum allowed bid
  min_bid_increment BIGINT NOT NULL,  -- ⭐ Minimum step
  status ENUM('OPEN', 'FINISHED', 'CLOSED') DEFAULT 'OPEN',
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE auto_bids (
  id INT PRIMARY KEY AUTO_INCREMENT,
  auction_id INT NOT NULL,
  user_id INT NOT NULL,
  max_bid BIGINT NOT NULL,  -- Maximum auto-bid amount
  increment BIGINT NOT NULL,  -- Bid step
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (auction_id) REFERENCES auctions(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE bidhistory (
  id INT PRIMARY KEY AUTO_INCREMENT,
  auction_id INT NOT NULL,
  user_id INT NOT NULL,
  bid_amount BIGINT NOT NULL,
  bid_type ENUM('MANUAL', 'AUTO') DEFAULT 'MANUAL',
  bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (auction_id) REFERENCES auctions(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Example Data:
INSERT INTO users VALUES
(1, 'seller_john', 'bcrypt_hash_xxx', 'SELLER', 'john@example.com', 'John Seller', '0901234567', 'M'),
(2, 'bidder_alice', 'bcrypt_hash_yyy', 'USER', 'alice@example.com', 'Alice Bidder', '0987654321', 'F'),
(3, 'bidder_bob', 'bcrypt_hash_zzz', 'USER', 'bob@example.com', 'Bob Bidder', '0912345678', 'M');

INSERT INTO auctions VALUES
(5, 'Xe Toyota Vios 2020', 'Xe sạch, ít sử dụng', 100000000, 55000000, 'bidder_bob', 1, 'VEHICLE', 200000000, 5000000, 'OPEN', '2026-06-02 10:00:00', '2026-06-02 14:30:00');

INSERT INTO auto_bids VALUES
(1, 5, 2, 150000000, 5000000, true, '2026-06-02 10:15:00');  -- Alice's auto-bid

INSERT INTO bidhistory VALUES
(1, 5, 2, 50000000, 'MANUAL', '2026-06-02 10:20:00'),
(2, 5, 3, 55000000, 'AUTO', '2026-06-02 10:20:05');  -- Auto-bid triggered
```

### Entity Relationships

```
users (1) ──── (N) auctions (seller_id)
users (1) ──── (N) auto_bids (user_id)
users (1) ──── (N) bidhistory (user_id)

auctions (1) ──── (N) auto_bids
auctions (1) ──── (N) bidhistory
```

---

## 🎨 Design Patterns

### 1. **Singleton Pattern**
```java
// NetworkClient - Ensures single connection to server
public class NetworkClient {
  private static NetworkClient instance;
  
  private NetworkClient() { } // Private constructor
  
  public static synchronized NetworkClient getInstance() {
    if (instance == null) {
      instance = new NetworkClient();
    }
    return instance;
  }
}

// Usage: NetworkClient.getInstance().sendRequest(...)
```
**Tác Dụng:** Tránh tạo nhiều kết nối socket, tập trung quản lý

### 2. **Factory Pattern**
```java
// ItemFactory - Creates right product subclass
public class ItemFactory {
  public static Item createItem(String category, Map<String, Object> data) {
    switch (category) {
      case "VEHICLE":
        return new Vehicle(data);
      case "REALESTATE":
        return new RealEstate(data);
      case "ARTCOLLECTIBLE":
        return new ArtCollectible(data);
      case "STATEPROPERTY":
        return new StateProperty(data);
      default:
        throw new IllegalArgumentException();
    }
  }
}

// Usage: Item product = ItemFactory.createItem("VEHICLE", {brand, model, ...});
```
**Tác Dụng:** Tách logic tạo object, dễ mở rộng loại sản phẩm

### 3. **Strategy Pattern**
```java
// ActionHandler interface - different strategies for each action
public interface ActionHandler {
  Response handle(Request request, String username);
}

// Different implementations for each action type
public class PlaceBidHandler implements ActionHandler { ... }
public class LoginHandler implements ActionHandler { ... }
public class CreateAuctionHandler implements ActionHandler { ... }

// Registry routes to correct strategy
ActionHandler handler = HandlerRegistry.getHandler("PLACE_BID");
Response response = handler.handle(request, username);
```
**Tác Dụng:** Dễ thêm handler mới, tách từng concern

### 4. **Observer Pattern**
```java
// ClientHandler broadcasts to all observers (clients)
public class ClientHandler {
  private static Set<PrintWriter> clientWriters;
  
  public static void broadcastAll(String message) {
    for (PrintWriter writer : clientWriters) {
      writer.println(message);  // Notify all observers
    }
  }
}

// Client receives & updates
private void handleBroadcast(BroadcastEvent event) {
  // Update UI when event received
  updateAuctionDisplay(event.data);
}
```
**Tác Dụng:** Real-time thông báo tất cả client mà không polling

### 5. **DAO Pattern**
```java
// Data Access Object - Encapsulate DB operations
public class AuctionDAO {
  public Auction getAuctionById(int id) { ... }
  public void update(Auction auction) { ... }
  public List<Auction> getAll() { ... }
}

// Business logic uses DAO, not direct SQL
public class PlaceBidHandler implements ActionHandler {
  private AuctionDAO auctionDAO;
  
  Auction auction = auctionDAO.getAuctionById(id);  // Use DAO
}
```
**Tác Dụng:** Tập trung quản lý SQL, dễ thay đổi database

### 6. **Registry Pattern**
```java
// HandlerRegistry - Central mapping of actions to handlers
public class HandlerRegistry {
  private static Map<String, ActionHandler> handlers;
  
  static {
    handlers.put("LOGIN", new LoginHandler());
    handlers.put("PLACE_BID", new PlaceBidHandler());
    // ... register all handlers
  }
  
  public static ActionHandler getHandler(String action) {
    return handlers.get(action);
  }
}

// Usage in ClientHandler
ActionHandler handler = HandlerRegistry.getHandler(request.action);
```
**Tác Dụng:** Linh hoạt routing, dễ thêm handler

---

## 🔒 Bảo Mật & Xác Thực

### Authentication Flow

```
┌─────────────────────────────┐
│ 1. User enters credentials  │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ 2. LoginHandler validates   │
│    - Find user by username  │
│    - BCrypt.check(password) │
└──────────────┬──────────────┘
               │
        ┌──────┴──────┐
        │             │
    ✗ INVALID    ✓ MATCH
        │             │
        ▼             ▼
    Error      ┌──────────────┐
               │ 3. Generate  │
               │    JWT Token │
               │    + Secret  │
               └──────┬───────┘
                      │
                      ▼
            ┌──────────────────┐
            │ 4. Return token  │
            │    to client     │
            └──────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ 5. Store in          │
        │    SessionManager    │
        └──────┬───────────────┘
               │
               ▼
    ┌─────────────────────────┐
    │ 6. All requests include │
    │    token in header      │
    └──────┬──────────────────┘
           │
           ▼
    ┌────────────────────────┐
    │ 7. Server validates    │
    │    JWT.extract claims  │
    └────────────────────────┘
```

### BCrypt Password Hashing

```java
// Registration: Hash password
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
userDAO.insert(new User(username, hashedPassword, ...));

// Login: Compare password
boolean isCorrect = BCrypt.checkpw(plainPassword, storedHash);
// Result: true (hashes match) or false (don't match)
```

**Bảo Mật:**
- Plain text password NEVER stored
- Each hash is unique (even same password hashes differently)
- Extremely slow to brute force (computationally expensive)

### JWT Token Structure

```
Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"username": "john_bidder", "role": "BIDDER", "exp": 1719950400}
Signature: HMAC_SHA256(base64(Header) + "." + base64(Payload), SECRET_KEY)

Full Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6I...

Verification:
1. Split token by "."
2. Verify signature using SECRET_KEY
3. Check expiration time (exp claim)
4. Extract username from payload
```

### SQL Injection Prevention

```java
// ❌ UNSAFE - Concatenation (vulnerable)
String query = "SELECT * FROM users WHERE username = '" + username + "'";
// Attacker: username = "' OR '1'='1" → Always returns all users!

// ✅ SAFE - Prepared Statement (parameterized)
String query = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = connection.prepareStatement(query);
stmt.setString(1, username);  // Parameter safely bound
ResultSet rs = stmt.executeQuery();
// Attacker input treated as literal string, not code
```

**BidNova Protection:** All DAOs use PreparedStatement

### Authorization by Role

```java
// Each handler checks user role from JWT
public class DeleteUserHandler implements ActionHandler {
  @Override
  public Response handle(Request request, String username) {
    User currentUser = userDAO.findByUsername(username);
    
    // ✓ Only ADMIN can delete users
    if (currentUser.role != UserRole.ADMIN) {
      throw new UnauthorizedException("Admin only");
    }
    
    // Proceed with deletion
    ...
  }
}
```

---

## 🧵 Xử Lý Luồng Đơn Vị (Threading)

### Server Threading Model

```
Main Thread
    │
    └─→ ServerSocket.accept() → loop
        ├─ Accept client 1 → new ClientHandler → new Thread A
        ├─ Accept client 2 → new ClientHandler → new Thread B
        ├─ Accept client 3 → new ClientHandler → new Thread C
        └─ Accept client 4 → new ClientHandler → new Thread D

Background Thread (ScheduledExecutor)
    └─→ Every 30 seconds
        └─→ Scan & finish expired auctions

Thread Pool
    ├─ Thread A: Handle requests from Client 1
    ├─ Thread B: Handle requests from Client 2
    ├─ Thread C: Handle requests from Client 3
    └─ Thread D: Handle requests from Client 4

Shared Resource (Broadcast)
    ├─ Static Set<PrintWriter> clientWriters
    ├─ Accessed by multiple threads
    └─ Protected by synchronized block
```

### Example: Thread-Safe Broadcast

```java
// ❌ NOT THREAD-SAFE (Race condition)
public static void broadcastAll(String message) {
  for (PrintWriter writer : clientWriters) {  // What if set modified here?
    writer.println(message);
  }
}

// ✅ THREAD-SAFE
public static synchronized void broadcastAll(String message) {
  for (PrintWriter writer : clientWriters) {  // Locked - safe
    writer.println(message);
  }
}

// When Thread A broadcasts while Thread B adding new client?
synchronized method ensures only one thread at a time
```

### Client Threading Model

```
JavaFX Main Thread
    │
    ├─ UI Event Handling
    │  ├─ User clicks "Place Bid"
    │  └─ Can't block here (freezes UI)
    │
    └─ Dispatch to background thread
       └─ Send request to server
          └─ Wait for response (long operation)

Background Thread
    ├─ Block on server response
    ├─ Process response
    └─ Platform.runLater(() -> updateUI())  // Switch back to UI thread

NetworkClient Receive Thread
    ├─ Listen for broadcasts
    ├─ Parse incoming message
    └─ Platform.runLater(() -> updateDisplay())  // Update UI safely
```

### Code Example

```java
// UI Thread - WRONG (freezes UI)
@FXML
private void handlePlaceBid() {
  // ❌ This blocks the UI thread!
  Response response = networkClient.sendRequest(request);
  updateUI(response);  // UI frozen for 1-2 seconds!
}

// UI Thread - CORRECT (background thread)
@FXML
private void handlePlaceBid() {
  new Thread(() -> {
    // ✓ Run in background, doesn't freeze UI
    Response response = networkClient.sendRequest(request);
    
    // Switch to UI thread for updates
    Platform.runLater(() -> {
      updateUI(response);  // Safe update
    });
  }).start();
}
```

---

## 📊 Complete Feature Workflow Diagram

### Auction Lifecycle

```
1. CREATION
   Seller creates → Status: OPEN → Broadcast to all clients
   Clients see in "Available Auctions" list

2. BIDDING PHASE
   Bidder 1 places bid → Auto-bid execution → Broadcast
   Bidder 2 sees update → Can place higher bid → Auto-bid execution
   ... (repeat many times)

3. EXTENDED BY SNIPE
   Bid placed in last 5 min → Extend end_time +5min
   Continue bidding...

4. CEILING REACHED
   Auto-bid reaches price_ceiling → Status: FINISHED
   Automatic closure, no extension

5. TIME RUNS OUT
   end_time <= NOW → Status: FINISHED
   Winner determined (highest bidder)
   Email notification

6. ADMIN CLOSES
   Admin action → Status: CLOSED (overrides scheduled end)
   Can prevent further bids
```

### Bid Placement Sequence

```
Bidder places 50M on Auction #5
│
├─ Client validates: 50M > current + minimum?
├─ Send Request{PLACE_BID, 50M, token}
│
└─ Server (PlaceBidHandler)
   ├─ Validate JWT token
   ├─ Validate 50M > current + minimum?
   ├─ Validate 50M < ceiling?
   ├─ Update DB: current = 50M, leader = Bidder
   ├─ Record bidhistory
   │
   ├─ ⭐ AutoBidService.executeAutoBids()
   │  └─ For each active auto-bid:
   │     ├─ nextBid = 50M + increment
   │     ├─ If nextBid <= maxBid: Place auto-bid
   │     ├─ If nextBid >= ceiling: Close auction
   │     └─ Else: Stop
   │
   ├─ ClientHandler.broadcastAll(BID_UPDATE)
   │
   └─ Send Response SUCCESS back to Bidder
      └─ All clients receive broadcast
         └─ Update displays simultaneously
```

---

## 🎓 Tổng Kết Kiến Thức

### Các Khái Niệm Chính

| Khái Niệm | Định Nghĩa |
|-----------|-----------|
| **Socket** | Kết nối TCP giữa client và server |
| **JSON Protocol** | Định dạng dữ liệu trao đổi qua socket |
| **JWT Token** | Token xác thực, chứa username + role + expiry |
| **BCrypt** | Hash mật khẩu an toàn, không thể reverse |
| **Broadcast** | Gửi message đến TẤT CẢ client đã kết nối |
| **Auto-Bid** | Tự động tăng giá theo quy tắc của người dùng |
| **Price Ceiling** | Giới hạn giá tối đa, tự động kết thúc phiên |
| **Min Increment** | Bước giá tối thiểu cho mỗi lần đặt giá |
| **Anti-Sniping** | Mở rộng thời gian nếu bid trong 5 phút cuối |
| **Thread Pool** | Gồm nhiều thread xử lý client song song |
| **Singleton** | Chỉ 1 instance duy nhất trong app |
| **Factory** | Tạo object từ đúng subclass |

### Luồng Dữ Liệu Chính

```
User Action → Client UI → NetworkClient Socket 
→ Server:8888 → ClientHandler thread → HandlerRegistry 
→ Correct Handler → Business Logic + Services 
→ Database (SQL) → Response JSON → Broadcasting 
→ All Clients Update Display
```

### Performance Highlights

- **Real-time Updates:** 0 delay broadcast (no polling)
- **Concurrent Users:** Multi-threaded server handles N clients simultaneously
- **Secure:** JWT tokens + BCrypt hashing + SQL injection prevention
- **Scalable:** Handler registry pattern, easy to add features
- **Testable:** Separated concerns (handlers, services, DAOs)

---

**Sử Dụng tài liệu này để:**
- 📝 Thuyết trình về kiến trúc
- 📚 Giải thích cách code vận hành
- 🎓 Onboard developer mới
- 🐛 Debug issues
- 📈 Lập kế hoạch nâng cấp
