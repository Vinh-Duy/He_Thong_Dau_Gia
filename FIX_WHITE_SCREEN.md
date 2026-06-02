# 🔴 Fix Màn Hình Trắng - Hướng Dẫn Khắc Phục

**Ngày:** 2 Tháng 6, 2026  
**Vấn đề:** Deploy Server thành công nhưng Client (Frontend) không được deploy

---

## 🎯 Vấn Đề Chính

Bạn deploy **Server** lên Render thành công, nhưng **Client không được deploy**:

```
❌ Frontend (Client) = Trắng tinh → Vì nó là JavaFX Desktop App
✅ Backend (Server) = Chạy tốt trên Render
```

**Lý do:**
- Client là ứng dụng Desktop (JavaFX) - cần GUI → không thể chạy trên cloud
- Server là Java Socket Server - chạy headless tốt → hoạt động bình thường

---

## ✅ Giải Pháp Nhanh: Chạy Client Locally

### **Bước 1: Build Client** 

```bash
# Vào thư mục project
cd /Users/vinhduy/Desktop/HeThongDauGia

# Build client JAR
mvn clean package -f client/pom.xml -DskipTests
```

### **Bước 2: Chạy Client Kết Nối Tới Render Server**

**Trên macOS/Linux:**
```bash
AUCTION_SERVER_HOST="bidnova-server.onrender.com" \
AUCTION_SERVER_PORT="8888" \
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Trên Windows (Command Prompt):**
```cmd
set AUCTION_SERVER_HOST=bidnova-server.onrender.com
set AUCTION_SERVER_PORT=8888
java -jar client\target\client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Trên Windows (PowerShell):**
```powershell
$env:AUCTION_SERVER_HOST = "bidnova-server.onrender.com"
$env:AUCTION_SERVER_PORT = "8888"
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### **Bước 3: Kiểm Tra Kết Nối** ✅

Nếu thấy dòng này = **Thành công!**
```
✅ Đã kết nối tới Server thành công!
```

---

## 🚀 Giải Pháp Dài Hạn: Tạo Web Frontend

Để deploy frontend trên cloud, bạn cần:

### **Option 1: Dùng WebSocket + HTML/CSS/JS**
```
┌─────────────┐         ┌──────────────────────┐
│   Browser   │ ←→ WS → │  Backend (Render)    │
│  (HTML/JS)  │         │  bidnova-server      │
└─────────────┘         └──────────────────────┘
```

### **Option 2: Spring Boot Web App**
```
┌────────────────────────┐
│  Render Web Service    │
│  ├─ Frontend (JSP)     │
│  └─ Backend APIs       │
└────────────────────────┘
```

---

## 🔧 Các Thay Đổi Đã Làm

### 1. **NetworkConfig.java** - Hỗ trợ Environment Variables
```java
✅ Trước: Hardcode localhost:8888 → Chỉ chạy local
✅ Sau:   Read từ env vars → Linh hoạt cho production
```

**Có thể config bằng:**
```bash
# Environment variables
AUCTION_SERVER_HOST=...
AUCTION_SERVER_PORT=...

# Hoặc Java system properties
-Dauction.server.host=...
-Dauction.server.port=...
```

---

## ❓ Thường Gặp

### **Q: Màn hình vẫn trắng?**
**A:** Kiểm tra logs trong terminal - nếu thấy lỗi kết nối:
```
❌ Không thể kết nối tới Server.
```

→ Điều đó có thể là vì:
1. ❌ Render server chưa khởi động
2. ❌ Host/port sai
3. ❌ Network bị chặn

**Fix:** 
```bash
# Kiểm tra backend chạy không
curl https://bidnova-server.onrender.com/health
# Nếu lỗi → Backend chưa khởi động, check Render logs
```

### **Q: Làm sao deploy client lên cloud?**
**A:** 
- JavaFX app không thể chạy trên headless server
- Cần convert thành web app (HTML/CSS/JS + WebSocket)
- Hoặc chạy locally, dùng script run-client-render.sh

---

## 📝 Summary

| Thành phần | Tình trạng | Giải pháp |
|-----------|-----------|---------|
| Server | ✅ Chạy trên Render | Sẵn sàng |
| Client (Desktop) | ❌ Không thể chạy trên cloud | Chạy locally |
| Frontend Web | ❌ Chưa có | Cần tạo (long-term) |

---

**Cần giúp gì thêm? Xin hãy báo!** 🚀
