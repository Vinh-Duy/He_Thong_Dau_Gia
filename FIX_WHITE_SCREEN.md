# 🔴 Fix Màn Hình Trắng - Hướng Dẫn Khắc Phục

**Ngày:** 2 Tháng 6, 2026  
**Vấn đề:** Deploy Server thành công nhưng Client không kết nối được

---

## 🎯 Vấn Đề Chính

Bạn deploy **Server** lên Render thành công, nhưng **Client không thể kết nối**:

```
❌ Frontend (Client) = Màn hình trắng / Không kết nối
✅ Backend (Server) = Chạy tốt trên Render
```

**Lý do:**
- Client cố connect tới `bidnova-server.onrender.com:8888` (raw TCP socket)
- Render dùng HTTPS reverse proxy (port 443)
- Raw socket connections (port 8888) **không đi được qua HTTP/HTTPS proxy**

---

## ✅ Giải Pháp: Chạy Local Development

### **Cách 1: Chạy Tất Cả Local (Đơn Giản Nhất)** 🎯

**macOS/Linux:**
```bash
bash /Users/vinhduy/Desktop/HeThongDauGia/run-dev.sh
```

**Windows (Command Prompt):**
```cmd
run-dev.bat
```

**Windows (PowerShell):**
```powershell
.\run-dev.ps1
```

**Cách hoạt động:**
1. Build Server & Client
2. Khởi động Server local (port 8888)
3. Khởi động Client (connect tới localhost:8888)
4. Khi đóng Client → Server cũng dừng

---

### **Cách 2: Chạy Server & Client Riêng (Để Debug)**

**Terminal 1 - Chạy Server:**
```bash
cd /Users/vinhduy/Desktop/HeThongDauGia
mvn clean package -f server/pom.xml -DskipTests
java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
```

Chờ đến thấy:
```
Server đang chạy tại port 8888...
```

**Terminal 2 - Chạy Client:**
```bash
cd /Users/vinhduy/Desktop/HeThongDauGia
bash run-client.sh
```

(Mặc định sẽ connect tới localhost:8888)

---

## 🌐 Kết Nối Render Server (Production)

### **Vấn đề Công Nghệ:**

Raw TCP socket không đi qua HTTP reverse proxy. Cần giải pháp:

#### **Option 1: WebSocket Adapter** (Recommended)
```
┌─────────────┐         ┌──────────────────────┐
│   Client    │ ←→ WS → │  Backend (Render)    │
│ (Socket)    │ HTTPS   │                      │
└─────────────┘         └──────────────────────┘
```

#### **Option 2: Custom HTTP Tunneling**
Modify Client to send commands via HTTP/REST instead of socket

#### **Option 3: Direct VPS**
Host server trên VPS riêng (không dùng Render proxy)

---

## 📝 Summary Các Files

### Chạy Local Development

| OS | Command |
|----|---------| 
| **macOS/Linux** | `bash run-dev.sh` |
| **Windows (CMD)** | `run-dev.bat` |
| **Windows (PowerShell)** | `.\run-dev.ps1` |

### Chạy Client Riêng (kết nối localhost)

| OS | Command |
|----|---------| 
| **macOS/Linux** | `bash run-client.sh` |
| **Windows (CMD)** | `run-client.bat` |
| **Windows (PowerShell)** | `.\run-client.ps1` |

---

## ❓ Thường Gặp

### **Q: Tại sao không kết nối được Render server?**
**A:** Raw TCP socket không đi qua HTTPS proxy. Render chỉ cho phép:
- ✅ HTTP/HTTPS traffic (port 443)
- ❌ Raw TCP (port 8888)

Giải pháp → Chạy local dev hoặc convert tới WebSocket

### **Q: Làm sao để dùng Render backend?**
**A:** Cần upgrade client code:
1. Replace raw Socket → WebSocket
2. Server broadcast via WebSocket
3. Client connect via `wss://bidnova-server.onrender.com`

**Estimated effort:** 2-3 hours

---

**Nếu cần giúp, báo lỗi cho mình!** 🚀
