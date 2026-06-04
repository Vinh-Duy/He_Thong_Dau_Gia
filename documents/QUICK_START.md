# QUICK START - Chạy BidNova Local

## TL;DR

**macOS/Linux:**

```bash
bash /Users/vinhduy/Desktop/HeThongDauGia/run-dev.sh
```

**Windows CMD:**

```cmd
run-dev.bat
```

**Windows PowerShell:**

```powershell
.\run-dev.ps1
```

---

## Kết Quả

- Server khởi động trên port 8888
- Client khởi động và kết nối tới server
- Ứng dụng chạy bình thường

---

## 🔍 Giải Thích

### Vấn Đề:

- Client không kết nối được Render backend (raw TCP socket blocked)
- Render dùng HTTPS proxy - chỉ cho phép HTTP/HTTPS traffic

### Giải Pháp Tạm Thời:

- Chạy Server + Client local (cùng máy)
- Client connects to `localhost:8888` (no proxy)

### Giải Pháp Dài Hạn:

- Convert Socket → WebSocket
- Deploy client trên Render (hoặc web app riêng)
- Estimated: 2-3 hours

---

## 📂 Scripts Có Sẵn

| File             | Dùng Cho                            | OS                   |
| ---------------- | ----------------------------------- | -------------------- |
| `run-dev.sh`     | Chạy Server + Client                | macOS/Linux          |
| `run-dev.bat`    | Chạy Server + Client                | Windows              |
| `run-dev.ps1`    | Chạy Server + Client                | Windows (PowerShell) |
| `run-client.sh`  | Chỉ chạy Client (kết nối localhost) | macOS/Linux          |
| `run-client.bat` | Chỉ chạy Client (kết nối localhost) | Windows              |

---

**Thử ngay!**
