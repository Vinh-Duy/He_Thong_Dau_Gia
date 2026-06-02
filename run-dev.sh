#!/usr/bin/env zsh

# ================================================================
# LOCAL DEVELOPMENT: Build & Run Server + Client Together
# ================================================================
# Usage: bash run-dev.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo ""
echo "🚀 BidNova Local Development Setup"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Build server
echo "📦 Building Server..."
mvn clean package -f server/pom.xml -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Server build failed!"
    exit 1
fi

echo "✅ Server build complete!"
echo ""

# Build client
echo "📦 Building Client..."
mvn clean package -f client/pom.xml -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Client build failed!"
    exit 1
fi

echo "✅ Client build complete!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔌 Starting Server in background..."
echo ""

# Start server in background
java -cp "server/target/classes:server/target/dependency/*" \
    com.bidnova.ServerMain &
SERVER_PID=$!

echo "✅ Server started with PID: $SERVER_PID"
echo ""

# Give server time to start
sleep 3

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Starting Client (connecting to localhost:8888)..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Start client (will connect to localhost server)
mvn -q clean javafx:run -f client/pom.xml \
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main

# Kill server when client exits
echo ""
echo "🛑 Stopping server..."
kill $SERVER_PID 2>/dev/null || true

echo "✅ Done!"
