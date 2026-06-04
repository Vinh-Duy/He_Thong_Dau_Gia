#!/usr/bin/env zsh

# ==================================================================
# QUICK START: Build & Run Client Connecting to Render Server
# ================================================================
# Usage: bash run-client.sh

set -e

echo "Starting BidNova Client..."
echo ""

# Navigate to project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Check if JAR already built
if [ ! -f "client/target/BidNova-Client.jar" ]; then
    echo " Building client package..."
    mvn clean package -f client/pom.xml -DskipTests -q
    
    if [ $? -ne 0 ]; then
        echo " Build failed!"
        exit 1
    fi
    echo " Build complete!"
else
    echo " Using cached build..."
fi

echo ""
echo "🔌 Starting client..."

# Set default environment variables
DEFAULT_AUCTION_SERVER_HOST="bidnova-server.onrender.com"
DEFAULT_AUCTION_SERVER_PORT="8888"

# Check for command-line arguments (Usage: bash run-client.sh [IP] [PORT])
AUCTION_SERVER_HOST=${1:-$DEFAULT_AUCTION_SERVER_HOST}
AUCTION_SERVER_PORT=${2:-$DEFAULT_AUCTION_SERVER_PORT}

echo " Server: ${AUCTION_SERVER_HOST}:${AUCTION_SERVER_PORT}"
echo ""

# Get JavaFX module path from Maven
echo " Resolving JavaFX modules..."
# This part is not strictly necessary for `javafx:run` but kept for consistency if you ever use `java -jar` directly
# JAVAFX_MODULES=$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout -f client/pom.xml 2>/dev/null | grep -o "[^:]*javafx[^:]*" | head -1 || echo "")

# Run using Maven exec (best for JavaFX)
mvn -q clean javafx:run -f client/pom.xml \
    -Djavafx.maven.plugin.mainClass=com.bidnova.Main \
    -DAUCTION_SERVER_HOST="${AUCTION_SERVER_HOST}" \
    -DAUCTION_SERVER_PORT="${AUCTION_SERVER_PORT}"

if [ $? -ne 0 ]; then
    echo " Client failed to start!"
    exit 1
fi
