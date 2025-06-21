$body = '{"name":"João Guedes de Brito","email":"joao@gmail.com","password":"123456"}'

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -ContentType "application/json" -Body $body
    Write-Host "✅ Sucesso! Status: $($response.StatusCode)"
    Write-Host "Resposta: $($response.Content)"
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)"
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode"
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorContent = $reader.ReadToEnd()
            Write-Host "Conteúdo do erro: $errorContent"
        } catch {
            Write-Host "Não foi possível ler o conteúdo do erro"
        }
    }
} 