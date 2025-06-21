Write-Host "TESTE ERRO 500 - REGISTRO" -ForegroundColor Red

# Definir dados
$body = '{"name":"Test User 500","email":"test500@example.com","password":"123456"}'
$uri = "http://localhost:8081/api/v1/autenticacao/registrar"

Write-Host "Testando: $uri" -ForegroundColor Yellow
Write-Host "Body: $body" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $uri -Method POST -ContentType "application/json" -Body $body
    Write-Host "SUCESSO - Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Resposta: $($response.Content)" -ForegroundColor White
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "ERRO $status - $($_.Exception.Message)" -ForegroundColor Red
    
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Detalhes: $errorBody" -ForegroundColor Red
        $reader.Close()
    } catch {
        Write-Host "Nao foi possivel ler detalhes do erro" -ForegroundColor Yellow
    }
} 