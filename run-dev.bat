@echo off
REM ================================================================
REM LOCAL DEVELOPMENT: Build & Run Server + Client Together (Windows)
REM ================================================================
REM Usage: run-dev.bat

setlocal enabledelayedexpansion

echo.
echo 🚀 BidNova Local Development Setup
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

cd /d "%~dp0"
set PROJECT_ROOT=%cd%

echo 📦 Building Server...
call mvn clean package -f server/pom.xml -DskipTests -q

if !ERRORLEVEL! neq 0 (
    echo ❌ Server build failed!
    pause
    exit /b 1
)

echo ✅ Server build complete!
echo.

echo 📦 Building Client...
call mvn clean package -f client/pom.xml -DskipTests -q

if !ERRORLEVEL! neq 0 (
    echo ❌ Client build failed!
    pause
    exit /b 1
)

echo ✅ Client build complete!
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🔌 Starting Server in new window...
echo.

REM Start server in new window
start "BidNova Server" /min java -cp "server/target/classes;server/target/dependency/*" ^
    com.bidnova.ServerMain

timeout /t 3 /nobreak

echo ✅ Server started!
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🚀 Starting Client (connecting to localhost:8888)...
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

REM Start client
call mvn -q clean javafx:run -f client/pom.xml ^
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main

if !ERRORLEVEL! neq 0 (
    echo.
    echo ❌ Client failed to start!
    pause
    exit /b 1
)

echo ✅ Done!
pause
