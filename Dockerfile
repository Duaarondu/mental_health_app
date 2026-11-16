# ---------- Stage 1: build the Spring Boot jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Download dependencies first so Docker can cache them between builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Then copy the source code and build (tests run in CI, so skip them here)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: run it with a small Java runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
