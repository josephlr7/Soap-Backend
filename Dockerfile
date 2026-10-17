# Etapa 1: Compilacion con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de ejecucion
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Factura-Server/target/Factura-Server-0.0.1-SNAPSHOT.jar app.jar

# Puertos: 8085 (REST) y 8090 (SOAP)
EXPOSE 8085
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
