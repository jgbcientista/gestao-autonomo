Write-Host "🧪 TESTANDO DADOS DA BLOCKCHAIN" -ForegroundColor Green

$BASE_URL = "http://localhost:8081"
$TEST_URL = "$BASE_URL/api/blockchain/test"

Write-Host "Aguardando aplicacao..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "TESTE 1: HEALTH CHECK" -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "Aplicacao: OK" -ForegroundColor Green
    $health | ConvertTo-Json
} catch {
    Write-Host "Aplicacao nao disponivel" -ForegroundColor Red
}

Write-Host "TESTE 2: CONEXAO BLOCKCHAIN" -ForegroundColor Cyan
try {
    $connection = Invoke-RestMethod -Uri "$TEST_URL/connection" -Method GET -TimeoutSec 10
    Write-Host "Conexao: OK" -ForegroundColor Green
    $connection | ConvertTo-Json
} catch {
    Write-Host "Erro na conexao" -ForegroundColor Red
}

Write-Host "TESTE 3: STATUS DA REDE" -ForegroundColor Cyan
try {
    $status = Invoke-RestMethod -Uri "$TEST_URL/status" -Method GET -TimeoutSec 10
    Write-Host "Status: OK" -ForegroundColor Green
    $status | ConvertTo-Json
} catch {
    Write-Host "Erro no status" -ForegroundColor Red
}

Write-Host "TESTE 4: REGISTRAR EVENTO" -ForegroundColor Cyan
$evento = @{
    userId = "usuario_teste"
    action = "login"
    status = "success"
    details = "Teste via PowerShell"
}

try {
    $headers = @{ "Content-Type" = "application/json" }
    $body = $evento | ConvertTo-Json
    $result = Invoke-RestMethod -Uri "$TEST_URL/event" -Method POST -Body $body -Headers $headers -TimeoutSec 10
    Write-Host "Evento registrado: OK" -ForegroundColor Green
    $result | ConvertTo-Json
} catch {
    Write-Host "Erro ao registrar evento" -ForegroundColor Red
}

Write-Host "TESTE 5: ESTATISTICAS" -ForegroundColor Cyan
try {
    $stats = Invoke-RestMethod -Uri "$TEST_URL/statistics" -Method GET -TimeoutSec 10
    Write-Host "Estatisticas: OK" -ForegroundColor Green
    $stats | ConvertTo-Json
} catch {
    Write-Host "Erro nas estatisticas" -ForegroundColor Red
}

Write-Host "TESTES CONCLUIDOS!" -ForegroundColor Green 