# 📐 Sơ đồ Kiến trúc Hệ thống Đấu giá

> Sơ đồ đơn giản, chia nhóm rõ ràng, không rối mắt.

---

## 1. 🏗️ Tổng quan Client – Server – Database

```mermaid
graph TB
    subgraph CLIENT["💻 CLIENT (JavaFX App)"]
        UI["🖼️ Giao diện (FXML + Controller)"]
        NET["📡 NetworkClient<br/>Singleton gửi/nhận"]
        SES["🔐 SessionManager<br/>Lưu token, username"]
    end

    subgraph SERVER["🖥️ SERVER (Java Socket)"]
        MAIN["ServerMain<br/>Port 8888"]
        CH["ClientHandler<br/>1 thread / client"]
        REG["HandlerRegistry<br/>Bảng phân công"]
        HD[(Handlers<br/>Login, PlaceBid, AddProduct...)]
        SVC[(Services<br/>AutoBidService)]
    end

    subgraph DB["🗄️ DATABASE (SQLite)"]
        T1["users"]
        T2["auctions"]
        T3["auto_bids"]
    end

    UI -->|click "Ra giá"| NET
    NET -->|Socket<br/>Request JSON| MAIN
    MAIN -->|accept| CH
    CH --> REG
    REG --> HD
    HD --> SVC
    HD -->|SQL| T1
    HD -->|SQL| T2
    HD -->|SQL| T3
    CH -->|Response JSON| NET
    NET -->|update label| UI
    NET -.->|lưu token| SES
```

**Giải thích ngắn:**
- **CLIENT**: Người dùng bấm nút → Controller gọi `NetworkClient` gửi JSON qua Socket.
- **SERVER**: `ServerMain` lắng nghe cổng 8888. Mỗi client kết nối → tạo 1 `ClientHandler` chạy riêng. `HandlerRegistry` tìm đúng Handler xử lý.
- **DB**: SQLite 3 bảng chính. Handler gọi DAO để đọc/ghi.

---

## 2. 🔄 Luồng xử lý 1 Request (ví dụ: Đăng nhập)

```mermaid
sequenceDiagram
    actor U as 👤 Người dùng
    participant C as LoginPopupController
    participant N as NetworkClient
    participant S as ClientHandler
    participant R as HandlerRegistry
    participant H as LoginHandler
    participant D as UserDAO
    participant DB as SQLite

    U->>C: Nhập username/password<br/>bấm "Đăng nhập"
    C->>N: new Request("LOGIN", json)
    N->>S: Gửi qua Socket
    S->>R: get("LOGIN")
    R-->>S: trả về LoginHandler
    S->>H: handle(request)
    H->>D: checkLogin(username, password)
    D->>DB: SELECT ... FROM users
    DB-->>D: trả kết quả
    D-->>H: User object
    H-->>S: Response("SUCCESS", token)
    S-->>N: Gửi response về
    N-->>C: Nhận response
    C->>C: SessionManager.login(token, user)
    C->>C: Chuyển màn HomeView
```

---

## 3. ⚡ Luồng Đặt giá + Auto-bid + Real-time Broadcast

```mermaid
sequenceDiagram
    actor A as 👤 User A
    actor B as 👤 User B<br/>(đang nhìn màn hình)
    participant C1 as ItemDetailController A
    participant N as NetworkClient
    participant S as ClientHandler A
    participant PB as PlaceBidHandler
    participant AS as AutoBidService
    participant DB as SQLite
    participant CH_B as ClientHandler B
    participant C2 as DanhSachSanPhamController B

    Note over A,DB: BƯỚC 1: User A đặt giá
    A->>C1: Nhập số tiền, bấm "Ra Giá"
    C1->>N: Request("PLACE_BID", {auctionId, amount})
    N->>S: Gửi lên Server
    S->>PB: Xử lý
    PB->>DB: UPDATE auctions SET current_highest_bid=...

    Note over PB,DB: BƯỚC 2: Kích hoạt Auto-bid
    PB->>AS: executeAutoBids(auctionId)
    AS->>DB: SELECT auto_bids WHERE maxBid > current
    AS->>DB: UPDATE giá mới
    AS-->>PB: Giá cuối cùng sau auto-bid

    Note over PB,CH_B: BƯỚC 3: PHÁT SÓNG cho mọi người
    PB->>PB: Tạo BID_UPDATE JSON
    PB->>N: ClientHandler.broadcastAll(msg)
    N->>CH_B: Nhận message qua Socket
    CH_B->>C2: Cập nhật label giá
    C2->>B: Thấy giá nhảy lên

    PB-->>S: Response("SUCCESS")
    S-->>N: Trả về A
    N-->>C1: Hiển thị "Đặt giá thành công"
```

---

## 4. 📦 Cấu trúc Package (tầng)

```mermaid
graph LR
    subgraph Client["📦 auctionclient"]
        V["views/ FXML"]
        CTRL["controllers/ JavaFX"]
        NET2["network/ Request, Response"]
        MOD1["models/ User, Auction"]
        UTL["utils/ SessionManager"]
    end

    subgraph Server["📦 auctionserver"]
        HAND["handlers/ ActionHandler"]
        DAO["dao/ UserDAO, AuctionDAO"]
        MOD2["models/ User, Auction"]
        SVC2["services/ AutoBidService"]
        DB2["database/ DatabaseConnection"]
    end

    CTRL -->|gọi| NET2
    NET2 -->|JSON Socket| HAND
    HAND -->|gọi| DAO
    HAND -->|gọi| SVC2
    DAO -->|JDBC| DB2
    MOD1 -.->|copy| MOD2
```

**3 tầng chính:**
1. **Presentation** (`views` + `controllers`) → Người dùng tương tác
2. **Network** (`network`) → Cầu nối Client–Server bằng JSON qua Socket
3. **Business + Data** (`handlers`, `services`, `dao`) → Xử lý logic + ghi DB

---

## 5. 🎯 Các Handler chính (Bảng tra nhanh)

| Handler | Action | Nhiệm vụ |
|---------|--------|----------|
| `LoginHandler` | `LOGIN` | Kiểm tra user, tạo token |
| `RegisterHandler` | `REGISTER` | Tạo tài khoản mới |
| `PlaceBidHandler` | `PLACE_BID` | Đặt giá + kích hoạt auto-bid + broadcast |
| `SetAutoBidHandler` | `SET_AUTO_BID` | Lưu cấu hình auto-bid |
| `AddProductHandler` | `ADD_PRODUCT` | Seller tạo sản phẩm mới |
| `UpdateProductHandler` | `UPDATE_PRODUCT` | Seller sửa sản phẩm |
| `DeleteProductHandler` | `DELETE_PRODUCT` | Seller xóa sản phẩm |
| `DeleteUserHandler` | `DELETE_USER` | Admin xóa tài khoản |
| `GetAllAuctionsHandler` | `GET_ALL_AUCTIONS` | Lấy danh sách tất cả đấu giá |
| `GetAuctionsByCategoryHandler` | `GET_AUCTIONS_BY_CATEGORY` | Lọc theo danh mục |
| `GetAuctionByIdHandler` | `GET_AUCTION_BY_ID` | Lấy chi tiết 1 sản phẩm |
| `GetMyAuctionsHandler` | `GET_MY_AUCTIONS` | Seller xem sản phẩm của mình |

---

## 6. 🗃️ Sơ đồ Database (3 bảng chính)

```mermaid
erDiagram
    USERS {
        int id PK
        string username
        string password
        string email
        string full_name
        string role
        string token
    }

    AUCTIONS {
        int id PK
        string name
        string description
        double start_price
        double current_highest_bid
        string highest_bidder
        string status
        string category
        string end_time
        int seller_id FK
    }

    AUTO_BIDS {
        int id PK
        int user_id FK
        int auction_id FK
        double max_bid
        double bid_increment
        boolean active
    }

    USERS ||--o{ AUCTIONS : "tạo (seller)"
    USERS ||--o{ AUTO_BIDS : "đặt auto-bid"
    AUCTIONS ||--o{ AUTO_BIDS : "có"
```

---

*File này dùng Mermaid syntax. Bạn có thể xem trực tiếp trong GitHub, VS Code (plugin Mermaid), hoặc [mermaid.live](https://mermaid.live).*
