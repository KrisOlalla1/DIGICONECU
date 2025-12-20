# Etapa 1: Construcción (Maven con Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Imagen liviana JRE 21)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del microservicio
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]