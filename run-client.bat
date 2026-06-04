@echo off
REM ================================================================
REM QUICK START: Build & Run Client on Windows
REM ================================================================
REM Usage: run-client.bat

setlocal enabledelayedexpansion

echo.
echo 🚀 Starting BidNova Client...
echo.

REM Get project root
cd /d "%~dp0"
set PROJECT_ROOT=%cd%

REM Check if JAR already exists
if exist "%PROJECT_ROOT%\client\target\BidNova-Client.jar" (
    echo Using cached build...
) else (
    echo Building client package...
    call mvn clean package -f client/pom.xml -DskipTests -q
    
    if !ERRORLEVEL! neq 0 (
        echo.
        echo Build failed! Check errors above.
        pause
        exit /b 1
    )
    echo Build complete!
)

echo.
echo 🔌 Starting client...
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REM Check for command-line arguments (Usage: run-client.bat [IP] [PORT])
if "%1"=="" (
    set AUCTION_SERVER_HOST=bidnova-server.onrender.com
) else (
    set AUCTION_SERVER_HOST=%1
)

if "%2"=="" (
    set AUCTION_SERVER_PORT=8888
) else (
    set AUCTION_SERVER_PORT=%2
)

echo Server: !AUCTION_SERVER_HOST!:!AUCTION_SERVER_PORT!
echo.

REM Run using Maven exec (handles JavaFX properly)
echo  Starting application via Maven...
echo.

call mvn -q clean javafx:run -f client/pom.xml ^
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main ^
    -DAUCTION_SERVER_HOST=!AUCTION_SERVER_HOST! ^
    -DAUCTION_SERVER_PORT=!AUCTION_SERVER_PORT!

if !ERRORLEVEL! neq 0 (
    echo.
    echo Client failed to start!
    pause
    exit /b 1
)

pause
