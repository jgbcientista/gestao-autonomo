Write-Host "Registrando usuário João Guedes" -ForegroundColor Cyan

$userData = @{
    name = "João Guedes"
    email = "joaoguedes@gmail.com"
    password = "1234567890"
} | ConvertTo-Json

Write-Host "Dados do registro: $userData"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json"
    Write-Host "USUÁRIO REGISTRADO COM SUCESSO!" -ForegroundColor Green
    Write-Host "Token: $($response.token.Substring(0,30))..."
    Write-Host "Nome: $($response.name)" 
    Write-Host "Email: $($response.email)"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 409) {
        Write-Host "Usuário já existe - OK para continuar" -ForegroundColor Yellow
    } else {
        Write-Host "ERRO no registro:" -ForegroundColor Red
        Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
        Write-Host "Mensagem: $($_.Exception.Message)"
    }
} 