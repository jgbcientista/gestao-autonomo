Write-Host "🧪 TESTANDO DADOS DA BLOCKCHAIN" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

$BASE_URL = "http://localhost:8081"
$TEST_URL = "$BASE_URL/api/blockchain/test"

Write-Host "⏳ Aguardando aplicação..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "`n🔍 TESTE 1: HEALTH CHECK" -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "✅ Aplicação: OK" -ForegroundColor Green
    $health | ConvertTo-Json | Write-Host -ForegroundColor White
} catch {
    Write-Host "❌ Aplicação não disponível" -ForegroundColor Red
}

Write-Host "`n🔍 TESTE 2: CONEXÃO BLOCKCHAIN" -ForegroundColor Cyan
try {
    $connection = Invoke-RestMethod -Uri "$TEST_URL/connection" -Method GET -TimeoutSec 10
    Write-Host "✅ Conexão: OK" -ForegroundColor Green
    $connection | ConvertTo-Json | Write-Host -ForegroundColor White
} catch {
    Write-Host "❌ Erro na conexão: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔍 TESTE 3: STATUS DA REDE" -ForegroundColor Cyan
try {
    $status = Invoke-RestMethod -Uri "$TEST_URL/status" -Method GET -TimeoutSec 10
    Write-Host "✅ Status: OK" -ForegroundColor Green
    $status | ConvertTo-Json | Write-Host -ForegroundColor White
} catch {
    Write-Host "❌ Erro no status: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔍 TESTE 4: REGISTRAR EVENTO" -ForegroundColor Cyan
$evento = @{
    userId = "usuario_teste_$(Get-Date -Format 'yyyyMMddHHmmss')"
    action = "login"
    status = "success"
    details = "Teste via PowerShell"
}

try {
    $headers = @{ "Content-Type" = "application/json" }
    $body = $evento | ConvertTo-Json
    $result = Invoke-RestMethod -Uri "$TEST_URL/event" -Method POST -Body $body -Headers $headers -TimeoutSec 10
    Write-Host "✅ Evento registrado: OK" -ForegroundColor Green
    $result | ConvertTo-Json | Write-Host -ForegroundColor White
} catch {
    Write-Host "❌ Erro ao registrar: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔍 TESTE 5: ESTATÍSTICAS" -ForegroundColor Cyan
try {
    $stats = Invoke-RestMethod -Uri "$TEST_URL/statistics" -Method GET -TimeoutSec 10
    Write-Host "✅ Estatísticas: OK" -ForegroundColor Green
    $stats | ConvertTo-Json | Write-Host -ForegroundColor White
} catch {
    Write-Host "❌ Erro nas estatísticas: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 TESTES CONCLUÍDOS!" -ForegroundColor Green
Write-Host "Verifique os dados acima para ver os resultados da blockchain." -ForegroundColor Yellow 