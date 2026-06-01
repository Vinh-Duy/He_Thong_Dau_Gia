FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY server/ server/
COPY client/ client/

RUN mvn clean package -DskipTests -pl server

# ============ Runtime Stage ============
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

COPY --from=builder /app/server/target/bidnova-server.jar app.jar

EXPOSE 8080

CMD ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
