# syntax=docker/dockerfile:1

# --- Build stage: Maven builds the backend AND orchestrates the Angular build via
# frontend-maven-plugin (which downloads its own Node/npm - no OS Node package needed here). ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache Maven dependencies separately from source so a source-only change doesn't re-download them.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true

COPY src ./src
COPY frontend ./frontend
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage: JRE only, no JDK/Maven/Node left in the final image. ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Railway (and most PaaS hosts) inject PORT at runtime; application.properties resolves
# server.port=${PORT:62828}, so no extra flag is needed here.
EXPOSE 62828
ENTRYPOINT ["java", "-jar", "app.jar"]
