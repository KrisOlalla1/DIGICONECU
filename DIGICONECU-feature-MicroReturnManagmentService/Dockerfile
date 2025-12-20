# Etapa 1: Construcción (Maven con Java 17)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Imagen liviana JRE 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del microservicio
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
