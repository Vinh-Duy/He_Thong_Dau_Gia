# DEPLOYMENT GUIDE - BIDNOVA TRÊN RENDER

**Ngày:** 1 Tháng 6, 2026  
**Project:** BidNova - Hệ Thống Đấu Giá Trực Tuyến  
**Platform:** Render.com

---

## 📋 MỤC LỤC

1. [Yêu Cầu Tiên Quyết](#yêu-cầu-tiên-quyết)
2. [Chuẩn Bị Database](#chuẩn-bị-database)
3. [Deploy Server](#deploy-server)
4. [Deploy Client](#deploy-client)
5. [Cấu Hình Environment](#cấu-hình-environment)
6. [Troubleshooting](#troubleshooting)

---

## 🔧 YÊU CẦU TIÊN QUYẾT

### Accounts Cần Có

- Tài khoản Render.com
- Tài khoản GitHub (để push code)
- MySQL Database (Railway.app hoặc Render database)

### Tools Cần Cài

- Git
- Java JDK 25
- Maven 3.8.0+
- MySQL Client (optional, để test connection)

---

## 🗄️ CHUẨN BỊ DATABASE

### Option 1: Sử Dụng Railway.app (Recommended)

#### Bước 1: Tạo MySQL Database trên Railway

```
1. Vào https://railway.app
2. Đăng nhập/Tạo tài khoản
3. Tạo Project mới
4. Thêm MySQL
5. Lấy thông tin connection:
   - HOST: zephyr.proxy.rlwy.net (hoặc tương tự)
   - PORT: 32243
   - DATABASE: railway
   - USER: root
   - PASSWORD: xxxxx
```

#### Bước 2: Import SQL Schema

```bash
# Kết nối từ terminal
mysql -h zephyr.proxy.rlwy.net -P 32243 -u root -p

# Thực thi lệnh SQL
USE railway;
SOURCE db_setup.sql;
```

Hoặc dùng MySQL Workbench:

1. Kết nối tới Railway MySQL
2. Mở file `db_setup.sql`
3. Execute (Ctrl+Shift+Enter)

### Option 2: Sử Dụng Render PostgreSQL

_(Cần modify code để support PostgreSQL, recommended cho sau)_

---

## 🖥️ DEPLOY SERVER TRÊN RENDER

### Bước 1: Chuẩn Bị Repo trên GitHub

```bash
# Clone/Navigate tới project
cd /path/to/Tai_Xiu

# Tạo .gitignore nếu chưa có
cat > .gitignore << 'EOF'
target/
.env
.env.local
.idea/
*.iml
*.class
*.jar
EOF

# Commit code
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

### Bước 2: Tạo File Konfigurasi

#### File: `.env` (Local - không push)

```env
# Database Configuration
DB_URL=jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway
DB_USER=root
DB_PASSWORD=your_password_here

# Server Configuration
SERVER_PORT=8888
JAVA_OPTS=-Xmx512m
```

#### File: `Procfile` (Root directory)

```
web: mvn clean package -q && java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
```

#### File: `render.yaml` (Root directory)

```yaml
services:
    - type: web
      name: bidnova-server
      env: java
      plan: standard
      buildCommand: mvn clean package -DskipTests
      startCommand: java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
      envVars:
          - key: DB_URL
            fromDatabase:
                name: bidnova-db
                property: connection_string
          - key: PORT
            value: 10000

databases:
    - name: bidnova-db
      engine: mysql
      version: 8
```

### Bước 3: Cấu Hình pom.xml cho Build

Thêm vào `server/pom.xml`:

```xml
<build>
    <plugins>
        <!-- Maven Assembly Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.4.2</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                    <configuration>
                        <archive>
                            <manifest>
                                <mainClass>com.bidnova.ServerMain</mainClass>
                            </manifest>
                        </archive>
                        <descriptorRefs>
                            <descriptorRef>jar-with-dependencies</descriptorRef>
                        </descriptorRefs>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Maven Shade Plugin (Alternative) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.bidnova.ServerMain</mainClass>
                            </transformer>
                        </transformers>
                        <finalName>bidnova-server</finalName>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Download Dependencies -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.3.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>copy-dependencies</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.build.directory}/dependency</outputDirectory>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Bước 4: Push tới Render

```bash
# Option A: Connect Render to GitHub
1. Vào https://dashboard.render.com/
2. Click "New +" → "Web Service"
3. Connect GitHub repository
4. Config:
   - Name: bidnova-server
   - Environment: Java
   - Region: Singapore (hoặc gần nhất)
   - Build Command: mvn clean package -DskipTests
   - Start Command: java -jar server/target/server-jar-with-dependencies.jar
   - Plan: Starter (Free) hoặc Standard
5. Add Environment Variables (xem phía dưới)
6. Deploy

# Option B: Push manually
git push render main
```

### Bước 5: Set Environment Variables trên Render

```
Vào Project Settings → Environment:

DB_URL=jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway
DB_USER=root
DB_PASSWORD=your_password
PORT=10000  (Render sẽ assign port này)
```

---

## 🎨 DEPLOY CLIENT TRÊN RENDER

### Lưu Ý Quan Trọng

- JavaFX Client cần Display/GUI → Không thể deploy trên Render server
- **Solution:** Deploy Web version hoặc Hybrid (Electron/Tauri)
- **Tạm thời:** Giữ client chạy local, server chạy trên Render

### Option 1: Client Web (Recommended cho Render)

Migrate từ JavaFX → React/Vue:

1. Tạo Frontend với React
2. Deploy trên Vercel/Netlify
3. Kết nối tới server Render

### Option 2: Electron App (Desktop)

Wrap JavaFX/Web app:

1. Build React app
2. Wrap với Electron
3. Distribute installer

### Option 3: Keep Client Local

```
Architecture:
┌─ Client (Local Desktop)
│  └─ JavaFX UI
│     └─ Kết nối tới Render Server
│
└─ Server (Render Cloud)
   ├─ Port: 10000 (from Render)
   └─ Database: Railway MySQL
```

#### Cấu hình Client để kết nối Server Render

Sửa `client/src/main/java/com/bidnova/config/NetworkConfig.java`:

```java
public class NetworkConfig {
    // Local development
    private static final String DEFAULT_HOST_LOCAL = "127.0.0.1";

    // Render production
    private static final String DEFAULT_HOST_RENDER = "your-bidnova-server.onrender.com";

    private static final int DEFAULT_PORT = 8888;  // Keep same, Render will map

    public static String getHost() {
        // Detect environment
        boolean isProduction = System.getenv("PRODUCTION") != null;
        return isProduction ? DEFAULT_HOST_RENDER : DEFAULT_HOST_LOCAL;
    }
}
```

Hoặc sử dụng System Property:

```bash
# Local
mvn clean javafx:run

# Production (connect to Render)
mvn clean javafx:run -Dauction.server.host=your-bidnova-server.onrender.com
```

---

## 🔐 CẤU HÌNH ENVIRONMENT

### Server Environment Variables (.env)

```env
# ==========================================
# DATABASE CONFIGURATION
# ==========================================
DB_URL=jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway?useUnicode=true&characterEncoding=utf8mb4&allowMultiQueries=true&autoReconnect=true
DB_USER=root
DB_PASSWORD=your_railway_password

# ==========================================
# SERVER CONFIGURATION
# ==========================================
SERVER_PORT=8888  # Render will map this to their assigned port
JAVA_OPTS=-Xmx512m -Xms256m

# ==========================================
# LOGGING
# ==========================================
LOG_LEVEL=INFO
```

### Client Configuration

Cách 1: System Properties

```bash
java -Dauction.server.host=your-bidnova-server.onrender.com \
     -Dauction.server.port=443 \
     -m javafx.controls \
     com.bidnova.Main
```

Cách 2: Properties File

```properties
# client/config.properties
auction.server.host=your-bidnova-server.onrender.com
auction.server.port=443
auction.server.timeout=10
```

Cách 3: Environment Variable

```bash
export AUCTION_SERVER_HOST=your-bidnova-server.onrender.com
export AUCTION_SERVER_PORT=443
mvn clean javafx:run
```

---

## 📊 DATABASE SCHEMA

### Tables Cần Tạo

```sql
-- 1. Users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    phone VARCHAR(20),
    gender VARCHAR(10),
    role VARCHAR(50) DEFAULT 'BIDDER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Auctions
CREATE TABLE auctions (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    image_url VARCHAR(500),
    start_price DECIMAL(20,2) NOT NULL,
    current_highest_bid DECIMAL(20,2) DEFAULT 0,
    highest_bidder VARCHAR(100),
    seller_id INT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    status VARCHAR(50) DEFAULT 'OPEN',
    price_ceiling DECIMAL(20,2),
    min_bid_increment DECIMAL(20,2) DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Bid History
CREATE TABLE bid_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    username VARCHAR(100),
    bid_amount DECIMAL(20,2) NOT NULL,
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_auction (auction_id),
    INDEX idx_user (user_id),
    INDEX idx_bid_time (bid_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Auto Bids
CREATE TABLE auto_bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    username VARCHAR(100),
    max_bid DECIMAL(20,2) NOT NULL,
    increment DECIMAL(20,2) DEFAULT 1000,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_auction (auction_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## DEPLOYMENT CHECKLIST

### Pre-Deployment

- [ ] Code committed to GitHub
- [ ] .env file created locally (NOT committed)
- [ ] Database imported on Railway
- [ ] Maven builds successfully locally
- [ ] Tests pass
- [ ] Procfile created
- [ ] pom.xml has dependency plugin

### On Render

- [ ] Create Web Service
- [ ] Connect GitHub repo
- [ ] Set Environment Variables
- [ ] Configure Build Command
- [ ] Configure Start Command
- [ ] Set region to Singapore/closest
- [ ] Enable Auto-Deploy from main branch

### Post-Deployment

- [ ] Server is running (check logs)
- [ ] Database connection is working
- [ ] Client can connect to server URL
- [ ] Test login functionality
- [ ] Test bid placement
- [ ] Monitor logs for errors

---

## 📡 NETWORK CONFIGURATION

### Server Running on Render

```
Client (Local)
    ↓
Render Server (bidnova-server.onrender.com:10000)
    ↓
Railway MySQL (zephyr.proxy.rlwy.net:32243)
```

### Connection String

```java
// Server connects to Railway MySQL
DB_URL=jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway?useSSL=false&allowMultiQueries=true

// Client connects to Render Server
SERVER_URL=https://bidnova-server.onrender.com
SERVER_PORT=443 (HTTPS) hoặc 80 (HTTP)
```

---

## 🔍 TROUBLESHOOTING

### Error 1: "Cannot connect to database"

```
Nguyên nhân: DB_URL/USER/PASSWORD sai
Giải pháp:
1. Check Environment Variables trên Render
2. Test connection locally với same credentials
3. Verify Railway database is running
```

### Error 2: "Port already in use"

```
Nguyên nhân: Port 8888 bị occupied
Giải pháp:
1. Render assigns port tự động
2. Không cần set PORT=8888 trên Render
3. Client kết nối via Render URL
```

### Error 3: "Connection timeout"

```
Nguyên nhân: Network issue, server not ready
Giải pháp:
1. Reload server page trên Render dashboard
2. Check logs: Render Dashboard → Logs
3. Verify database connection in logs
```

### Error 4: "MySQL driver not found"

```
Nguyên nhân: maven-dependency-plugin không download driver
Giải pháp:
1. Add mysql-connector-java trong dependencies
2. Use maven-shade-plugin hoặc maven-assembly-plugin
3. Rebuild: mvn clean package -DskipTests
```

---

## 📝 BUILD & DEPLOY COMMANDS

### Local Testing

```bash
# Build server
cd server
mvn clean package -DskipTests

# Run server (requires local MySQL running)
java -cp "target/classes:target/dependency/*" com.bidnova.ServerMain

# Build client
cd ../client
mvn clean package

# Run client
mvn clean javafx:run
```

### Render Deployment

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar server/target/server-jar-with-dependencies.jar
# OR
java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
```

---

## QUICK START SUMMARY

### Step 1: Setup Railway MySQL

1. Create Railway account
2. Add MySQL database
3. Import db_setup.sql
4. Note connection details

### Step 2: Prepare Code

1. Add Procfile to root
2. Add pom.xml build config
3. Create .env file (local only)
4. Push to GitHub

### Step 3: Deploy on Render

1. Create Web Service on Render
2. Connect GitHub repo
3. Add Environment Variables
4. Deploy

### Step 4: Configure Client

1. Update NetworkConfig.java with Render server URL
2. Build client locally
3. Run client pointing to Render server

### Step 5: Test

1. Login with test account
2. Create auction
3. Place bid
4. Check real-time updates

---

**Deployment Status:** Ready for Cloud Deployment

_Last Updated: 1 June 2026_
