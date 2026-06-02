@echo off
REM ================================================================
REM QUICK START: Build & Run Client on Windows
REM ================================================================
REM Usage: run-client.bat

setlocal enabledelayedexpansion

echo.
echo 🚀 Starting BidNova Client Build...
echo.

REM Get project root
cd /d "%~dp0"
set PROJECT_ROOT=%cd%

echo 📦 Building client package...
call mvn clean package -f client/pom.xml -DskipTests -q

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ Build failed! Check errors above.
    pause
    exit /b 1
)

echo ✅ Build complete!
echo.
echo 🔌 Starting client - connecting to Render server...
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📍 Server: bidnova-server.onrender.com:8888
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

REM Set environment variables
set AUCTION_SERVER_HOST=bidnova-server.onrender.com
set AUCTION_SERVER_PORT=8888

REM Run the client
java -jar "%PROJECT_ROOT%\client\target\BidNova-Client.jar"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ Client failed to start!
    pause
    exit /b 1
)

pause
