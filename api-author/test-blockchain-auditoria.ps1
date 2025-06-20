# Script de Teste - Blockchain Auditoria
# Testa todas as funcionalidades de registro e auditoria em blockchain

$ErrorActionPreference = "Stop"

Write-Host "=== TESTE DE BLOCKCHAIN AUDITORIA ===" -ForegroundColor Green
Write-Host "Iniciando testes de registro de acessos em blockchain..."

# Configurações
$baseUrl = "http://localhost:8081/api/v1"
$email = "teste@blockchain.com"
$password = "TestPassword123!"
$ipAddress = "192.168.1.100"
$location = "São Paulo, SP, Brasil"
$userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Blockchain Test"

# Função para fazer requests HTTP
function Invoke-ApiRequest {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body | ConvertTo-Json -Depth 10
        }
        
        return Invoke-RestMethod @params
    }
    catch {
        Write-Host "Erro na requisição: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Função para exibir resultado de teste
function Show-TestResult {
    param(
        [string]$TestName,
        [bool]$Success,
        [object]$Data = $null
    )
    
    if ($Success) {
        Write-Host "✅ $TestName" -ForegroundColor Green
        if ($Data) {
            Write-Host "   Dados: $($Data | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ $TestName" -ForegroundColor Red
    }
}

# Teste 1: Registro de usuário
Write-Host "`n1. Testando registro de usuário..." -ForegroundColor Yellow

$registerData = @{
    name = "Usuário Teste Blockchain"
    email = $email
    password = $password
}

$registerResult = Invoke-ApiRequest -Uri "$baseUrl/auth/register" -Method POST -Body $registerData
Show-TestResult -TestName "Registro de usuário" -Success ($registerResult -ne $null)

# Teste 2: Login (gerará transação blockchain)
Write-Host "`n2. Testando login com registro blockchain..." -ForegroundColor Yellow

$loginData = @{
    email = $email
    password = $password
    ipAddress = $ipAddress
    location = $location
    userAgent = $userAgent
}

$loginResult = Invoke-ApiRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData
$token = $null

if ($loginResult -and $loginResult.token) {
    $token = $loginResult.token
    Show-TestResult -TestName "Login com registro blockchain" -Success $true -Data @{
        token = $token.Substring(0, 20) + "..."
        trustScore = $loginResult.trustScore
    }
} else {
    Show-TestResult -TestName "Login com registro blockchain" -Success $false
}

if (-not $token) {
    Write-Host "❌ Não foi possível obter token. Abortando testes de auditoria." -ForegroundColor Red
    exit 1
}

# Headers para requests autenticados
$authHeaders = @{ Authorization = "Bearer $token" }

# Aguardar um pouco para o registro ser processado
Write-Host "`nAguardando processamento da transação blockchain..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Teste 3: Buscar transações do usuário
Write-Host "`n3. Testando busca de transações por usuário..." -ForegroundColor Yellow

$userTransactions = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/usuario/1" -Headers $authHeaders
Show-TestResult -TestName "Busca de transações por usuário" -Success ($userTransactions -ne $null) -Data @{
    totalTransacoes = if($userTransactions) { $userTransactions.Count } else { 0 }
}

# Teste 4: Buscar transações por período (últimas 24 horas)
Write-Host "`n4. Testando busca de transações por período..." -ForegroundColor Yellow

$dataInicio = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
$dataFim = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")

$periodTransactions = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/periodo?inicio=$dataInicio&fim=$dataFim" -Headers $authHeaders
Show-TestResult -TestName "Busca de transações por período" -Success ($periodTransactions -ne $null) -Data @{
    totalTransacoes = if($periodTransactions) { $periodTransactions.Count } else { 0 }
}

# Teste 5: Buscar transações de alto risco
Write-Host "`n5. Testando busca de transações de alto risco..." -ForegroundColor Yellow

$highRiskTransactions = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/alto-risco?limiteRisco=0.5" -Headers $authHeaders
Show-TestResult -TestName "Busca de transações de alto risco" -Success ($highRiskTransactions -ne $null) -Data @{
    totalTransacoes = if($highRiskTransactions) { $highRiskTransactions.Count } else { 0 }
}

# Teste 6: Verificar integridade (se houver transações)
if ($userTransactions -and $userTransactions.Count -gt 0) {
    Write-Host "`n6. Testando verificação de integridade..." -ForegroundColor Yellow
    
    $hash = $userTransactions[0].hashTransacao
    if ($hash) {
        $integrityCheck = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/integridade/$hash" -Headers $authHeaders
        Show-TestResult -TestName "Verificação de integridade" -Success ($integrityCheck -ne $null) -Data @{
            hash = $hash
            integridadeOk = if($integrityCheck) { $integrityCheck.integridadeOk } else { $false }
        }
    }
}

# Teste 7: Verificar status de confirmação
if ($userTransactions -and $userTransactions.Count -gt 0) {
    Write-Host "`n7. Testando verificação de confirmação..." -ForegroundColor Yellow
    
    $hash = $userTransactions[0].hashTransacao
    if ($hash) {
        $confirmationStatus = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/confirmacao/$hash" -Headers $authHeaders
        Show-TestResult -TestName "Verificação de confirmação" -Success ($confirmationStatus -ne $null) -Data @{
            hash = $hash
            status = if($confirmationStatus) { $confirmationStatus.statusConfirmacao } else { "N/A" }
            verificado = if($confirmationStatus) { $confirmationStatus.verificado } else { $false }
        }
    }
}

# Teste 8: Gerar relatório de auditoria
Write-Host "`n8. Testando geração de relatório de auditoria..." -ForegroundColor Yellow

$auditReport = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/relatorio/usuario/1" -Headers $authHeaders
Show-TestResult -TestName "Geração de relatório de auditoria" -Success ($auditReport -ne $null) -Data @{
    usuarioId = if($auditReport) { $auditReport.usuarioId } else { 0 }
    totalTransacoes = if($auditReport) { $auditReport.totalTransacoes } else { 0 }
    mediaRisco = if($auditReport) { $auditReport.mediaRisco } else { 0 }
}

# Teste 9: Obter estatísticas gerais
Write-Host "`n9. Testando obtenção de estatísticas gerais..." -ForegroundColor Yellow

$generalStats = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/estatisticas" -Headers $authHeaders
Show-TestResult -TestName "Obtenção de estatísticas gerais" -Success ($generalStats -ne $null) -Data @{
    transacoesNaoVerificadas = if($generalStats) { $generalStats.transacoesNaoVerificadas } else { 0 }
    transacoesAltoRisco = if($generalStats) { $generalStats.transacoesAltoRisco } else { 0 }
    transacoesUltimas24h = if($generalStats) { $generalStats.transacoesUltimas24h } else { 0 }
}

# Teste 10: Verificar transações não confirmadas
Write-Host "`n10. Testando verificação de transações não confirmadas..." -ForegroundColor Yellow

$unconfirmedTransactions = Invoke-ApiRequest -Uri "$baseUrl/blockchain/auditoria/nao-confirmadas" -Headers $authHeaders
Show-TestResult -TestName "Verificação de transações não confirmadas" -Success ($unconfirmedTransactions -ne $null) -Data @{
    totalNaoConfirmadas = if($unconfirmedTransactions) { $unconfirmedTransactions.Count } else { 0 }
}

# Resumo dos testes
Write-Host "`n=== RESUMO DOS TESTES ===" -ForegroundColor Green
Write-Host "Testes de Blockchain Auditoria concluídos!"
Write-Host "Verifique os resultados acima para confirmar o funcionamento correto."

# Instruções finais
Write-Host "`n=== INSTRUÇÕES PARA VERIFICAÇÃO ===" -ForegroundColor Blue
Write-Host "1. Verifique se todas as transações foram registradas corretamente"
Write-Host "2. Confirme que os hashes de integridade estão corretos"
Write-Host "3. Valide que as confirmações estão sendo processadas"
Write-Host "4. Monitore os logs para erros ou warnings"
Write-Host "5. Acesse o Swagger UI em http://localhost:8081/api/v1/swagger-ui.html"

Write-Host "`n=== COMANDOS ÚTEIS ===" -ForegroundColor Cyan
Write-Host "Swagger: http://localhost:8081/api/v1/swagger-ui.html"
Write-Host "Health Check: $baseUrl/health"

Write-Host "`nTeste concluído!" -ForegroundColor Green
