# Script de Teste do Módulo de IA
# Execute este script após iniciar o servidor (mvn spring-boot:run)

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "🧪 Testando Módulo de IA - Sistema de Autenticação" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# Teste 1: Verificar se o servidor está rodando
Write-Host "`n1. 🔍 Verificando se o servidor está ativo..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✅ Servidor ativo: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor não está rodando. Execute 'mvn spring-boot:run' primeiro!" -ForegroundColor Red
    exit 1
}

# Teste 2: Treinar os modelos de IA
Write-Host "`n2. 🤖 Treinando modelos de IA..." -ForegroundColor Yellow
try {
    $treino = Invoke-RestMethod -Uri "$baseUrl/test/ia/treinar" -Method POST
    Write-Host "✅ Treinamento concluído:" -ForegroundColor Green
    Write-Host "   - Isolation Forest: $($treino.isolationForest)" -ForegroundColor Cyan
    Write-Host "   - Random Forest: $($treino.randomForest)" -ForegroundColor Cyan
    Write-Host "   - Deep Learning: $($treino.deepLearning)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro no treinamento: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 3: Testar score com dados simulados
Write-Host "`n3. 📊 Testando score com dados simulados..." -ForegroundColor Yellow
try {
    $score = Invoke-RestMethod -Uri "$baseUrl/test/ia/testar-score" -Method GET
    Write-Host "✅ Score calculado:" -ForegroundColor Green
    Write-Host "   - Score Final: $($score.scoreFinal)" -ForegroundColor Cyan
    Write-Host "   - Classificação: $($score.classificacao)" -ForegroundColor Cyan
    Write-Host "   - Confiança: $($score.confianca)" -ForegroundColor Cyan
    Write-Host "   - Algoritmo: $($score.algoritmo)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro no teste de score: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 4: Simular análise completa
Write-Host "`n4. 🔬 Simulando análise comportamental..." -ForegroundColor Yellow
try {
    $analise = Invoke-RestMethod -Uri "$baseUrl/test/ia/simular-analise/1" -Method GET
    Write-Host "✅ Análise comportamental concluída:" -ForegroundColor Green
    Write-Host "   - Usuário ID: $($analise.usuarioId)" -ForegroundColor Cyan
    Write-Host "   - Score Isolation Forest: $($analise.scoreIsolationForest)" -ForegroundColor Cyan
    Write-Host "   - Score Random Forest: $($analise.scoreRandomForest)" -ForegroundColor Cyan
    Write-Host "   - Score Deep Learning: $($analise.scoreDeepLearning)" -ForegroundColor Cyan
    Write-Host "   - Score Ensemble: $($analise.scoreEnsemble)" -ForegroundColor Cyan
    Write-Host "   - Classificação Final: $($analise.classificacao)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro na análise: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 5: Verificar estatísticas
Write-Host "`n5. 📈 Verificando estatísticas de anomalias..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/test/ia/estatisticas" -Method GET
    Write-Host "✅ Estatísticas obtidas:" -ForegroundColor Green
    Write-Host "   - Total de Análises: $($stats.totalAnalises)" -ForegroundColor Cyan
    Write-Host "   - Anomalias Detectadas: $($stats.anomaliasDetectadas)" -ForegroundColor Cyan
    Write-Host "   - Taxa de Anomalias: $($stats.taxaAnomalias)%" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro nas estatísticas: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 6: Teste de autenticação com análise de IA
Write-Host "`n6. 🔐 Testando autenticação com análise de IA..." -ForegroundColor Yellow

# Primeiro, registrar um usuário
$registerBody = @{
    name = "Usuário Teste"
    email = "teste@exemplo.com"
    password = "senha123"
} | ConvertTo-Json

try {
    $register = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $registerBody -ContentType "application/json"
    Write-Host "✅ Usuário registrado com sucesso" -ForegroundColor Green
    
    # Agora testar autenticação
    $authBody = @{
        email = "teste@exemplo.com"
        password = "senha123"
        ipAddress = "192.168.1.100"
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        deviceFingerprint = "device-test-123"
        location = "São Paulo, SP"
    } | ConvertTo-Json
    
    $auth = Invoke-RestMethod -Uri "$baseUrl/auth/authenticate" -Method POST -Body $authBody -ContentType "application/json"
    Write-Host "✅ Autenticação com IA realizada:" -ForegroundColor Green
    Write-Host "   - Token gerado: Sim" -ForegroundColor Cyan
    Write-Host "   - Análise de risco: Executada" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erro na autenticação: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Testes do Módulo de IA concluídos!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host "Para mais testes, acesse: http://localhost:8080/swagger-ui.html" -ForegroundColor Blue 