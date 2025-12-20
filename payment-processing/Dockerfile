# Usamos una imagen base de Java 17 o 21
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiamos el jar generado (asegúrate de hacer mvn clean package primero)
COPY target/payment-processing-service-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8081
EXPOSE 8081

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]