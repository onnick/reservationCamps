FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY pom.xml .
COPY config/ config/
COPY src/ src/

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN useradd --uid 10001 --create-home --shell /usr/sbin/nologin app && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/target/reservationCamps-0.1.0-SNAPSHOT.jar /app/app.jar

USER 10001
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]

