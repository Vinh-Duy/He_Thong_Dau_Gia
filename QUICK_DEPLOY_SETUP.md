# 🚀 Deploy BidNova to Render - QUICK SETUP

**Updated:** 1 Tháng 6, 2026  
**Status:** ✅ Ready to Deploy

---

## 📝 TÓM TẮT NHỮNG GÌ ĐÃ CỰC:

Vì database đã có trong `.env`, nên chúng ta chỉ cần:

1. ✅ **Fix PORT** - Thay từ hardcoded 8888 → dynamic từ env
2. ✅ **Build JAR** - Thêm maven-shade-plugin để tạo standalone JAR
3. ✅ **Tạo Dockerfile** - Deploy dễ dàng
4. ✅ **Tạo docker-compose** - Test local trước

---

## 🎯 DEPLOY STEPS (3 bước đơn giản)

### STEP 1: Build & Test Local

```bash
# 1. Build JAR
mvn -f server/pom.xml clean package -DskipTests

# Verify file tạo thành công
ls server/target/bidnova-server.jar
```

**Expected output:**
```
-rw-r--r--  45MB bidnova-server.jar
```

---

### STEP 2: Test với Docker Compose (Optional)

```bash
# Start MySQL + Server locally
docker-compose up -d

# Check logs
docker-compose logs -f bidnova-server

# Stop
docker-compose down
```

Expected:
```
Server starting on port: 8080
Database connected successfully
```

---

### STEP 3: Push lên GitHub & Deploy trên Render

```bash
# 1. Commit changes
git add .
git commit -m "Ready for Render deployment - PORT from env, maven-shade-plugin, Dockerfile"
git push origin main

# 2. Trên Render Dashboard:
#    - New → Web Service
#    - Select GitHub repo
#    - Runtime: Docker
#    - Environment: (xem bước 4 dưới)
```

---

## 📋 RENDER CONFIGURATION

### Trên Dashboard Render (Step by Step):

**1. Create Web Service**
- Click "New +" → "Web Service"
- Select your GitHub repository

**2. Configure Build Settings**
```
Root Directory: (leave empty)
Build Command: (leave empty - uses Dockerfile)
Start Command: (leave empty - uses Dockerfile CMD)
```

**3. Set Environment Variables**
Click "Advanced" → "Add Environment Variable" cho mỗi cái:

```
PORT                 = 10000
DB_URL              = jdbc:mysql://zephyr.proxy.rlwy.net:32243/auction_db
DB_USER             = root
DB_PASSWORD         = vGhcIvAXFhNwzGTOOWZTIGmJvyShpoST
JWT_SECRET          = E4aKywl5o8u6LekAb9jKIr0Nqduhet6+n4QBDFzSLJU=
CLOUDINARY_UPLOAD_PRESET = bidnova_preset
CLOUD_NAME          = deuqbo9ul
```

**4. Plan: Starter** (hoặc Standard nếu cần performance)

**5. Click "Create Web Service"**

✅ Deploy sẽ tự động chạy!

---

## ✅ VERIFY DEPLOYMENT

Sau khi deploy xong:

```bash
# Check service running
curl https://bidnova-server.onrender.com/health

# Hoặc xem logs trên Render dashboard
# (Click service → Logs)
```

**Expected logs:**
```
Server starting on port: 10000
Database connected successfully
```

---

## 🐳 Local Development (Using Docker)

```bash
# Start everything
docker-compose up

# In another terminal, test connection
curl localhost:8080

# See logs
docker-compose logs -f bidnova-server

# Stop
docker-compose down -v
```

---

## 📂 FILES CREATED/MODIFIED

| File | Action | Purpose |
|------|--------|---------|
| `server/src/main/java/com/bidnova/ServerMain.java` | Modified | Read PORT from env |
| `server/pom.xml` | Modified | Add maven-shade-plugin |
| `Dockerfile` | Created | Multi-stage Docker build |
| `docker-compose.yml` | Created | Local dev environment |
| `RENDER_DEPLOY_SIMPLE.md` | Created | Detailed guide |
| `QUICK_DEPLOY_SETUP.md` | Created | This file |

---

## 🔧 TROUBLESHOOTING

### ❌ Build fails: "Cannot find main class"
**Fix:** Verify `maven-shade-plugin` in `server/pom.xml`

### ❌ JAR not created
**Fix:** Run `mvn clean package -DskipTests` without `-DskipTests` first to see full error

### ❌ Port already in use
**Fix:** Render will assign random port, check `PORT` env var is set to `10000`

### ❌ Cannot connect to database
**Fix:** Verify `DB_URL`, `DB_USER`, `DB_PASSWORD` match exactly

### ❌ Timeout during build
**Fix:** Upgrade to Standard plan on Render

---

## 🎁 BONUS: GitHub Actions CI/CD

File `.github/workflows/maven.yml` already exists and will:
- ✅ Build on every push
- ✅ Run tests
- ✅ Generate artifacts

No action needed, it's already working!

---

## 🎯 WHAT'S NEXT?

1. **Done:** Basic server deployment ✅
2. **Next:** Deploy client (JavaFX Desktop app) - separate topic
3. **Optional:** Add API monitoring, alerting

---

## 📞 SUPPORT

**Render Docs:** https://render.com/docs/docker  
**Maven Shade:** https://maven.apache.org/plugins/maven-shade-plugin/  
**Docker Docs:** https://docs.docker.com/

---

## 🎉 YOU'RE READY!

Tất cả đã sẵn sàng. Chỉ cần:
1. ✅ Build locally để confirm
2. ✅ Push lên GitHub
3. ✅ Deploy trên Render (3-5 phút)
4. ✅ Done! 🚀

