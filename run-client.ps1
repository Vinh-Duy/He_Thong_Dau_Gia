<#
.SYNOPSIS
    Quick Start: Build & Run BidNova Client on Windows (PowerShell).
.DESCRIPTION
    This script builds the BidNova client and runs it, allowing you to specify
    the server IP and port as command-line arguments. If no arguments are
    provided, it defaults to the Render server.
.PARAMETER ServerHost
    The IP address or hostname of the auction server. Defaults to 'bidnova-server.onrender.com'.
.PARAMETER ServerPort
    The port number of the auction server. Defaults to '8888'.
.EXAMPLE
    .\run-client.ps1
    Runs the client connecting to the default Render server.
.EXAMPLE
    .\run-client.ps1 192.168.1.100 8888
    Runs the client connecting to a local server at 192.168.1.100 on port 8888.
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "`n Starting BidNova Client..." -ForegroundColor Green

# Get project root
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $PSScriptRoot

# Check if JAR already built
if (Test-Path "client/target/BidNova-Client.jar") {
    Write-Host "`n Using cached build..." -ForegroundColor Green
} else {
    Write-Host "`n Building client package..." -ForegroundColor Cyan
    try {
        mvn clean package -f client/pom.xml -DskipTests -q
        Write-Host " Build complete!" -ForegroundColor Green
    } catch {
        Write-Host " Build failed! Check errors above." -ForegroundColor Red
        Read-Host "Press Enter to exit..."
        exit 1
    }
}

Write-Host "`nStarting client..." -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray

# Set default environment variables
$DefaultAuctionServerHost = "bidnova-server.onrender.com"
$DefaultAuctionServerPort = "8888"

# Check for command-line arguments (Usage: .\run-client.ps1 [IP] [PORT])
$AuctionServerHost = if ($args.Count -ge 1) { $args[0] } else { $DefaultAuctionServerHost }
$AuctionServerPort = if ($args.Count -ge 2) { $args[1] } else { $DefaultAuctionServerPort }

Write-Host "Server: $($AuctionServerHost):$($AuctionServerPort)" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "`n  Starting application via Maven..." -ForegroundColor Cyan

try {
    mvn -q clean javafx:run -f client/pom.xml -Djavafx.maven.plugin.mainClass=com.bidnova.Main -DAUCTION_SERVER_HOST="$AuctionServerHost" -DAUCTION_SERVER_PORT="$AuctionServerPort"
} catch {
    Write-Host "`n Client failed to start!" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit 1
}

Pop-Location
Read-Host "Press Enter to exit..."