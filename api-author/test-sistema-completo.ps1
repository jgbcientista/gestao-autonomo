Write-Host "TESTE COMPLETO - SISTEMA DE AUTENTICACAO" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. VERIFICAR STATUS DOS SERVICOS
Write-Host "`n1. VERIFICANDO STATUS DOS SERVICOS..." -ForegroundColor Yellow

# Verificar portas
Write-Host "Portas ativas:"
netstat -ano | Select-String ":4200|:8081"

# Teste Backend
Write-Host "`n2. TESTANDO BACKEND..." -ForegroundColor Yellow
try {
    $status = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET
    Write-Host "✅ Backend OK - Status: $($status.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend ERRO: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. TESTE DE REGISTRO
Write-Host "`n3. TESTANDO REGISTRO..." -ForegroundColor Yellow

$bodyRegistro = '{"name":"Test User","email":"test@example.com","password":"123456"}'
try {
    $registro = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -ContentType "application/json" -Body $bodyRegistro
    Write-Host "✅ Registro OK - Status: $($registro.StatusCode)" -ForegroundColor Green
    Write-Host "Resposta: $($registro.Content.Substring(0, 100))..." -ForegroundColor White
    
    # Extrair dados para login
    $regResponse = $registro.Content | ConvertFrom-Json
    $userEmail = $regResponse.email
    
} catch {
    Write-Host "❌ Registro ERRO: $($_.Exception.Message)" -ForegroundColor Red
    $userEmail = "test@example.com"  # fallback
}

# 4. TESTE DE LOGIN
Write-Host "`n4. TESTANDO LOGIN..." -ForegroundColor Yellow

$bodyLogin = "{`"email`":`"$userEmail`",`"password`":`"123456`",`"location`":`"São Paulo, BR`"}"
try {
    $login = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Method POST -ContentType "application/json" -Body $bodyLogin
    Write-Host "✅ Login OK - Status: $($login.StatusCode)" -ForegroundColor Green
    
    # Extrair token
    $loginResponse = $login.Content | ConvertFrom-Json
    $token = $loginResponse.token
    Write-Host "Token extraido: $($token.Substring(0, 30))..." -ForegroundColor White
    
} catch {
    Write-Host "❌ Login ERRO: $($_.Exception.Message)" -ForegroundColor Red
    $token = $null
}

# 5. TESTE DE VALIDACAO TOKEN
Write-Host "`n5. TESTANDO VALIDACAO TOKEN..." -ForegroundColor Yellow

if ($token) {
    try {
        $validate = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/validar-token?token=$token" -Method POST
        Write-Host "✅ Validacao Token OK - Status: $($validate.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Validacao Token ERRO: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️ Token nao disponivel para teste" -ForegroundColor Yellow
}

# 6. TESTE FRONTEND
Write-Host "`n6. TESTANDO FRONTEND..." -ForegroundColor Yellow

try {
    $frontend = Invoke-WebRequest -Uri "http://localhost:4200" -Method GET -TimeoutSec 5
    Write-Host "✅ Frontend OK - Status: $($frontend.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Frontend ERRO: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=========================================" -ForegroundColor Cyan  
Write-Host "TESTE COMPLETO FINALIZADO!" -ForegroundColor Cyan 