@echo off
REM ================================================================
REM NETWORK MODE: Client kết nối tới Server trên máy khác (Windows)
REM ================================================================
REM Usage: run-client-network.bat [SERVER_IP] [SERVER_PORT]
REM Example: run-client-network.bat 192.168.1.100 8888

setlocal enabledelayedexpansion

cd /d "%~dp0"
set PROJECT_ROOT=%cd%

REM Lấy input hoặc dùng default
set SERVER_IP=%1
set SERVER_PORT=%2

if "!SERVER_IP!"=="" (
    set SERVER_IP=127.0.0.1
)
if "!SERVER_PORT!"=="" (
    set SERVER_PORT=8888
)

echo.
echo 🚀 BidNova Client - Network Mode
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📍 Server: !SERVER_IP!:!SERVER_PORT!
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

REM Check if JAR exists
if not exist "%PROJECT_ROOT%\client\target\BidNova-Client.jar" (
    echo 📦 Building client...
    call mvn clean package -f client/pom.xml -DskipTests -q
    echo ✅ Build complete!
    echo.
)

echo 🔌 Starting client...
echo.

REM Set environment variables
set AUCTION_SERVER_HOST=!SERVER_IP!
set AUCTION_SERVER_PORT=!SERVER_PORT!

REM Run client
call mvn -q clean javafx:run -f client/pom.xml ^
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main

pause
