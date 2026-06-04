# Render Deployment - QUICK START

**Ngày:** 1 Tháng 6, 2026

---

## ⚡ CÁCH DEPLOY ĐỦN GIẢN NHẤT

### BƯỚC 1: Fix Port trong Code

Chỉnh sửa `ServerMain.java` để dùng PORT từ environment variable:

```java
// Thay dòng này:
private static final int PORT = 8888;

// Thành:
private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8888"));
```

Hoặc thêm method này:

```java
public class ServerMain {
    private static int getPort() {
        String port = System.getenv("PORT");
        return port != null ? Integer.parseInt(port) : 8888;
    }

    public static void main(String[] args) {
        int port = getPort();
        System.out.println("Server starting on port: " + port);
        // ... rest of code
    }
}
```

### BƯỚC 2: Tạo JAR File (có dependencies)

Thêm vào `server/pom.xml`:

```xml
<build>
    <plugins>
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
    </plugins>
</build>
```

Build:

```bash
mvn -f server/pom.xml clean package -DskipTests
# → Tạo file: server/target/bidnova-server.jar
```

### BƯỚC 3: Push lên GitHub

```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

---

## OPTION A: Deploy với Dockerfile (RECOMMENDED - Đơn Giản)

### Trên Render.com:

1. **Vào https://dashboard.render.com**
2. **Click "New +" → "Web Service"**
3. **Chọn GitHub Repo của bạn**
4. **Cấu Hình:**
    - Runtime: `Docker`
    - Build Command: `docker build -t bidnova .`
    - Start Command: (để trống, sẽ dùng CMD trong Dockerfile)

5. **Environment Variables:**

    ```
    PORT=10000
    DB_URL=jdbc:mysql://zephyr.proxy.rlwy.net:32243/auction_db
    DB_USER=root
    DB_PASSWORD=vGhcIvAXFhNwzGTOOWZTIGmJvyShpoST
    JWT_SECRET=E4aKywl5o8u6LekAb9jKIr0Nqduhet6+n4QBDFzSLJU=
    ```

6. **Click "Create Web Service"**

**Xong!** Deploy tự động khi push code

---

## OPTION B: Deploy với render.yaml (Nếu muốn)

Tạo file `render.yaml`:

```yaml
services:
    - type: web
      name: bidnova-server
      plan: starter

      dockerfile: ./Dockerfile
      dockerContext: ./

      envVars:
          - key: PORT
            value: 10000
          - key: DB_URL
            value: jdbc:mysql://zephyr.proxy.rlwy.net:32243/auction_db
          - key: DB_USER
            value: root
          - key: DB_PASSWORD
            sync: false
```

Sau đó add secret variables trên Render dashboard.

---

## OPTION C: Deploy với Procfile (Simplest)

Cái này nếu không dùng Docker, dùng Procfile:

```
web: java -Xmx512m -jar server/target/bidnova-server.jar
```

Nhưng **yêu cầu** Maven phải build JAR thành công trước.

---

## CHECKLIST DEPLOY

- [ ] Cập nhật `ServerMain.java` để dùng PORT từ env
- [ ] Thêm `maven-shade-plugin` vào `server/pom.xml`
- [ ] Build JAR: `mvn -f server/pom.xml clean package -DskipTests`
- [ ] Verify JAR tạo thành công: `ls server/target/bidnova-server.jar`
- [ ] Thêm Dockerfile vào root (nếu chọn Option A)
- [ ] Commit & push: `git add . && git commit -m "Deploy ready" && git push`
- [ ] Trên Render: Connect GitHub repo
- [ ] Set Environment Variables
- [ ] Click Deploy

---

## 🧪 TEST LOCAL (Before Deploy)

```bash
# Build JAR
mvn -f server/pom.xml clean package -DskipTests

# Test run với env variables
PORT=8888 \
DB_URL="jdbc:mysql://localhost:3306/auction_db" \
DB_USER="root" \
DB_PASSWORD="password" \
java -jar server/target/bidnova-server.jar
```

Nếu thấy:

```
Server starting on port: 8888
Database connected successfully
```

→ Bạn ready để deploy!

---

## 🔗 QUICK LINKS

- **Render Dashboard:** https://dashboard.render.com
- **Render Docs:** https://render.com/docs
- **Maven Shade Plugin:** https://maven.apache.org/plugins/maven-shade-plugin/

---

## ❌ TROUBLESHOOTING

### Error: "Port already in use"

→ Render assigns dynamic port, chỉnh `PORT` env var

### Error: "Cannot connect to database"

→ Kiểm tra `DB_URL`, `DB_USER`, `DB_PASSWORD` env vars

### Error: "JAR not found"

→ Confirm `maven-shade-plugin` config, run `mvn clean package` locally

### Error: "Java version not found"

→ Confirm Java 25 dependency, hoặc downgrade tới Java 21
