# Script Simples de Teste do Módulo de IA
Write-Host "Testando Módulo de IA..." -ForegroundColor Green

$baseUrl = "http://localhost:8081/api/v1"

# Teste 1: Health Check
Write-Host "1. Verificando servidor..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "Servidor OK" -ForegroundColor Green
} catch {
    Write-Host "Servidor não está rodando!" -ForegroundColor Red
    exit
}

# Teste 2: Treinar IA
Write-Host "2. Treinando modelos de IA..." -ForegroundColor Yellow
try {
    $treino = Invoke-RestMethod -Uri "$baseUrl/test/ia/treinar" -Method POST
    Write-Host "Treinamento concluído" -ForegroundColor Green
} catch {
    Write-Host "Erro no treinamento" -ForegroundColor Red
}

# Teste 3: Testar Score
Write-Host "3. Testando score..." -ForegroundColor Yellow
try {
    $score = Invoke-RestMethod -Uri "$baseUrl/test/ia/testar-score" -Method GET
    Write-Host "Score: $($score.scoreFinal) - $($score.classificacao)" -ForegroundColor Green
} catch {
    Write-Host "Erro no teste de score" -ForegroundColor Red
}

# Teste 4: Análise Comportamental
Write-Host "4. Análise comportamental..." -ForegroundColor Yellow
try {
    $analise = Invoke-RestMethod -Uri "$baseUrl/test/ia/simular-analise/1" -Method GET
    Write-Host "Análise OK - Score: $($analise.scoreEnsemble)" -ForegroundColor Green
} catch {
    Write-Host "Erro na análise" -ForegroundColor Red
}

Write-Host "Testes concluídos!" -ForegroundColor Green 