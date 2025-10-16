# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar pom.xml e settings.xml
COPY api-author/pom.xml .
COPY api-author/settings.xml .

# Baixar dependências
RUN mvn dependency:go-offline -s settings.xml

# Copiar código fonte
COPY api-author/src ./src

# Compilar aplicação
RUN mvn clean package -DskipTests -s settings.xml

# Production stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar JAR compilado do stage anterior
COPY --from=build /app/target/auth-service-1.0.0.jar app.jar

# Expor porta da aplicação
EXPOSE 8080

# Comando para executar aplicação
CMD ["java", "-jar", "app.jar"] 