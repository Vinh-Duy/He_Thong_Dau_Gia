# ================================================================
# QUICK START: Build & Run Client on Windows (PowerShell)
# ================================================================
# Usage: .\run-client.ps1

param(
    [string]$Host = "10.11.80.227",
    [int]$Port = 8888
)

Write-Host ""
Write-Host "🚀 Starting BidNova Client..." -ForegroundColor Cyan
Write-Host ""

# Get project root
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# Check if JAR already exists
$JarPath = Join-Path $ProjectRoot "client\target\BidNova-Client.jar"
if (Test-Path $JarPath) {
    Write-Host "✅ Using cached build..." -ForegroundColor Green
} else {
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
}

Write-Host ""
Write-Host "🔌 Starting client - connecting to Render server..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host "📍 Server: $Host`:$Port" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

# Set environment variables
$env:AUCTION_SERVER_HOST = $Host
$env:AUCTION_SERVER_PORT = $Port

# Run using Maven exec (handles JavaFX properly)
Write-Host "⚙️  Starting application via Maven..." -ForegroundColor Yellow
Write-Host ""

& mvn -q clean javafx:run -f client/pom.xml `
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Client failed to start!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
