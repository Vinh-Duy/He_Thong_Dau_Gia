# ================================================================
# QUICK START: Build & Run Client on Windows (PowerShell)
# ================================================================
# Usage: .\run-client.ps1

param(
    [string]$Host = "bidnova-server.onrender.com",
    [int]$Port = 8888
)

Write-Host ""
Write-Host "🚀 Starting BidNova Client Build..." -ForegroundColor Cyan
Write-Host ""

# Get project root
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

Write-Host "📦 Building client package..." -ForegroundColor Yellow

# Build
& mvn clean package -f client/pom.xml -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Build failed! Check errors above." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✅ Build complete!" -ForegroundColor Green
Write-Host ""
Write-Host "🔌 Starting client - connecting to Render server..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host "📍 Server: $Host`:$Port" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

# Set environment variables
$env:AUCTION_SERVER_HOST = $Host
$env:AUCTION_SERVER_PORT = $Port

# Run the client
$JarPath = Join-Path $ProjectRoot "client\target\BidNova-Client.jar"

if (-Not (Test-Path $JarPath)) {
    Write-Host "❌ JAR file not found: $JarPath" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Launching Java application..." -ForegroundColor Green
java -jar $JarPath

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Client failed to start!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
