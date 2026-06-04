# 🔨 BIDNOVA - Hệ Thống Đấu Giá Trực Tuyến

## Mô Tả Dự Án

**BidNova** là một hệ thống đấu giá (auction system) hiện đại, xây dựng bằng Java với kiến trúc **Client-Server** và giao diện desktop. Hệ thống cho phép các người dùng:

- **Người bán (Seller):** Tạo phiên đấu giá cho sản phẩm của mình (Xe, Bất động sản, Đồ nghệ thuật, Tài sản công)
- **Người mua (Bidder):** Tham gia đặt giá với các tính năng:
    - Đặt giá thủ công
    - Đặt giá tự động (Auto Bid) với bước giá tùy chỉnh
    - Xem cập nhật giá trong thời gian thực (Real-time)
- **Quản trị viên (Admin):** Quản lý người dùng, phiên đấu giá và các giao dịch

### Tính Năng Chính

- **Hệ thống xác thực:** Đăng ký, đăng nhập, phân quyền (User/Seller/Admin)
- **Quản lý phiên đấu giá:** Tạo, xem, cập nhật trạng thái đấu giá
- **Đặt giá thủ công:** Validation kiểm tra bước giá tối thiểu
- **Đặt giá tự động (AutoBid):** Tự động tăng giá đến mức tối đa của người dùng
- **Giá trần (Price Ceiling):** Tự động kết thúc phiên khi đạt giới hạn giá
- **Bước giá tối thiểu (Min Bid Increment):** Đảm bảo tính hợp lý của các giá đặt
- **Cập nhật thời gian thực:** Observer pattern cho phép tất cả người dùng thấy cập nhật giá ngay lập tức
- **Lịch sử đấu giá (Bid History):** Theo dõi tất cả các giá được đặt
- **Phân loại sản phẩm:** Hỗ trợ nhiều loại sản phẩm (Vehicle, RealEstate, ArtCollectible, StateProperty)
- **Unit Tests:** Kiểm thử toàn diện các module chính

---

## Công Nghệ Sử Dụng

| Thành Phần             | Công Nghệ                    | Phiên Bản |
| ---------------------- | ---------------------------- | --------- |
| **Ngôn ngữ lập trình** | Java                         | 25        |
| **Build Tool**         | Maven                        | 4.0.0     |
| **Framework Client**   | JavaFX                       | 25        |
| **Framework Server**   | Core Java (Socket/Threading) | 25        |
| **Cơ sở dữ liệu**      | MySQL                        | 8.0+      |
| **Connection Pool**    | MySQL Connector              | 8.0.33    |
| **Password Hashing**   | BCrypt (jbcrypt)             | 0.4       |
| **JSON Serialization** | Gson                         | 2.10.1    |
| **Testing**            | JUnit 5                      | 5.10.1    |
| **Icon Library**       | FontAwesomeFX                | 4.7.0     |
| **Mocking**            | Mockito                      | 5.2.0     |

---

## Yêu Cầu Hệ Thống

### Yêu Cầu Bắt Buộc

- **Java JDK:** Version 25 trở lên ([Download Java 25](https://jdk.java.net/25))
- **Maven:** Version 3.8.0 trở lên ([Download Maven](https://maven.apache.org/download.cgi))
- **MySQL Server:** Version 8.0 trở lên ([Download MySQL](https://dev.mysql.com/downloads/mysql))

### Hệ Điều Hành Hỗ Trợ

- **macOS** (Intel & Apple Silicon)
- **Linux** (Ubuntu, CentOS, Fedora)
- **Windows** (Windows 10, Windows 11)

---

## Cấu Trúc Dự Án

```
HeThongDauGia/
├── server/                          # Backend Server
│   ├── src/main/java/com/bidnova/
│   │   ├── ClientHandler.java       # Xử lý kết nối client
│   │   ├── ServerMain.java          # Server entry point
│   │   ├── dao/                     # Data Access Objects
│   │   │   ├── AuctionDAO.java
│   │   │   ├── UserDAO.java
│   │   │   ├── BidHistoryDAO.java
│   │   │   └── AutoBidDAO.java
│   │   ├── database/                # Database connection
│   │   │   └── DatabaseConnection.java
│   │   ├── handlers/                # Request handlers
│   │   │   ├── PlaceBidHandler.java
│   │   │   ├── LoginHandler.java
│   │   │   └── ...
│   │   ├── models/                  # Data models
│   │   │   ├── User.java
│   │   │   ├── Auction.java
│   │   │   ├── AutoBid.java
│   │   │   ├── BidHistory.java
│   │   │   └── Item.java (abstract)
│   │   ├── patterns/                # Design patterns
│   │   │   ├── factory/             # Factory Pattern
│   │   │   ├── observer/            # Observer Pattern
│   │   │   └── singleton/           # Singleton Pattern
│   │   ├── services/                # Business logic
│   │   │   ├── AutoBidService.java
│   │   │   └── AuctionService.java
│   │   ├── network/                 # Network communication
│   │   │   ├── Request.java
│   │   │   └── Response.java
│   │   └── utils/                   # Utilities
│   │       ├── PasswordUtil.java
│   │       └── DateUtil.java
│   ├── src/test/                    # Unit tests
│   ├── target/                      # Compiled output
│   └── pom.xml                      # Maven config
│
├── client/                          # JavaFX Desktop Client
│   ├── src/main/java/com/bidnova/
│   │   ├── Main.java                # Client entry point
│   │   ├── config/                  # Configuration
│   │   ├── controllers/             # MVC Controllers
│   │   │   ├── LoginController.java
│   │   │   ├── AuctionListController.java
│   │   │   ├── AuctionDetailController.java
│   │   │   └── ...
│   │   ├── models/                  # Client-side models
│   │   ├── network/                 # Network client
│   │   │   └── NetworkClient.java
│   │   └── utils/                   # Utilities
│   ├── src/main/resources/
│   │   ├── views/                   # FXML files (UI)
│   │   │   ├── admin/
│   │   │   ├── auth/
│   │   │   ├── bidder/
│   │   │   ├── seller/
│   │   │   ├── common/
│   │   │   └── components/
│   │   ├── css/                     # Stylesheets
│   │   ├── images/                  # Assets
│   │   └── fonts/                   # Custom fonts
│   ├── target/                      # Compiled output
│   └── pom.xml                      # Maven config
│
├── db_setup.sql                     # Database schema
├── pom.xml                          # Parent Maven config
├── README.md                        # This file
├── IMPLEMENT_PLAN.md                # Implementation plan
├── IMPLEMENTATION_DONE.md           # Completed features
├── TESTING_GUIDE.md                 # Testing instructions
├── EVALUATION_REPORT.md             # Project evaluation
└── VISUAL_GUIDE_AND_FAQ.md         # Visual guide
```

---

## Hướng Dẫn Cài Đặt & Chạy

### 1️⃣ Chuẩn Bị Cơ Sở Dữ Liệu

#### Bước 1: Tạo database và bảng

```bash
# Trên Linux/macOS/Windows, mở MySQL command line:
mysql -u root -p

# Thực thi script
source /path/to/db_setup.sql
```

Hoặc sử dụng MySQL Workbench:

1. Mở MySQL Workbench
2. Kết nối tới MySQL server
3. Mở file `db_setup.sql`
4. Thực thi (Ctrl+Shift+Enter hoặc Cmd+Shift+Enter)

#### Bước 2: Cập nhật cấu hình kết nối (Nếu cần)

Chỉnh sửa [server/src/main/java/com/bidnova/database/DatabaseConnection.java](server/src/main/java/com/bidnova/database/DatabaseConnection.java):

```java
// Mặc định:
String HOST = "zephyr.proxy.rlwy.net";
int PORT = 32243;
String DATABASE = "auction_db";
String USER = "root";
String PASSWORD = "<mang tính bảo mật>";
```

### 2️⃣ Build Dự Án

Mở terminal tại thư mục gốc của dự án và chạy:

#### Build server

```bash
mvn -f server/pom.xml clean package
```

#### Build client

```bash
mvn -f client/pom.xml clean package
```

Hoặc build cả hai cùng lúc (tại thư mục root):

```bash
mvn clean package
```

**Kết quả:**

- Server JAR: `server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar`
- Client sẽ được biên dịch sẵn sàng chạy với Maven

### 3️⃣ Chạy Server

#### Cách 1: Sử dụng Maven (Khuyến nghị)

```bash
cd server
mvn exec:java -Dexec.mainClass="com.bidnova.ServerMain"
```

#### Cách 2: Sử dụng Java command (Sau khi build)

```bash
cd server/target
java -jar server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### Cách 3: Chạy từ IDE (IntelliJ/Eclipse/VS Code)

1. Mở project
2. Tìm file [ServerMain.java](server/src/main/java/com/bidnova/ServerMain.java)
3. Click chuột phải → Run 'ServerMain.main()'

**Output mong đợi:**

```
Server is running on port 5000...
Waiting for client connections...
```

**Lưu ý:**

- Server khởi động trên port `5000`
- Đảm bảo MySQL đã chạy trước khi khởi động server
- Để dừng server, nhấn `Ctrl+C` (hoặc `Cmd+C` trên macOS)

### 4️⃣ Chạy Client

#### Cách 1: Sử dụng Maven (Khuyến nghị)

```bash
cd client
mvn javafx:run
```

#### Cách 2: Sử dụng IDE

1. Mở project
2. Tìm file [Main.java](client/src/main/java/com/bidnova/Main.java)
3. Click chuột phải → Run 'Main'

**Lưu ý:**

- Client sẽ tự động kết nối tới server trên `localhost:5000`
- Nếu kết nối thất bại, kiểm tra:
    - Server đã chạy trên port 5000?
    - Network config trong [NetworkClient.java](client/src/main/java/com/bidnova/network/NetworkClient.java)

### 5️⃣ Sử Dụng Ứng Dụng

#### Tài Khoản Mặc Định

**Admin:**

```
Username: admin
Password: uet123
```

**Seller (Người bán):**

```
Username: doquangvinhseller
Password: doquangvinh
```

**Bidder (Người mua):**

```
Username: doquangvinh
Password: doquangvinh

Username: buivinhduy
Password: vididi
```

#### Các Bước Sử Dụng Cơ Bản

1. **Đăng nhập:** Nhập username & password
2. **Người bán:**
    - Tạo phiên đấu giá mới
    - Nhập thông tin sản phẩm, giá khởi điểm, thời gian
    - Có thể thiết lập giá trần (price ceiling) và bước giá tối thiểu
3. **Người mua:**
    - Xem danh sách phiên đấu giá
    - Chọn phiên để xem chi tiết
    - Đặt giá hoặc thiết lập AutoBid
    - Cập nhật thời gian thực hiển thị (Real-time updates)

---

## Hướng Dẫn Test

### Chạy Unit Tests

#### Tất cả tests

```bash
mvn test
```

#### Test từng module

```bash
# Test server
mvn -f server/pom.xml test

# Test client
mvn -f client/pom.xml test
```

#### Test một file cụ thể

```bash
mvn -f server/pom.xml test -Dtest=AuctionTest
mvn -f server/pom.xml test -Dtest=AutoBidServiceTest
mvn -f server/pom.xml test -Dtest=AuctionDAOTest
```

### Test Các Tính Năng Chính

Xem chi tiết hướng dẫn test manual trong [TESTING_GUIDE.md](documents/TESTING_GUIDE.md)

**Các scenario test chính:**

1. Min Bid Increment Validation
2. Price Ceiling (Instant Win)
3. AutoBid với Min Increment
4. AutoBid Reaches Ceiling

---

## Danh Sách Chức Năng Đã Hoàn Thành

### Quản Lý Người Dùng

- Đăng ký tài khoản
- Đăng nhập/Đăng xuất
- Xác thực mật khẩu (BCrypt hashing)
- Phân quyền người dùng (Bidder/Seller)
- Quản lý hồ sơ người dùng

### Quản Lý Phiên Đấu Giá

- Tạo phiên đấu giá
- Xem danh sách phiên đấu giá (Active, Finished, All)
- Cập nhật chi tiết phiên
- Tự động kết thúc phiên khi hết thời gian
- Hỗ trợ nhiều loại sản phẩm

### Quản Lý Đấu Giá

- Đặt giá thủ công (Place Bid)
- Validation bước giá tối thiểu (Min Bid Increment)
- Validation giá trần (Price Ceiling) - Tự động kết thúc khi đạt
- Lịch sử đấu giá (Bid History)
- Xem người dùng đang dẫn đầu (Highest Bidder)

### Đấu Giá Tự Động (AutoBid)

- Tạo AutoBid với bước giá tùy chỉnh
- Tự động tăng giá đến mức tối đa
- Điều chỉnh tự động nếu bước giá < min increment
- Dừng AutoBid khi đạt giá trần hoặc maxBid
- Quản lý (xem, hủy) AutoBid đã tạo

### Cập Nhật Thời Gian Thực

- Observer Pattern cho phép broadcast updates
- Tất cả client thấy cập nhật giá ngay lập tức
- Cập nhật trạng thái phiên (OPEN, FINISHED, CLOSED)
- Thông báo khi phiên kết thúc

### Kiến Trúc & Design Patterns

- **MVC Pattern:** Tách biệt Controller, View, Model (JavaFX)
- **DAO Pattern:** Tách biệt database access logic
- **Factory Pattern:** Tạo các loại Item khác nhau
- **Observer Pattern:** Broadcast bid updates
- **Singleton Pattern:** Database connection

### Kỹ Thuật Nâng Cao

- **Multi-threading:** Server xử lý multiple clients
- **Socket Programming:** Client-Server communication
- **Concurrency Control:** Thread-safe operations
- **Connection Pooling:** Efficient database access
- **JSON Serialization:** Data exchange (Gson)

### Build & Testing

- **Maven Build Tool:** POM.xml configuration
- **Unit Tests:** JUnit 5 với 53 test cases
- **Test Coverage:**
    - User models & authentication
    - Auction models & business logic
    - AutoBid service
    - Bid history tracking
    - Factory pattern item creation
    - Request/Response network communication

### Giao Diện & UX

- JavaFX UI với FXML layouts
- CSS styling (base-style.css)
- Font Awesome icons
- Responsive design
- Separate views cho Admin/Seller/Bidder

### Tính Năng Nâng Cao (Bonus)

- **Price Ceiling (Giá Trần):** Tự động kết thúc phiên khi đạt
- **Min Bid Increment (Bước Giá Tối Thiểu):** Đảm bảo tính hợp lý
- **AutoBid Smart Adjustment:** Tự động điều chỉnh bước giá
- **Multiple Item Categories:** Factory pattern for item creation
- **Advanced Bid Validation:** Multi-layer validation (Client + Server)

---

## Thống Kê Dự Án

| Metrics                 | Giá Trị                      |
| ----------------------- | ---------------------------- |
| **Total Files**         | ~50+ Java files              |
| **Lines of Code**       | ~10,000+ LOC                 |
| **Test Cases**          | 53+ Unit tests               |
| **Design Patterns**     | 4+ patterns                  |
| **Database Tables**     | 5 tables                     |
| **Supported Platforms** | 3 OS (Windows, macOS, Linux) |

---

## Troubleshooting

### Lỗi: "Connection refused" khi server khởi động

**Nguyên nhân:** MySQL không chạy hoặc cấu hình sai

**Giải pháp:**

```bash
# Kiểm tra MySQL đang chạy
# macOS:
brew services list | grep mysql

# Linux:
sudo systemctl status mysql

# Windows (Command Prompt):
sc query MySQL80

# Nếu chưa chạy, khởi động:
# macOS:
brew services start mysql

# Linux:
sudo systemctl start mysql
```

### Lỗi: "Port 5000 already in use"

**Giải pháp:** Thay đổi port trong [ServerMain.java](server/src/main/java/com/bidnova/ServerMain.java):

```java
final int PORT = 8888;
```

### Lỗi: "Cannot find main class com.bidnova.Main"

**Giải pháp:** Rebuild project

```bash
mvn clean package
```

### Lỗi: JavaFX module not found

**Giải pháp:** Cài đặt JavaFX SDK từ [openjfx.io](https://gluonhq.com/products/javafx/)

---

## Tác Giả

- **Tên:** Nhóm 1
- **Trường:** UET - VNU
- **Lớp:** Lập Trình Hướng Đối Tượng (LTNC)
- **Thời gian:** 06/2026

---

## Hỗ Trợ & Liên Hệ

Nếu gặp vấn đề, vui lòng:

1. Kiểm tra [TESTING_GUIDE.md](documents/TESTING_GUIDE.md)
2. Xem lại [VISUAL_GUIDE_AND_FAQ.md](documents/VISUAL_GUIDE_AND_FAQ.md)
3. Kiểm tra log server tại terminal
4. Kiểm tra database bằng MySQL Workbench

---

## Bài báo cáo và đường link video demo

Link báo cáo PDF: [Report_BidNova.pdf](REPORT_BIDNOVA.pdf)

Video demo:[]()

**Happy Bidding!**
