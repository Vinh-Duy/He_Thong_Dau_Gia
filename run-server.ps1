# ================================================================
# Run Server - Network Mode (Windows PowerShell)
# ================================================================
# Usage: .\run-server.ps1

Write-Host ""
Write-Host "🚀 BidNova Server - Starting..." -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# Check if JAR exists
if (-Not (Test-Path "server/target/bidnova-server.jar")) {
    Write-Host "📦 Building server..." -ForegroundColor Yellow
    & mvn clean package -f server/pom.xml -DskipTests -q
    Write-Host "✅ Build complete!" -ForegroundColor Green
    Write-Host ""
}

# Run server
Write-Host "Running server..." -ForegroundColor Green
& java -jar "server/target/bidnova-server.jar"
