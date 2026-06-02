#!/usr/bin/env zsh

# ================================================================
# NETWORK MODE: Client kết nối tới Server trên máy khác
# ================================================================
# Usage: bash run-client-network.sh [SERVER_IP] [SERVER_PORT]
# Example: bash run-client-network.sh 192.168.1.100 8888

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Lấy input hoặc dùng default
SERVER_IP="${1:-127.0.0.1}"
SERVER_PORT="${2:-8888}"

echo ""
echo "🚀 BidNova Client - Network Mode"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📍 Server: $SERVER_IP:$SERVER_PORT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Build nếu chưa có
if [ ! -f "client/target/BidNova-Client.jar" ]; then
    echo "📦 Building client..."
    mvn clean package -f client/pom.xml -DskipTests -q
    echo "✅ Build complete!"
    echo ""
fi

echo "🔌 Starting client..."
echo ""

# Run with network configuration
AUCTION_SERVER_HOST="$SERVER_IP" \
AUCTION_SERVER_PORT="$SERVER_PORT" \
mvn -q clean javafx:run -f client/pom.xml \
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main
