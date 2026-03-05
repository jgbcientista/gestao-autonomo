@echo off
REM Configurações de Banco de Dados
setx DB_URL "jdbc:postgresql://localhost:5432/auth_db"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "postgres"

REM Configurações de E-mail (Gmail)
setx MAIL_USERNAME "joaoguedesdebrito@gmail.com"
setx MAIL_PASSWORD "sua_senha_de_app"

REM Configurações JWT
setx JWT_SECRET "***REMOVIDO***"
setx JWT_EXPIRATION "86400000"

REM Configurações de Geolocalização
setx OPENCAGE_API_KEY "sua_chave_api"

REM Configurações de IA (Python AI Service)
setx AI_SERVICE_URL "http://localhost:5000"
setx AI_SERVICE_ENABLED "false"

REM Configurações de Blockchain
setx BLOCKCHAIN_PRIVATE_KEY "chave_privada"
setx BLOCKCHAIN_CONTRACT_ADDRESS "endereco_contrato"
setx BLOCKCHAIN_CONFIRMATION_BLOCKS "12"
setx HYPERLEDGER_CERT_PATH "caminho_certificado"
setx HYPERLEDGER_KEY_PATH "caminho_chave_privada"

REM Configurações Redis
setx REDIS_HOST "localhost"
setx REDIS_PORT "6379"
setx REDIS_PASSWORD "senha_redis"

REM URL Base da Aplicação
setx APP_BASE_URL "http://localhost:4200"

REM Configurações Keycloak
setx KEYCLOAK_URL "http://localhost:8180"
setx KEYCLOAK_ISSUER_URI "http://localhost:8180/realms/auth-system"
setx KEYCLOAK_JWK_SET_URI "http://localhost:8180/realms/auth-system/protocol/openid-connect/certs"

REM Configurações ELK Stack
setx LOGSTASH_HOST "logstash"
setx LOGSTASH_PORT "5044"

echo Variaveis de ambiente configuradas com sucesso!
echo Por favor, feche e abra novamente o terminal para que as alteracoes tenham efeito.
pause
