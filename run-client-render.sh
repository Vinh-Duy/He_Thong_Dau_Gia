#!/bin/bash

# Script chạy BidNova Client kết nối tới Server trên Render
# Sử dụng: ./run-client-render.sh

echo "🚀 Building BidNova Client..."
cd "$(dirname "$0")"
mvn clean package -f client/pom.xml -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo "🔌 Starting Client - Connecting to Render Server..."
echo ""

# Configuration
RENDER_SERVER_HOST="bidnova-server.onrender.com"
RENDER_SERVER_PORT="8888"

echo "📍 Server: $RENDER_SERVER_HOST:$RENDER_SERVER_PORT"
echo ""

# Run client with environment variables pointing to Render
AUCTION_SERVER_HOST="$RENDER_SERVER_HOST" \
AUCTION_SERVER_PORT="$RENDER_SERVER_PORT" \
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar

echo ""
echo "❌ Client disconnected"
