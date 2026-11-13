# ---- 1) frontend build ----
FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package*.json ./
RUN npm ci || npm install
COPY frontend/ ./
RUN npm run build

# ---- 2) backend build ----
FROM eclipse-temurin:21-jdk AS api
WORKDIR /src
COPY connectly/ ./
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests package

# ---- 3) runtime: jedan servis (API + frontend) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 connectly && mkdir -p /app/uploads && chown -R connectly /app
COPY --from=api /src/app/target/*.jar /app/app.jar
COPY --from=web /web/dist /app/static
ENV STATIC_DIR=/app/static \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
USER connectly
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
