# 🔥 Các Sự kiện QUAN TRỌNG trong Hệ thống Đấu giá

> Giải thích kiểu "người mới đọc cũng hiểu" — tập trung vào REALTIME.

---

## 📋 Danh sách 6 sự kiện chính

| # | Sự kiện | Ai làm? | Component chính | Có Realtime? |
|---|---------|---------|-----------------|-------------|
| 1 | **Đăng nhập** | User | `LoginPopupController` → `LoginHandler` | ❌ |
| 2 | **Đăng ký** | User | `SignUpController` → `RegisterHandler` | ❌ |
| 3 | **Đăng sản phẩm** | Seller | `AddProductController` → `AddProductHandler` | ❌ |
| 4 | **Sửa / Xóa sản phẩm** | Seller | `ManageProductController` → `Update/Delete Handler` | ❌ |
| 5 | **Đặt giá (Place Bid)** | Bidder | `ItemDetailController` → `PlaceBidHandler` | ✅ **CÓ** |
| 6 | **Xóa tài khoản** | Admin | `AdminUserController` → `DeleteUserHandler` | ❌ |

---

## ⚡ SỰ KIỆN 5: ĐẶT GIÁ + REALTIME (Quan trọng nhất)

### 🎯 Tại sao phải có Realtime?

Khi **User A** đặt giá, **User B** đang mở cùng 1 sản phẩm phải thấy giá nhảy lên **ngay lập tức**, không cần refresh.

### 🏃 Luồng chạy từng bước

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│   USER A    │────▶│   CLIENT A   │────▶│  NetworkClient  │
│  Bấm "Ra giá"     │(ItemDetail)  │     │  Gửi JSON qua   │
└─────────────┘     └──────────────┘     │     Socket      │
                                         └────────┬────────┘
                                                  │
                                         ┌────────▼────────┐
                                         │     SERVER      │
                                         │ ClientHandler A │
                                         └────────┬────────┘
                                                  │
                                         ┌────────▼────────┐
                                         │ PlaceBidHandler │
                                         │  1. Kiểm tra    │
                                         │  2. Lưu giá mới │
                                         │  3. Chạy AutoBid│
                                         └────────┬────────┘
                                                  │
                                         ┌────────▼────────┐
              ┌─────────────────────────│ ClientHandler.  │
              │     BROADCAST ALL         │ broadcastAll()  │
              │   (Gửi cho TẤT CẢ)       │  static method  │
              │                         └─────────────────┘
              │                                   │
    ┌─────────┴──────────┐              ┌──────────┴──────────┐
    │     USER B         │◀───────────│     CLIENT B        │
    │ Thấy giá tăng lên  │  Socket nhận│ (DanhSachSanPham)  │
    │  KHÔNG cần refresh!│  BID_UPDATE │  cập nhật label    │
    └────────────────────┘              └─────────────────────┘
```

### 🔧 Code cụ thể chạy ở đâu?

#### 1️⃣ Client gửi lên (ItemDetailController.java)
```java
// User bấm nút "Ra Giá"
Request req = new Request("PLACE_BID", bidData);
NetworkClient.getInstance().sendRequest(req);
```

#### 2️⃣ Server nhận + xử lý (PlaceBidHandler.java)
```java
public Response handle(Request req, AuthUserContext user) {
    // 1. Kiểm tra hợp lệ
    // 2. Lưu giá mới vào DB
    auctionDAO.updateHighestBid(auctionId, newPrice, bidder);

    // 3. Kích hoạt AutoBidService
    autoBidService.executeAutoBids(auctionId);

    // 4. Lấy giá CUỐI CÙNG sau auto-bid
    double finalPrice = auction.getCurrentHighestBid();

    // 5. 🔴 TẠO BID_UPDATE EVENT
    JsonObject event = new JsonObject();
    event.addProperty("action", "BID_UPDATE");
    event.addProperty("auctionId", auctionId);
    event.addProperty("newPrice", finalPrice);

    // 6. 🔴 PHÁT SÓNG cho TẤT CẢ clients
    ClientHandler.broadcastAll(event.toString());

    return new Response("SUCCESS", "Đặt giá thành công!", event);
}
```

#### 3️⃣ Server phát sóng (ClientHandler.java)
```java
public static void broadcastAll(String message) {
    // static cho phép gọi từ bất cứ đâu (Handler, Service...)
    for (PrintWriter writer : clientWriters) {
        writer.println(message);  // Gửi qua Socket
    }
}
```

#### 4️⃣ Client nhận + cập nhật UI (NetworkClient.java)
```java
// Thread riêng lắng nghe liên tục
try {
    String response = in.readLine();  // Đọc từ Server
    handleServerMessage(response);    // Xử lý message
} catch (IOException e) { ... }

// Hàm xử lý khi nhận được BID_UPDATE
private void handleServerMessage(String json) {
    JsonObject msg = JsonParser.parseString(json).getAsJsonObject();
    if ("BID_UPDATE".equals(msg.get("action").getAsString())) {
        int auctionId = msg.get("auctionId").getAsInt();
        double newPrice = msg.get("newPrice").getAsDouble();

        // 🔴 CẬP NHẬT UI NGAY LẬP TỨC
        Platform.runLater(() -> {
            // Cập nhật label giá trong màn hình hiện tại
            if (currentAuctionId == auctionId) {
                lblCurrentPrice.setText(String.format("%,.0f VNĐ", newPrice));
            }
        });
    }
}
```

### ⚠️ Tại sao phải dùng `Platform.runLater()`?

JavaFX chỉ cho phép thay đổi UI từ **JavaFX Application Thread**.
`NetworkClient` chạy trên **thread riêng** (lắng nghe Socket), nên phải wrap bằng `Platform.runLater()` để JavaFX thread cập nhật giao diện an toàn.

---

## 🔗 Các sự kiện khác chạy như thế nào?

### Sự kiện 1: Đăng nhập (Request – Response đơn giản)
```
Client: Request("LOGIN", {username, password})
        ─────Socket─────▶
Server: LoginHandler → UserDAO.checkLogin() → DB
        ─────Socket─────▶
Client: Response("SUCCESS", token) → Lưu token → Chuyển màn hình
```
**KHÔNG có realtime** vì chỉ liên quan 1 client.

### Sự kiện 3: Đăng sản phẩm (Seller)
```
AddProductController → Request("ADD_PRODUCT", productData)
        ─────Socket─────▶
Server: AddProductHandler → AuctionDAO.insert()
        ─────Socket─────▶
Client: Response("SUCCESS") → Quay về ManageProduct
```

### Sự kiện 6: Xóa tài khoản (Admin)
```
AdminUserController → Request("DELETE_USER", {userId})
        ─────Socket─────▶
Server: DeleteUserHandler → UserDAO.deleteUser()
        ─────Socket─────▶
Client: Response("SUCCESS") → Refresh bảng user
```

---

## 🧠 Tóm tắt kiến thức quan trọng để trình bày

| Câu hỏi | Câu trả lời |
|---------|-------------|
| **Realtime ở đâu?** | Chỉ có ở **Đặt giá (Place Bid)** |
| **Chạy như thế nào?** | `PlaceBidHandler` → `ClientHandler.broadcastAll()` → Mọi client nhận `BID_UPDATE` |
| **Tại sao dùng `static` broadcast?** | Handler không có reference đến instance ClientHandler cụ thể |
| **Client nhận ra sao?** | `NetworkClient` chạy thread riên lắng nghe Socket, `Platform.runLater()` cập nhật UI |
| **Có bao nhiêu thread?** | Mỗi client kết nối = 1 `ClientHandler` thread. Client có thread lắng nghe riêng. |

---

*Đọc thêm chi tiết kiến trúc tại `architecture.md`.*
