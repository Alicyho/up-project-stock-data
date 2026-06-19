FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 先複製 pom.xml，利用 Docker layer cache 加快重建
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=railway
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Xmx384m", "-jar", "app.jar"]
