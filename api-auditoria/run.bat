@echo off
echo ================================
echo    API AUDITORIA - AUDITORIA SERVICE
echo ================================
echo.
echo Escolha uma opcao:
echo 1. Executar com Docker (Recomendado)
echo 2. Executar Maven local (pode ter problemas de dependencias)
echo 3. Compilar apenas (teste)
echo 4. Limpar projeto
echo.
set /p choice=Digite sua opcao (1-4): 

if "%choice%"=="1" goto docker
if "%choice%"=="2" goto maven
if "%choice%"=="3" goto compile
if "%choice%"=="4" goto clean
goto end

:docker
echo.
echo ========================================
echo Executando com Docker...
echo ========================================
cd ..
docker-compose up --build auditoria-service
goto end

:maven
echo.
echo ========================================
echo Executando com Maven local...
echo ========================================
mvn clean spring-boot:run -s settings.xml
goto end

:compile
echo.
echo ========================================
echo Compilando projeto...
echo ========================================
mvn clean compile -s settings.xml
goto end

:clean
echo.
echo ========================================
echo Limpando projeto...
echo ========================================
mvn clean
rmdir /s /q target 2>nul
echo Projeto limpo!
goto end

:end
echo.
echo ========================================
echo Processo finalizado!
echo ========================================
pause 