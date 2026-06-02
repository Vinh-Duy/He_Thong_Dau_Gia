#!/usr/bin/env zsh

# ================================================================
# QUICK START: Build & Run Client Connecting to Render Server
# ================================================================
# Usage: bash run-client.sh

set -e

echo "🚀 Starting BidNova Client..."
echo ""

# Navigate to project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Check if JAR already built
if [ ! -f "client/target/BidNova-Client.jar" ]; then
    echo "📦 Building client package..."
    mvn clean package -f client/pom.xml -DskipTests -q
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
    echo "✅ Build complete!"
else
    echo "✅ Using cached build..."
fi

echo ""
echo "🔌 Starting client - connecting to Render server..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📍 Server: bidnova-server.onrender.com:8888"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Get JavaFX module path from Maven
echo "⚙️  Resolving JavaFX modules..."
JAVAFX_MODULES=$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout -f client/pom.xml 2>/dev/null | grep -o "[^:]*javafx[^:]*" | head -1 || echo "")

# Run using Maven exec (best for JavaFX)
AUCTION_SERVER_HOST="bidnova-server.onrender.com" \
AUCTION_SERVER_PORT="8888" \
mvn -q clean javafx:run -f client/pom.xml \
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main
