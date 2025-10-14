# Use OpenJDK 17 como base
FROM openjdk:17-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Copiar o JAR já compilado
COPY api-author/target/auth-service-1.0.0.jar app.jar

# Expor porta da aplicação
EXPOSE 8080

# Comando para executar aplicação
CMD ["java", "-jar", "app.jar"] 