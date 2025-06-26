# Use OpenJDK 17 como base
FROM openjdk:17-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração Maven
COPY api-author/pom.xml .
COPY api-author/.mvn .mvn
COPY api-author/mvnw .
COPY api-author/mvnw.cmd .
COPY api-author/settings.xml .

# Dar permissão de execução ao mvnw
RUN chmod +x ./mvnw

# Baixar dependências (camada de cache)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY api-author/src ./src

# Construir aplicação
RUN ./mvnw clean package -DskipTests

# Expor porta da aplicação
EXPOSE 8080

# Comando para executar aplicação
CMD ["java", "-jar", "target/*.jar"] 