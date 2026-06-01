# 📦 DEPLOYMENT DATA SHEETS - BIDNOVA

## 1️⃣ DATABASE CONNECTION INFO

### Railway MySQL (Recommended)

```
Host:     zephyr.proxy.rlwy.net
Port:     32243
Database: railway
User:     root
Password: [Check Railway Dashboard]

JDBC URL: jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway?useSSL=false&allowMultiQueries=true&autoReconnect=true
```

### Test Connection

```bash
mysql -h zephyr.proxy.rlwy.net -P 32243 -u root -p
```

---

## 2️⃣ RENDER CONFIGURATION

### Server Service

```yaml
Service Name: bidnova-server
Service Type: Web Service
Runtime: Java
Build Command: mvn clean package -DskipTests
Start Command: java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
Instance Type: Starter (Free) / Standard
Region: Singapore
```

### Environment Variables (Set in Render Dashboard)

```
DB_URL        = jdbc:mysql://zephyr.proxy.rlwy.net:32243/railway?useSSL=false&allowMultiQueries=true
DB_USER       = root
DB_PASSWORD   = [Railway Password]
JAVA_OPTS     = -Xmx512m -Xms256m
```

### Render Service URL

```
Production URL: https://bidnova-server.onrender.com
Internal Port:  10000 (assigned by Render)
Public Port:    443 (HTTPS)
```

---

## 3️⃣ MAVEN BUILD CONFIG

### pom.xml Additions Required

```xml
<!-- 1. Dependency Plugin (download all dependencies) -->
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

<!-- 2. OR use Maven Assembly Plugin (create fat jar) -->
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
                <finalName>bidnova-server</finalName>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
        </execution>
    </executions>
</plugin>

<!-- 3. OR use Maven Shade Plugin (recommended for large projects) -->
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
```

---

## 4️⃣ CLIENT CONFIGURATION

### Connect to Render Server

**File:** `client/src/main/java/com/bidnova/config/NetworkConfig.java`

```java
public class NetworkConfig {
    // Detect environment
    private static boolean isProduction() {
        String env = System.getenv("DEPLOYMENT_ENV");
        return "production".equals(env);
    }

    public static String getHost() {
        if (isProduction()) {
            return "bidnova-server.onrender.com";  // Render URL
        }
        return "127.0.0.1";  // Local
    }

    public static int getPort() {
        if (isProduction()) {
            return 443;  // HTTPS
        }
        return 8888;  // Local socket
    }
}
```

### Run Client Against Render Server

```bash
# Option 1: System Properties
mvn clean javafx:run \
  -Dauction.server.host=bidnova-server.onrender.com \
  -Dauction.server.port=443 \
  -Dauction.server.timeout=15

# Option 2: Environment Variable
export DEPLOYMENT_ENV=production
mvn clean javafx:run

# Option 3: Manual config in code (hardcode for testing)
```

---

## 5️⃣ GITHUB PUSH CHECKLIST

Before pushing to GitHub for deployment:

```bash
# 1. Create .gitignore entries
cat >> .gitignore << 'EOF'
.env
.env.local
target/
*.class
*.jar
.idea/
*.iml
EOF

# 2. Ensure .env is NOT committed
git rm --cached .env
git status

# 3. Add deployment files
git add Procfile render.yaml .env.example

# 4. Commit
git commit -m "Add deployment configuration for Render"

# 5. Push
git push origin main
```

---

## 6️⃣ SQL DATABASE SCHEMA

### Full Setup Script

```sql
-- Create Database
CREATE DATABASE IF NOT EXISTS railway CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE railway;

-- Users Table
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

-- Auctions Table
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
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_end_time (end_time),
    INDEX idx_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bid History Table
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

-- Auto Bids Table
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
    INDEX idx_user (user_id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create test user
INSERT INTO users (username, password, email, full_name, role)
VALUES
  ('admin', 'hashed_password_here', 'admin@bidnova.com', 'Administrator', 'ADMIN'),
  ('seller1', 'hashed_password_here', 'seller1@bidnova.com', 'John Seller', 'SELLER'),
  ('bidder1', 'hashed_password_here', 'bidder1@bidnova.com', 'Jane Bidder', 'BIDDER');
```

---

## 7️⃣ BUILD COMMANDS

### Local Development

```bash
# Build server
mvn -f server/pom.xml clean package -DskipTests

# Run server (with local MySQL)
java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain

# Build client
mvn -f client/pom.xml clean package

# Run client
mvn -f client/pom.xml clean javafx:run
```

### Render Deployment

```bash
# Build (Render will run this)
mvn clean package -DskipTests

# Start (Render will run this)
java -cp "server/target/classes:server/target/dependency/*" com.bidnova.ServerMain
```

### Troubleshooting Build

```bash
# Check Maven version
mvn --version

# Force update dependencies
mvn clean dependency:resolve

# Skip tests (faster)
mvn clean package -DskipTests

# Full verbose output
mvn -X clean package
```

---

## 8️⃣ MONITORING & LOGS

### View Server Logs on Render

```
1. Vào Render Dashboard: https://dashboard.render.com
2. Select Service: bidnova-server
3. Xem Logs tab
4. Real-time monitoring
```

### Common Log Issues

```
[ERROR] Connection refused → Database not running
[ERROR] Port 8888 in use → Change port in ServerMain
[ERROR] ClassNotFoundException → Dependency not packaged
[ERROR] OutOfMemoryError → Increase JAVA_OPTS -Xmx
```

### Local Debug

```bash
# Enable verbose logging
mvn clean package -X

# Run with debug output
java -Xms256m -Xmx512m -Ddebug=true \
  -cp "server/target/classes:server/target/dependency/*" \
  com.bidnova.ServerMain
```

---

## 9️⃣ SECURITY BEST PRACTICES

### .gitignore (Never commit sensitive data)

```
# Environment
.env
.env.local
.env.*.local

# Build artifacts
target/
build/
dist/

# IDE
.idea/
.vscode/
*.iml
*.sublime-workspace

# Credentials
credentials.json
secrets.txt
```

### Database Security

- Use strong passwords (min 16 chars)
- Enable SSL for remote connections
- Restrict user permissions
- Regular backups
- Use environment variables (never hardcode credentials)

### Network Security

- Use HTTPS/TLS for client-server communication
- Validate all inputs on server
- Rate limiting on API endpoints
- SQL injection prevention (use PreparedStatement)
- CORS configuration if needed

---

## 🔟 QUICK REFERENCE

### Deployment Steps Summary

```
1. Setup Railway MySQL
   └─ Create DB, Import schema, Get credentials

2. Prepare Code
   └─ Add Procfile, pom.xml config, .env.example

3. Push to GitHub
   └─ Git add, commit, push main branch

4. Deploy on Render
   └─ Create Web Service, connect GitHub, set env vars

5. Test
   └─ Verify server running, client can connect

6. Monitor
   └─ Check logs, verify database, test features
```

### Key Files

```
Procfile              - Render build/start commands
render.yaml           - Render service configuration
.env.example          - Environment template
db_setup.sql          - Database schema
DEPLOYMENT_GUIDE_RENDER.md  - Full deployment guide
```

### Test URLs

```
Server (Local):       127.0.0.1:8888
Server (Render):      https://bidnova-server.onrender.com
Database (Railway):   zephyr.proxy.rlwy.net:32243
```

---

**Deployment Data Generated:** 1 June 2026  
**Status:** ✅ Ready for Cloud Deployment
