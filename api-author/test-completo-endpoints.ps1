#!/usr/bin/env pwsh

# TESTE COMPLETO DE TODOS OS ENDPOINTS DO SISTEMA
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "        TESTE COMPLETO - TODOS OS ENDPOINTS        " -ForegroundColor Green  
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "Início: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

$baseUrl = "http://localhost:8080"
$testCount = 0
$successCount = 0
$errorCount = 0

function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Method = "GET", 
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [string]$TestName,
        [string]$Category = "Geral"
    )
    
    $global:testCount++
    
    Write-Host "[$global:testCount] $Category -> $TestName" -ForegroundColor Cyan
    Write-Host "    $Method $Url" -ForegroundColor DarkGray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
            TimeoutSec = 20
        }
        
        if ($Body) { 
            $params.Body = $Body 
            Write-Host "    Body: $($Body.Substring(0, [Math]::Min(80, $Body.Length)))..." -ForegroundColor DarkGray
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "    ✅ SUCESSO" -ForegroundColor Green
        $global:successCount++
        return $response
    }
    catch {
        Write-Host "    ❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
        $global:errorCount++
        return $null
    }
}

# ==========================================
# FASE 1: HEALTH CHECK
# ==========================================
Write-Host "`n🏥 FASE 1: HEALTH CHECK" -ForegroundColor Yellow

Test-Endpoint -Url "$baseUrl/health" -TestName "Health Check do Sistema" -Category "Health"

# ==========================================
# FASE 2: AUTENTICAÇÃO E AUTORIZAÇÃO
# ==========================================
Write-Host "`n🔐 FASE 2: AUTENTICAÇÃO E AUTORIZAÇÃO" -ForegroundColor Yellow

# Registrar usuário admin
$adminRegisterData = @{
    nome = "Administrador Teste"
    email = "admin.teste@sistema.com"
    senha = "AdminTeste@123"
    roles = @("ADMIN", "AUDITOR", "USER")
} | ConvertTo-Json

$adminReg = Test-Endpoint -Url "$baseUrl/auth/register" -Method "POST" -Body $adminRegisterData -TestName "Registro Usuário Admin" -Category "Auth"

# Login admin
$adminLoginData = @{
    email = "admin.teste@sistema.com"
    password = "AdminTeste@123"
} | ConvertTo-Json

$adminAuth = Test-Endpoint -Url "$baseUrl/auth/login" -Method "POST" -Body $adminLoginData -TestName "Login Admin" -Category "Auth"
$adminToken = if ($adminAuth) { $adminAuth.token } else { $null }

if ($adminToken) {
    $adminHeaders = @{ "Authorization" = "Bearer $adminToken" }
    Write-Host "    Token Admin obtido com sucesso" -ForegroundColor Green
} else {
    Write-Host "    ⚠️ Falha ao obter token admin" -ForegroundColor Yellow
    $adminHeaders = @{}
}

# Registrar usuário comum
$userRegisterData = @{
    nome = "Usuario Teste Comum"
    email = "user.teste@sistema.com"
    senha = "UserTeste@123"
} | ConvertTo-Json

$userReg = Test-Endpoint -Url "$baseUrl/auth/register" -Method "POST" -Body $userRegisterData -TestName "Registro Usuário Comum" -Category "Auth"

# Login usuário comum
$userLoginData = @{
    email = "user.teste@sistema.com"
    password = "UserTeste@123"
} | ConvertTo-Json

$userAuth = Test-Endpoint -Url "$baseUrl/auth/login" -Method "POST" -Body $userLoginData -TestName "Login Usuário Comum" -Category "Auth"
$userToken = if ($userAuth) { $userAuth.token } else { $null }

if ($userToken) {
    $userHeaders = @{ "Authorization" = "Bearer $userToken" }
    Write-Host "    Token Usuário obtido com sucesso" -ForegroundColor Green
} else {
    Write-Host "    ⚠️ Falha ao obter token usuário" -ForegroundColor Yellow
    $userHeaders = @{}
}

# ==========================================
# FASE 3: ANÁLISE DE CONTEXTO
# ==========================================
Write-Host "`n🧠 FASE 3: ANÁLISE DE CONTEXTO" -ForegroundColor Yellow

$contextData = @{
    ipAddress = "192.168.1.150"
    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    location = "São Paulo, SP, Brasil"
    deviceFingerprint = "test_device_fingerprint_12345"
    sessionData = @{
        sessionId = "session_test_$(Get-Random)"
        timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    }
} | ConvertTo-Json -Depth 3

Test-Endpoint -Url "$baseUrl/auth/context/analyze" -Method "POST" -Body $contextData -Headers $userHeaders -TestName "Análise de Contexto Usuário" -Category "Context"

# ==========================================
# FASE 4: SCORE DE CONFIANÇA
# ==========================================
Write-Host "`n📊 FASE 4: SCORE DE CONFIANÇA" -ForegroundColor Yellow

$trustScoreData = @{
    usuarioId = 1
    enderecoIp = "192.168.1.150"
    localizacao = "São Paulo, SP"
    dispositivoConfiavel = $true
    horarioHabitual = $true
    redeConfiavel = $true
} | ConvertTo-Json

Test-Endpoint -Url "$baseUrl/api/v1/score-confianca/calcular" -Method "POST" -Body $trustScoreData -Headers $adminHeaders -TestName "Calcular Score de Confiança" -Category "TrustScore"

Test-Endpoint -Url "$baseUrl/api/v1/score-confianca/historico/1" -Headers $adminHeaders -TestName "Histórico Score Usuário 1" -Category "TrustScore"

Test-Endpoint -Url "$baseUrl/api/v1/score-confianca/estatisticas" -Headers $adminHeaders -TestName "Estatísticas Score Confiança" -Category "TrustScore"

# ==========================================
# FASE 5: IA COMPORTAMENTAL
# ==========================================
Write-Host "`n🤖 FASE 5: IA COMPORTAMENTAL" -ForegroundColor Yellow

# Treinar modelo IA
$iaTrainingData = @{
    usuarioId = 1
    dadosComportamentais = @{
        horariosAcesso = @("08:30", "12:00", "14:30", "18:00")
        localizacoesFrequentes = @("São Paulo, SP", "Campinas, SP")
        dispositivosUtilizados = @("Windows Desktop", "Android Mobile")
        padraoNavegacao = "normal"
        frequenciaLogin = "diaria"
    }
} | ConvertTo-Json -Depth 3

Test-Endpoint -Url "$baseUrl/api/v1/ia-comportamental/treinar" -Method "POST" -Body $iaTrainingData -Headers $adminHeaders -TestName "Treinar Modelo IA" -Category "AI"

# Analisar comportamento
$iaAnalysisData = @{
    usuarioId = 1
    contextoAtual = @{
        horarioAcesso = (Get-Date).ToString("HH:mm")
        localizacao = "São Paulo, SP"
        dispositivo = "Windows Desktop"
        enderecoIp = "192.168.1.150"
        comportamento = "normal"
    }
} | ConvertTo-Json -Depth 3

Test-Endpoint -Url "$baseUrl/api/v1/ia-comportamental/analisar" -Method "POST" -Body $iaAnalysisData -Headers $userHeaders -TestName "Análise Comportamental IA" -Category "AI"

# Detectar anomalias
$anomalyData = @{
    usuarioId = 1
    dadosAtividade = @{
        horarioIncomum = $false
        localizacaoNova = $false
        dispositivoDesconhecido = $false
        volumeTransacoes = "normal"
        padraoIncomum = $false
    }
} | ConvertTo-Json -Depth 3

Test-Endpoint -Url "$baseUrl/api/v1/ia-comportamental/detectar-anomalias" -Method "POST" -Body $anomalyData -Headers $adminHeaders -TestName "Detecção de Anomalias" -Category "AI"

Test-Endpoint -Url "$baseUrl/api/v1/ia-comportamental/estatisticas" -Headers $adminHeaders -TestName "Estatísticas IA Comportamental" -Category "AI"

# ==========================================
# FASE 6: GEOLOCALIZAÇÃO
# ==========================================
Write-Host "`n🌍 FASE 6: GEOLOCALIZAÇÃO" -ForegroundColor Yellow

$geoValidationData = @{
    enderecoIp = "192.168.1.150"
    localizacaoDeclarada = "São Paulo, SP, Brasil"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json

Test-Endpoint -Url "$baseUrl/api/v1/geolocalizacao/validar" -Method "POST" -Body $geoValidationData -Headers $userHeaders -TestName "Validar Geolocalização" -Category "Geo"

Test-Endpoint -Url "$baseUrl/api/v1/geolocalizacao/ip/192.168.1.150" -Headers $userHeaders -TestName "Obter Localização por IP" -Category "Geo"

Test-Endpoint -Url "$baseUrl/api/v1/geolocalizacao/historico/1" -Headers $adminHeaders -TestName "Histórico Geolocalizações" -Category "Geo"

# ==========================================
# FASE 7: BLOCKCHAIN AUDITORIA
# ==========================================
Write-Host "`n⛓️ FASE 7: BLOCKCHAIN AUDITORIA" -ForegroundColor Yellow

# Aguardar processamento das transações blockchain
Write-Host "    Aguardando processamento das transações blockchain..." -ForegroundColor Gray
Start-Sleep -Seconds 3

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/estatisticas" -Headers $adminHeaders -TestName "Estatísticas Blockchain" -Category "Blockchain"

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/usuario/1" -Headers $adminHeaders -TestName "Transações por Usuário" -Category "Blockchain"

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/alto-risco?limiteRisco=0.7" -Headers $adminHeaders -TestName "Transações Alto Risco" -Category "Blockchain"

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/nao-confirmadas" -Headers $adminHeaders -TestName "Transações Não Confirmadas" -Category "Blockchain"

# Buscar transações por período (últimas 24h)
$startDate = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
$endDate = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
Test-Endpoint -Url "$baseUrl/blockchain/auditoria/periodo?inicio=$startDate&fim=$endDate" -Headers $adminHeaders -TestName "Transações por Período" -Category "Blockchain"

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/relatorio/usuario/1" -Headers $adminHeaders -TestName "Relatório Auditoria Usuário" -Category "Blockchain"

# ==========================================
# FASE 8: HYPERLEDGER FABRIC
# ==========================================
Write-Host "`n🔗 FASE 8: HYPERLEDGER FABRIC" -ForegroundColor Yellow

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/hyperledger/info" -Headers $adminHeaders -TestName "Informações Rede Hyperledger" -Category "Hyperledger"

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/hyperledger/test-connectivity" -Method "POST" -Headers $adminHeaders -TestName "Teste Conectividade Hyperledger" -Category "Hyperledger"

# ==========================================
# FASE 9: SIMULAÇÃO DE MÚLTIPLOS ACESSOS
# ==========================================
Write-Host "`n🔄 FASE 9: SIMULAÇÃO DE MÚLTIPLOS ACESSOS" -ForegroundColor Yellow

for ($i = 1; $i -le 2; $i++) {
    Write-Host "    Simulação de acesso $i/2..." -ForegroundColor Gray
    
    # Login adicional para gerar mais transações
    $multiLogin = Test-Endpoint -Url "$baseUrl/auth/login" -Method "POST" -Body $userLoginData -TestName "Login Múltiplo $i" -Category "Simulation"
    
    Start-Sleep -Seconds 1
}

# ==========================================
# FASE 10: VERIFICAÇÃO FINAL
# ==========================================
Write-Host "`n🔍 FASE 10: VERIFICAÇÃO FINAL" -ForegroundColor Yellow

# Aguardar processamento final
Start-Sleep -Seconds 2

Test-Endpoint -Url "$baseUrl/blockchain/auditoria/estatisticas" -Headers $adminHeaders -TestName "Estatísticas Finais Blockchain" -Category "Final"

Test-Endpoint -Url "$baseUrl/api/v1/ia-comportamental/estatisticas" -Headers $adminHeaders -TestName "Estatísticas Finais IA" -Category "Final"

Test-Endpoint -Url "$baseUrl/health" -TestName "Health Check Final" -Category "Final"

# ==========================================
# RELATÓRIO FINAL
# ==========================================
Write-Host "`n" + "="*60 -ForegroundColor Green
Write-Host "                    RELATÓRIO FINAL                    " -ForegroundColor Green
Write-Host "="*60 -ForegroundColor Green

Write-Host "`n📊 ESTATÍSTICAS GERAIS:" -ForegroundColor White
Write-Host "  Total de testes executados: $global:testCount" -ForegroundColor Cyan
Write-Host "  Testes bem-sucedidos: $global:successCount" -ForegroundColor Green
Write-Host "  Testes com erro: $global:errorCount" -ForegroundColor Red

if ($global:testCount -gt 0) {
    $successRate = [Math]::Round(($global:successCount / $global:testCount) * 100, 2)
    Write-Host "  Taxa de sucesso: $successRate%" -ForegroundColor $(if ($successRate -ge 80) {"Green"} elseif ($successRate -ge 60) {"Yellow"} else {"Red"})
} else {
    Write-Host "  Taxa de sucesso: N/A" -ForegroundColor Gray
}

Write-Host "`n🎯 ENDPOINTS TESTADOS:" -ForegroundColor White
Write-Host "  ✓ Health Check" -ForegroundColor Gray
Write-Host "  ✓ Autenticação e Autorização" -ForegroundColor Gray
Write-Host "  ✓ Análise de Contexto" -ForegroundColor Gray
Write-Host "  ✓ Score de Confiança" -ForegroundColor Gray
Write-Host "  ✓ IA Comportamental" -ForegroundColor Gray
Write-Host "  ✓ Geolocalização" -ForegroundColor Gray
Write-Host "  ✓ Blockchain Auditoria" -ForegroundColor Gray
Write-Host "  ✓ Hyperledger Fabric" -ForegroundColor Gray
Write-Host "  ✓ Simulação de Acessos" -ForegroundColor Gray

Write-Host "`n🏆 AVALIAÇÃO GERAL:" -ForegroundColor White
if ($successRate -ge 90) {
    Write-Host "  🥇 EXCELENTE! Sistema funcionando perfeitamente." -ForegroundColor Green
} elseif ($successRate -ge 75) {
    Write-Host "  🥈 BOM! Sistema funcionando bem com pequenas falhas." -ForegroundColor Yellow  
} elseif ($successRate -ge 50) {
    Write-Host "  🥉 ACEITÁVEL! Sistema funcional mas precisa de melhorias." -ForegroundColor Orange
} else {
    Write-Host "  ❌ CRÍTICO! Sistema com muitas falhas. Verificar configurações." -ForegroundColor Red
}

Write-Host "`n📝 OBSERVAÇÕES:" -ForegroundColor White
Write-Host "  • Testes executados em ambiente de desenvolvimento" -ForegroundColor DarkGray
Write-Host "  • Algumas falhas podem ser esperadas (ex: Hyperledger em modo simulação)" -ForegroundColor DarkGray
Write-Host "  • Para produção, configurar todas as dependências externas" -ForegroundColor DarkGray
Write-Host "  • Transações blockchain podem demorar para aparecer" -ForegroundColor DarkGray

Write-Host "`n" + "="*60 -ForegroundColor Green
Write-Host "Teste finalizado em: $(Get-Date)" -ForegroundColor Gray
Write-Host "="*60 -ForegroundColor Green 