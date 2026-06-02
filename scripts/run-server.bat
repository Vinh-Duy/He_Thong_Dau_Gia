@echo off
REM ================================================================
REM Run Server - Network Mode (Windows)
REM ================================================================
REM Usage: run-server.bat

setlocal enabledelayedexpansion

cd /d "%~dp0"
set PROJECT_ROOT=%cd%

echo.
echo 🚀 BidNova Server - Starting...
echo.

REM Check if JAR exists
if not exist "%PROJECT_ROOT%\server\target\bidnova-server.jar" (
    echo 📦 Building server...
    call mvn clean package -f server/pom.xml -DskipTests -q
    echo ✅ Build complete!
    echo.
)

REM Run server
java -jar "%PROJECT_ROOT%\server\target\bidnova-server.jar"

pause
