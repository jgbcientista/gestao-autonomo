# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR do build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
