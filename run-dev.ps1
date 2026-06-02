# ================================================================
# LOCAL DEVELOPMENT: Build & Run Server + Client Together (PowerShell)
# ================================================================
# Usage: .\run-dev.ps1

Write-Host ""
Write-Host "🚀 BidNova Local Development Setup" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

Write-Host "📦 Building Server..." -ForegroundColor Yellow

& mvn clean package -f server/pom.xml -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Server build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✅ Server build complete!" -ForegroundColor Green
Write-Host ""

Write-Host "📦 Building Client..." -ForegroundColor Yellow

& mvn clean package -f client/pom.xml -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Client build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✅ Client build complete!" -ForegroundColor Green
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host "🔌 Starting Server..." -ForegroundColor Cyan
Write-Host ""

# Start server in background job
$ServerJob = Start-Job -ScriptBlock {
    param($Root)
    Set-Location $Root
    & java -cp "server/target/classes;server/target/dependency/*" `
        com.bidnova.ServerMain
} -ArgumentList $ProjectRoot

Write-Host "✅ Server started (Job ID: $($ServerJob.Id))" -ForegroundColor Green
Write-Host ""

# Wait for server to start
Start-Sleep -Seconds 3

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host "🚀 Starting Client (connecting to localhost:8888)..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

# Start client
& mvn -q clean javafx:run -f client/pom.xml `
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main

# Stop server when client exits
Write-Host ""
Write-Host "🛑 Stopping server..." -ForegroundColor Yellow
Stop-Job -Job $ServerJob
Remove-Job -Job $ServerJob

Write-Host "✅ Done!" -ForegroundColor Green
