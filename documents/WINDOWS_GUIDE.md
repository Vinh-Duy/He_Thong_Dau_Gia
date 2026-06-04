# 🪟 Hướng Dẫn Chạy trên Windows

**Ngày:** 2 Tháng 6, 2026

---

## Cách 1: Dùng Batch Script (Đơn Giản Nhất)

### **Bước 1: Kiểm Tra Java Đã Cài Chưa**

Mở **Command Prompt** (hoặc PowerShell):

```cmd
java -version
```

Nếu thấy version → OK  
Nếu lỗi → [Cài Java](https://www.oracle.com/java/technologies/downloads/#java25)

### **Bước 2: Kiểm Tra Maven Đã Cài Chưa**

```cmd
mvn -v
```

Nếu thấy version → OK  
Nếu lỗi → [Cài Maven](https://maven.apache.org/download.cgi)

### **Bước 3: Chạy Script**

1. **Mở File Explorer** → Navigate tới: `C:\Users\YourUser\Desktop\HeThongDauGia`
2. **Double-click** file `run-client.bat`
3. **Chờ build** (lần đầu sẽ lâu ~2-3 phút)
4. **Xem app khởi động**

---

## Cách 2: Chạy Manual (Từ Command Prompt)

### **Bước 1: Mở Command Prompt**

- `Win + R` → gõ `cmd` → Enter
- Hoặc tìm "Command Prompt" trong Start Menu

### **Bước 2: Navigate tới Project**

```cmd
cd /d C:\Users\[YourUsername]\Desktop\HeThongDauGia
```

**Ví dụ:**

```cmd
cd /d C:\Users\vinhduy\Desktop\HeThongDauGia
```

### **Bước 3: Build Client**

```cmd
mvn clean package -f client/pom.xml -DskipTests
```

**Đợi cho đến khi thấy:**

```
BUILD SUCCESS
```

### **Bước 4: Chạy Client**

```cmd
set AUCTION_SERVER_HOST=bidnova-server.onrender.com
set AUCTION_SERVER_PORT=8888
java -jar client/target/BidNova-Client.jar
```

**Nếu thấy:**

```
 Đã kết nối tới Server thành công!
```

→ **Thành công!** 🎉

---

## Cách 3: Chạy bằng PowerShell

```powershell
# 1. Navigate
cd C:\Users\[YourUsername]\Desktop\HeThongDauGia

# 2. Build
mvn clean package -f client/pom.xml -DskipTests

# 3. Run
$env:AUCTION_SERVER_HOST = "bidnova-server.onrender.com"
$env:AUCTION_SERVER_PORT = "8888"
java -jar client/target/BidNova-Client.jar
```

---

## 🔧 Troubleshooting Windows

### **"mvn is not recognized"**

**Giải pháp:**

- Maven chưa cài hoặc chưa add vào PATH
- [Download Maven](https://maven.apache.org/download.cgi)
- Extract → Add thư mục `bin` vào Windows PATH

**Kiểm tra:**

```cmd
mvn -v
```

### **"java is not recognized"**

**Giải pháp:**

- Java chưa cài hoặc chưa add vào PATH
- [Download Java JDK 25](https://www.oracle.com/java/technologies/downloads/)
- Cài đặt → Thêm vào System PATH

**Kiểm tra:**

```cmd
java -version
```

### **"no main manifest attribute"**

**Giải pháp:** Đã sửa rồi! Chạy lại:

```cmd
mvn clean package -f client/pom.xml -DskipTests
```

### **Build fail - "Cannot find dependency"**

**Giải pháp:**

```cmd
mvn clean install -DskipTests
mvn clean package -f client/pom.xml -DskipTests
```

### **Không kết nối được tới server**

**Kiểm tra:**

1. Backend có chạy trên Render không?
    ```cmd
    ping bidnova-server.onrender.com
    ```
2. Nếu ping lỗi → Server chưa khởi động trên Render
3. Nếu ping OK → Xem logs client:
    ```
    Không thể kết nối tới Server.
    ```
    → Vấn đề với socket connection (xem hướng dẫn chính)

---

## 📝 Summary Windows

| Thao tác         | Lệnh                                                                                                   |
| ---------------- | ------------------------------------------------------------------------------------------------------ |
| Build            | `mvn clean package -f client/pom.xml -DskipTests`                                                      |
| Run (CMD)        | `set AUCTION_SERVER_HOST=bidnova-server.onrender.com && java -jar client/target/BidNova-Client.jar`    |
| Run (PowerShell) | `$env:AUCTION_SERVER_HOST="bidnova-server.onrender.com" && java -jar client/target/BidNova-Client.jar` |
| Run (Quick)      | Double-click `run-client.bat`                                                                          |

---

**Nếu vẫn gặp vấn đề, báo lỗi cho mình!**
