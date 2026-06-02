#!/usr/bin/env zsh

# ================================================================
# QUICK START: Build & Run Client Connecting to Render Server
# ================================================================
# Usage: source run-client.sh

set -e

echo "🚀 Starting BidNova Client Build..."
echo ""

# Navigate to project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Build
echo "📦 Building client package..."
mvn clean package -f client/pom.xml -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✅ Build complete!"
    echo ""
    echo "🔌 Starting client - connecting to Render server..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📍 Server: bidnova-server.onrender.com:8888"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    # Run with environment variables
    AUCTION_SERVER_HOST="bidnova-server.onrender.com" \
    AUCTION_SERVER_PORT="8888" \
    java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
else
    echo "❌ Build failed! Check errors above."
    exit 1
fi
