Write-Host "=== TESTE ESPECIFICO ERRO 500 - REGISTRO ===" -ForegroundColor Red

# Teste 1: Verificar se API responde
Write-Host "`n1. Testando conectividade da API..." -ForegroundColor Yellow
try {
    $status = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET
    Write-Host "   ✅ API funcionando - Status: $($status.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "   ❌ API nao responde: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Teste 2: Tentar registrar usuario
Write-Host "`n2. Testando registro de usuario..." -ForegroundColor Yellow

$registroBody = @{
    name = "Usuario Teste"
    email = "usuario.teste@example.com"
    password = "123456"
} | ConvertTo-Json

Write-Host "   Payload: $registroBody" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -ContentType "application/json" -Body $registroBody
    Write-Host "   ✅ Registro OK - Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   📄 Resposta: $($response.Content)" -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "   ❌ ERRO $statusCode - $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "   📄 Erro detalhado: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "   ⚠️ Nao foi possivel ler detalhes do erro" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n=== FIM DO TESTE ===" -ForegroundColor Red 