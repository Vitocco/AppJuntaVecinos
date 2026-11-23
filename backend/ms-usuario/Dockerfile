# Etapa 1: compilar con Maven (la caché de ~/.m2 se reutiliza entre builds)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml settings.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -s settings.xml -q -B -DskipTests package

# Etapa 2: imagen liviana solo con el JRE y el .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
