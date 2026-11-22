FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY . .
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --home-dir /home/app --shell /usr/sbin/nologin datashop
COPY --from=build /build/target/datashop.jar app.jar
USER datashop
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
