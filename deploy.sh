#!/bin/bash

# 🚀 BidNova Deploy Script
# Run this to prepare for Render deployment

echo "🔧 Building BidNova for deployment..."

# STEP 1: Build JAR
echo "📦 Building JAR with dependencies..."
mvn -f server/pom.xml clean package -DskipTests

if [ ! -f "server/target/bidnova-server.jar" ]; then
    echo "❌ Build failed! JAR not found"
    exit 1
fi

echo "✅ JAR built successfully!"
ls -lh server/target/bidnova-server.jar

# STEP 2: Show file size
JAR_SIZE=$(du -h server/target/bidnova-server.jar | cut -f1)
echo "📊 JAR Size: $JAR_SIZE"

# STEP 3: Verify Docker
echo ""
echo "🐳 Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker not found, skipping Docker check"
else
    echo "✅ Docker found"
    
    echo ""
    echo "🐳 Building Docker image..."
    docker build -t bidnova-server .
    
    if [ $? -eq 0 ]; then
        echo "✅ Docker image built successfully"
        docker images | grep bidnova
    else
        echo "❌ Docker build failed"
        exit 1
    fi
fi

# STEP 4: Git status
echo ""
echo "📝 Git status:"
git status --short

echo ""
echo "✅ READY TO DEPLOY!"
echo ""
echo "Next steps:"
echo "1. Review changes: git diff"
echo "2. Commit: git commit -am 'Deploy ready'"
echo "3. Push: git push origin main"
echo "4. Render: Create new Web Service from your GitHub repo"
echo "5. Set environment variables on Render"
echo ""
echo "Deploy in 3-5 minutes! 🚀"
