# ── Build Stage ──────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q
COPY src src
RUN ./gradlew bootJar -x test --no-daemon -q

# ── Runtime Stage ─────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 21002
ENTRYPOINT ["java", \
  "-Xms128m", "-Xmx256m", \
  "-XX:+UseSerialGC", \
  "-XX:MaxMetaspaceSize=128m", \
  "-jar", "app.jar"]
