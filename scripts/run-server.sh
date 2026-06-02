#!/usr/bin/env zsh

# ================================================================
# Run Server - Network Mode (Listen on all interfaces)
# ================================================================
# Usage: bash run-server.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo ""
echo "🚀 BidNova Server - Starting..."
echo ""

# Build nếu chưa có
if [ ! -f "server/target/bidnova-server.jar" ]; then
    echo "📦 Building server..."
    mvn clean package -f server/pom.xml -DskipTests -q
    echo "✅ Build complete!"
    echo ""
fi

# Run server
java -jar server/target/bidnova-server.jar
