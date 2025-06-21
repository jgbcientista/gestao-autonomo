Write-Host "=== TESTE SWAGGER CORRIGIDO ===" -ForegroundColor Yellow

# Teste 1: Verificar se o Swagger UI está acessível
Write-Host "`n1. Testando acesso ao Swagger UI..." -ForegroundColor Cyan
try {
    $swaggerResponse = Invoke-WebRequest -Uri "http://localhost:8081/swagger-ui.html" -Method GET -TimeoutSec 10
    Write-Host "✓ Swagger UI acessível - Status: $($swaggerResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro ao acessar Swagger UI: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 2: Verificar se o API docs está funcionando
Write-Host "`n2. Testando API docs..." -ForegroundColor Cyan
try {
    $apiDocsResponse = Invoke-RestMethod -Uri "http://localhost:8081/api-docs" -Method GET -TimeoutSec 10
    Write-Host "✓ API docs funcionando" -ForegroundColor Green
    
    # Verificar se as URLs estão corretas
    $apiDocsJson = $apiDocsResponse | ConvertTo-Json -Depth 10
    
    if ($apiDocsJson -match '/api/v1/api/v1/') {
        Write-Host "✗ URLs duplicadas ainda presentes no API docs!" -ForegroundColor Red
    } else {
        Write-Host "✓ URLs corretas no API docs (sem duplicação)" -ForegroundColor Green
    }
    
    # Verificar servidor base URL
    if ($apiDocsResponse.servers) {
        Write-Host "Servidores configurados:" -ForegroundColor Yellow
        foreach ($server in $apiDocsResponse.servers) {
            Write-Host "  - $($server.url): $($server.description)" -ForegroundColor White
        }
    }
    
} catch {
    Write-Host "✗ Erro ao acessar API docs: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 3: Verificar endpoint de autenticação diretamente
Write-Host "`n3. Testando endpoint de autenticação..." -ForegroundColor Cyan

$loginData = @{
    email = "joaoguedes@gmail.com"
    password = "1234567890"
    location = "São Paulo, BR"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Method POST -Body $loginData -ContentType "application/json" -TimeoutSec 10
    Write-Host "✓ Endpoint funcionando corretamente" -ForegroundColor Green
    
    if ($loginResponse.token) {
        Write-Host "✓ Token retornado com sucesso" -ForegroundColor Green
    } else {
        Write-Host "⚠ Endpoint funcionou mas token está vazio" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Erro no endpoint: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

Write-Host "`n=== INSTRUÇÕES ===" -ForegroundColor Yellow
Write-Host "1. Acesse: http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "2. Verifique se as URLs não têm /api/v1 duplicado" -ForegroundColor White
Write-Host "3. Teste o endpoint POST /api/v1/autenticacao/entrar" -ForegroundColor White
Write-Host "4. Use os dados:" -ForegroundColor White
Write-Host "   {" -ForegroundColor Gray
Write-Host "     'email': 'joaoguedes@gmail.com'," -ForegroundColor Gray
Write-Host "     'password': '1234567890'," -ForegroundColor Gray
Write-Host "     'location': 'São Paulo, BR'" -ForegroundColor Gray
Write-Host "   }" -ForegroundColor Gray 