Write-Host "=== TESTE ENDPOINT REGISTRO - URL CORRETA ===" -ForegroundColor Green

# URL CORRETA (sem duplicação)
$url = "http://localhost:8081/api/v1/autenticacao/registrar"
$body = '{"name":"Usuario Teste Final","email":"usuario.final@test.com","password":"123456"}'

Write-Host "URL: $url" -ForegroundColor Yellow
Write-Host "Body: $body" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url -Method POST -ContentType "application/json" -Body $body
    Write-Host ""
    Write-Host "✅ SUCESSO!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Resposta:" -ForegroundColor White
    Write-Host $response.Content -ForegroundColor White
} catch {
    Write-Host ""
    Write-Host "❌ ERRO!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Mensagem: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Detalhes do erro:" -ForegroundColor Red
            Write-Host $errorBody -ForegroundColor Red
            $reader.Close()
        } catch {
            Write-Host "Não foi possível ler detalhes do erro" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "=== FIM DO TESTE ===" -ForegroundColor Green 