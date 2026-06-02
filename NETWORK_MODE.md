# 🌐 Network Mode - Chạy Server & Client Trên 2 Máy Khác Nhau

**Ngày:** 2 Tháng 6, 2026

---

## ✅ Yes! Hoàn toàn Có Được! 

Bạn có thể chạy:
- **Máy A:** Server
- **Máy B:** Client (1, 2, 3... nhiều client cùng lúc)

```
┌─────────────────┐         ┌──────────────────┐
│  Máy A (Server) │         │  Máy B (Client)  │
│  192.168.1.100  │ ←→ TCP  │  192.168.1.101   │
│  port 8888      │         │                  │
└─────────────────┘         └──────────────────┘
```

---

## 🚀 Setup Network Mode

### **Bước 1: Máy A - Khởi Động Server**

**macOS/Linux:**
```bash
bash /path/to/HeThongDauGia/run-server.sh
```

**Windows CMD:**
```cmd
run-server.bat
```

**Windows PowerShell:**
```powershell
.\run-server.ps1
```

**Server khởi động, thấy:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📍 SERVER IP: 192.168.1.100
🔌 PORT: 8888
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Server đang chạy tại port 8888...
```

**Ghi nhớ IP:** `192.168.1.100`

---

### **Bước 2: Máy B - Khởi Động Client Kết Nối Server**

**macOS/Linux:**
```bash
bash /path/to/HeThongDauGia/run-client-network.sh 192.168.1.100 8888
```

**Windows CMD:**
```cmd
run-client-network.bat 192.168.1.100 8888
```

**Windows PowerShell:**
```powershell
.\run-client-network.ps1 -ServerIP 192.168.1.100 -ServerPort 8888
```

---

## 🔍 Cách Tìm IP Address Máy Server

### **macOS/Linux:**
```bash
ifconfig | grep "inet "
```

Tìm dòng có dạng: `192.168.x.x` hoặc `10.0.x.x`

### **Windows:**
```cmd
ipconfig
```

Tìm "IPv4 Address"

### **Cách Dễ Nhất:**
Server sẽ tự in ra IP khi khởi động:
```
📍 SERVER IP: 192.168.1.100
```

---

## ⚠️ Chú Ý Quan Trọng

### **1. Firewall**
- ✅ Máy A phải cho phép port 8888 (incoming traffic)
- ✅ Hoặc disable firewall tạm thời (trong cùng network)

### **2. Network**
- ✅ Máy A & B phải **cùng WiFi network**
- ❌ Không thể qua Internet router (khi đó dùng Render backend)

### **3. Lỗi "Connection refused"**
```
java.net.ConnectException: Connection refused
```
→ Server chưa khởi động hoặc IP sai

### **4. Lỗi "No route to host"**
```
java.net.NoRouteToHostException
```
→ IP máy server sai hoặc không cùng network

---

## 📝 Quick Reference

| Máy | Lệnh |
|-----|------|
| **Server** (macOS/Linux) | `bash run-server.sh` |
| **Server** (Windows) | `run-server.bat` |
| **Client** (macOS/Linux) | `bash run-client-network.sh <SERVER_IP> <PORT>` |
| **Client** (Windows) | `run-client-network.bat <SERVER_IP> <PORT>` |

---

## 🎯 Ví Dụ Full Setup

**Máy A - MacBook:**
```bash
bash run-server.sh
# Output:
# 📍 SERVER IP: 192.168.1.100
```

**Máy B - Windows Laptop:**
```cmd
run-client-network.bat 192.168.1.100 8888
```

**Kết quả:**
- ✅ Client trên Máy B kết nối tới Server trên Máy A
- ✅ Có thể dùng app bình thường

---

**Có thêm câu hỏi? Hỏi tự do!** 🚀
