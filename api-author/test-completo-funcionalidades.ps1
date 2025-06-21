Write-Host "🔍 TESTE COMPLETO DE FUNCIONALIDADES - SISTEMA AUTH" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan

# Função auxiliar para testar endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    Write-Host "`n🧪 Testando: $Name" -ForegroundColor Yellow
    Write-Host "   URL: $Uri" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
        }
        
        if ($Body) {
            $params['Body'] = $Body
            $params['ContentType'] = 'application/json'
        }
        
        $response = Invoke-WebRequest @params
        Write-Host "   ✅ Status: $($response.StatusCode)" -ForegroundColor Green
        
        if ($response.Content) {
            $content = $response.Content
            if ($content.Length -gt 200) {
                $content = $content.Substring(0, 200) + "..."
            }
            Write-Host "   📄 Resposta: $content" -ForegroundColor White
        }
        
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            Content = $response.Content
        }
    }
    catch {
        Write-Host "   ❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "   📊 Status Code: $statusCode" -ForegroundColor Red
        }
        
        return @{
            Success = $false
            Error = $_.Exception.Message
        }
    }
}

# 1. TESTE DE STATUS DOS SERVIÇOS
Write-Host "`n🚀 1. VERIFICANDO STATUS DOS SERVIÇOS" -ForegroundColor Magenta

# Verificar portas
Write-Host "`n🔍 Verificando portas..." -ForegroundColor Yellow
$ports = netstat -ano | Select-String ":4200|:8081"
if ($ports) {
    Write-Host "   ✅ Portas encontradas:" -ForegroundColor Green
    $ports | ForEach-Object { Write-Host "     $($_.Line)" -ForegroundColor White }
} else {
    Write-Host "   ⚠️ Nenhuma porta encontrada" -ForegroundColor Yellow
}

# Teste de conectividade
$backendStatus = Test-Endpoint -Name "Backend Status" -Method "GET" -Uri "http://localhost:8081/api/v1/autenticacao/status"

# 2. TESTES DE REGISTRO
Write-Host "`n📝 2. TESTANDO REGISTRO DE USUÁRIOS" -ForegroundColor Magenta

# Teste 1: Registro normal
$registerBody1 = '{"name":"João Test","email":"joao.test@email.com","password":"123456"}'
$registerTest1 = Test-Endpoint -Name "Registro Normal" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Body $registerBody1

# Teste 2: Registro com caracteres especiais
$registerBody2 = '{"name":"Maria José da Silva","email":"maria.jose@email.com","password":"senha123"}'
$registerTest2 = Test-Endpoint -Name "Registro com Caracteres Especiais" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Body $registerBody2

# Teste 3: Registro com email duplicado (deve falhar)
$registerTest3 = Test-Endpoint -Name "Registro Email Duplicado" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Body $registerBody1

# 3. TESTES DE LOGIN
Write-Host "`n🔐 3. TESTANDO LOGIN DE USUÁRIOS" -ForegroundColor Magenta

# Teste 1: Login válido
$loginBody1 = '{"email":"joao.test@email.com","password":"123456","location":"São Paulo, BR"}'
$loginTest1 = Test-Endpoint -Name "Login Válido" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Body $loginBody1

# Extrair token se login foi bem-sucedido
$token = $null
if ($loginTest1.Success -and $loginTest1.Content) {
    try {
        $loginResponse = $loginTest1.Content | ConvertFrom-Json
        $token = $loginResponse.token
        Write-Host "   🎫 Token extraído: $($token.Substring(0, 50))..." -ForegroundColor Green
    }
    catch {
        Write-Host "   ⚠️ Não foi possível extrair token" -ForegroundColor Yellow
    }
}

# Teste 2: Login inválido
$loginBody2 = '{"email":"joao.test@email.com","password":"senhaerrada","location":"São Paulo, BR"}'
$loginTest2 = Test-Endpoint -Name "Login Inválido" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Body $loginBody2

# 4. TESTES DE VALIDAÇÃO DE TOKEN
Write-Host "`n🎫 4. TESTANDO VALIDAÇÃO DE TOKEN" -ForegroundColor Magenta

if ($token) {
    # Teste 1: Token válido
    $validateTest1 = Test-Endpoint -Name "Validar Token Válido" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/validar-token?token=$token"
    
    # Teste 2: Token inválido
    $validateTest2 = Test-Endpoint -Name "Validar Token Inválido" -Method "POST" -Uri "http://localhost:8081/api/v1/autenticacao/validar-token?token=invalid-token"
} else {
    Write-Host "   ⚠️ Token não disponível para testes" -ForegroundColor Yellow
}

# 5. TESTE DE CONECTIVIDADE FRONTEND
Write-Host "`n🌐 5. TESTANDO FRONTEND" -ForegroundColor Magenta

try {
    $frontendTest = Invoke-WebRequest -Uri "http://localhost:4200" -Method GET -TimeoutSec 10
    Write-Host "   ✅ Frontend Status: $($frontendTest.StatusCode)" -ForegroundColor Green
    Write-Host "   📄 Título: $($frontendTest.ParsedHtml.title)" -ForegroundColor White
}
catch {
    Write-Host "   ❌ Frontend não acessível: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. RESUMO DOS TESTES
Write-Host "`n📊 6. RESUMO DOS TESTES" -ForegroundColor Magenta
Write-Host "=" * 60 -ForegroundColor Cyan

$tests = @(
    @{ Name = "Backend Status"; Result = $backendStatus.Success },
    @{ Name = "Registro Normal"; Result = $registerTest1.Success },
    @{ Name = "Registro Caracteres Especiais"; Result = $registerTest2.Success },
    @{ Name = "Login Válido"; Result = $loginTest1.Success },
    @{ Name = "Login Inválido"; Result = !$loginTest2.Success }
)

$successCount = 0
$totalTests = $tests.Count

foreach ($test in $tests) {
    $status = if ($test.Result) { "✅ PASSOU" } else { "❌ FALHOU" }
    $color = if ($test.Result) { "Green" } else { "Red" }
    Write-Host "   $($test.Name): $status" -ForegroundColor $color
    if ($test.Result) { $successCount++ }
}

Write-Host "`n📈 RESULTADO FINAL: $successCount/$totalTests testes passaram" -ForegroundColor Cyan

if ($successCount -eq $totalTests) {
    Write-Host "🎉 TODOS OS TESTES PASSARAM! Sistema funcionando perfeitamente!" -ForegroundColor Green
} elseif ($successCount -gt ($totalTests / 2)) {
    Write-Host "⚠️ Maioria dos testes passou, mas há alguns problemas." -ForegroundColor Yellow
} else {
    Write-Host "❌ Muitos testes falharam. Sistema precisa de atenção." -ForegroundColor Red
}

Write-Host "`n🏁 Teste concluído!" -ForegroundColor Cyan 