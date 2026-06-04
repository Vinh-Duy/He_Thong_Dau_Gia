# DEPLOY SETUP - WHAT'S BEEN DONE

**Date:** 1 Tháng 6, 2026  
**Status:** ALL READY FOR RENDER DEPLOYMENT

---

## SUMMARY

Tôi đã chuẩn bị project của bạn để deploy lên Render một cách **đơn giản nhất**. Vì database đã trong `.env`, chỉ cần fix 3 thứ:

---

## CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1. ServerMain.java - Đọc PORT từ Environment

**File:** `server/src/main/java/com/bidnova/ServerMain.java`

**Thay đổi:**

```java
// TRƯỚC (hardcoded):
private static final int PORT = 8888;

// SAU (dynamic):
private static int getPort() {
    String portEnv = System.getenv("PORT");
    if (portEnv != null && !portEnv.isEmpty()) {
        try {
            return Integer.parseInt(portEnv);
        } catch (NumberFormatException e) {
            System.err.println(" Invalid PORT env variable: " + portEnv);
        }
    }
    return 8888;
}

private static final int PORT = getPort();
```

**Render sẽ set `PORT=10000` automatically**

---

### 2. server/pom.xml - Maven Shade Plugin

**File:** `server/pom.xml`

**Thêm vào `<build><plugins>`:**

```xml
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

**Tạo `bidnova-server.jar` với tất cả dependencies**

---

### 3. Dockerfile - Multi-stage Docker Build

**File:** `Dockerfile` (mới tạo)

```dockerfile
FROM maven:3.9-eclipse-temurin-25 as builder

WORKDIR /app
COPY pom.xml .
COPY server/ server/
COPY client/ client/

RUN mvn clean package -DskipTests -pl server

# ============ Runtime Stage ============
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app
COPY --from=builder /app/server/target/server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

**Lightweight image - chỉ cần JRE runtime**

---

### 4. docker-compose.yml - Local Testing

**File:** `docker-compose.yml` (mới tạo)

- Tạo MySQL container + Server container
- Dễ test local trước deploy

---

### 5. Documentation

**Files tạo:**

- `RENDER_DEPLOY_SIMPLE.md` - Chi tiết deployment guide
- `QUICK_DEPLOY_SETUP.md` - Quick start guide
- `deploy.sh` - Build script tự động

---

## NEXT STEPS (Bạn làm)

### Step 1: Build & Test Local

```bash
# 1. Build JAR
mvn -f server/pom.xml clean package -DskipTests

# 2. Verify
ls -lh server/target/bidnova-server.jar

# 3. Should see something like:
# -rw-r--r--  45M  bidnova-server.jar
```

### Step 2: Test with Docker (Optional)

```bash
# Start MySQL + Server
docker-compose up -d

# Check logs
docker-compose logs -f bidnova-server

# Should see:
# Server starting on port: 8080
# Database connected successfully

# Stop
docker-compose down
```

### Step 3: Push to GitHub

```bash
git add .
git commit -m "Deploy ready: PORT from env, maven-shade, Dockerfile"
git push origin main
```

### Step 4: Deploy on Render

1. **Go to:** https://dashboard.render.com
2. **Click:** "New +" → "Web Service"
3. **Select:** Your GitHub repo
4. **Build:**
    - Runtime: `Docker`
    - Build Command: (leave empty)
    - Start Command: (leave empty)

5. **Environment Variables** (in Advanced):

    ```
    PORT = 10000
    DB_URL = jdbc:mysql://zephyr.proxy.rlwy.net:32243/auction_db
    DB_USER = root
    DB_PASSWORD = vGhcIvAXFhNwzGTOOWZTIGmJvyShpoST
    JWT_SECRET = E4aKywl5o8u6LekAb9jKIr0Nqduhet6+n4QBDFzSLJU=
    ```

6. **Click:** "Create Web Service"

**Deploy tự động trong 3-5 phút!**

---

## 📊 WHAT CHANGED

| Component     | Before         | After              | Impact         |
| ------------- | -------------- | ------------------ | -------------- |
| **PORT**      | Hardcoded 8888 | Read from env      | Cloud-ready    |
| **Build**     | No JAR plugin  | maven-shade-plugin | Standalone JAR |
| **Docker**    | No Dockerfile  | Multi-stage build  | Easy deploy    |
| **Local Dev** | Manual setup   | docker-compose     | One command    |

---

## DEPLOY CHECKLIST

- [ ] Run: `mvn -f server/pom.xml clean package -DskipTests`
- [ ] Verify: `ls server/target/bidnova-server.jar`
- [ ] (Optional) Test: `docker-compose up -d`
- [ ] Commit: `git add . && git commit -m "..."`
- [ ] Push: `git push origin main`
- [ ] Render: New Web Service
- [ ] Set env variables
- [ ] Click Deploy
- [ ] Check logs: `docker-compose logs` or Render dashboard
- [ ] Test: `curl https://bidnova-server.onrender.com/health`

---

## 🎁 FILES CREATED/MODIFIED

```
 server/src/main/java/com/bidnova/ServerMain.java (MODIFIED)
 server/pom.xml (MODIFIED)
 Dockerfile (CREATED)
 docker-compose.yml (CREATED)
 RENDER_DEPLOY_SIMPLE.md (CREATED)
 QUICK_DEPLOY_SETUP.md (CREATED)
 deploy.sh (CREATED)
 THIS_FILE (CREATED)
```

---

## 💡 QUICK REFERENCE

**Build command:**

```bash
mvn -f server/pom.xml clean package -DskipTests
```

**Test locally:**

```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

**Deploy:**

1. Push to GitHub
2. Create Web Service on Render
3. Set env vars
4. Done!

---

## ❓ COMMON QUESTIONS

**Q: Do I need to set database separately?**  
A: No! Database config is in `.env` which you already have. Render will use those env vars.

**Q: What if build fails?**  
A: Check `server/pom.xml` has maven-shade-plugin. Run `mvn clean package` locally first.

**Q: Can I rollback?**  
A: Yes! On Render dashboard, click service → Deployments → select previous version.

**Q: How do I monitor?**  
A: Render dashboard → Service → Logs. Or use `docker-compose logs`.

---

## YOU'RE READY!

Tất cả chuẩn bị xong. Chỉ cần:

1. Build locally (5 min)
2. Push to GitHub (1 min)
3. Deploy on Render (3-5 min)

**Total time: ~15 minutes** ⏱️

Happy deploying!
