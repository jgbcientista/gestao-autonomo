# Script de Teste do Serviço de Geolocalização
Write-Host "🌍 Testando Serviço de Geolocalização..." -ForegroundColor Green

$baseUrl = "http://localhost:8081/api/v1"

# Teste 1: Geolocalização por IP público
Write-Host "`n1. 📍 Testando geolocalização por IP público (Google DNS)..." -ForegroundColor Yellow
try {
    $geoGoogle = Invoke-RestMethod -Uri "$baseUrl/geo/ip/8.8.8.8" -Method GET
    Write-Host "✅ Resultado para 8.8.8.8:" -ForegroundColor Green
    Write-Host "   - País: $($geoGoogle.pais)" -ForegroundColor Cyan
    Write-Host "   - Cidade: $($geoGoogle.cidade)" -ForegroundColor Cyan
    Write-Host "   - Coordenadas: $($geoGoogle.latitude), $($geoGoogle.longitude)" -ForegroundColor Cyan
    Write-Host "   - Fonte: $($geoGoogle.fonte)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 2: IP privado (deve retornar padrão)
Write-Host "`n2. 🏠 Testando IP privado (deve retornar localização padrão)..." -ForegroundColor Yellow
try {
    $geoPrivado = Invoke-RestMethod -Uri "$baseUrl/geo/ip/192.168.1.1" -Method GET
    Write-Host "✅ Resultado para 192.168.1.1:" -ForegroundColor Green
    Write-Host "   - País: $($geoPrivado.pais)" -ForegroundColor Cyan
    Write-Host "   - Cidade: $($geoPrivado.cidade)" -ForegroundColor Cyan
    Write-Host "   - Fonte: $($geoPrivado.fonte)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 3: Geocoding de endereço
Write-Host "`n3. 🗺️ Testando geocoding de endereço..." -ForegroundColor Yellow
try {
    $geoEndereco = Invoke-RestMethod -Uri "$baseUrl/geo/endereco?endereco=São Paulo, Brasil" -Method GET
    Write-Host "✅ Coordenadas para 'São Paulo, Brasil':" -ForegroundColor Green
    Write-Host "   - Latitude: $($geoEndereco.latitude)" -ForegroundColor Cyan
    Write-Host "   - Longitude: $($geoEndereco.longitude)" -ForegroundColor Cyan
    Write-Host "   - Endereço completo: $($geoEndereco.endereco)" -ForegroundColor Cyan
    Write-Host "   - Fonte: $($geoEndereco.fonte)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 4: Cálculo de distância entre coordenadas
Write-Host "`n4. 📏 Testando cálculo de distância entre coordenadas..." -ForegroundColor Yellow
try {
    $distancia = Invoke-RestMethod -Uri "$baseUrl/geo/distancia?lat1=-23.5505&lon1=-46.6333&lat2=-22.9068&lon2=-43.1729" -Method GET
    Write-Host "✅ Distância São Paulo → Rio de Janeiro:" -ForegroundColor Green
    Write-Host "   - Distância: $($distancia.distanciaKm) km" -ForegroundColor Cyan
    Write-Host "   - Distância: $($distancia.distanciaMilhas) milhas" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 5: Distância entre endereços
Write-Host "`n5. 🚗 Testando distância entre endereços..." -ForegroundColor Yellow
try {
    $distEnderecos = Invoke-RestMethod -Uri "$baseUrl/geo/distancia-enderecos?endereco1=São Paulo, Brasil&endereco2=Rio de Janeiro, Brasil" -Method GET
    Write-Host "✅ Distância calculada:" -ForegroundColor Green
    Write-Host "   - De: $($distEnderecos.endereco1.localizacao)" -ForegroundColor Cyan
    Write-Host "   - Para: $($distEnderecos.endereco2.localizacao)" -ForegroundColor Cyan
    Write-Host "   - Distância: $($distEnderecos.distanciaKm) km" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 6: Verificação de países de risco
Write-Host "`n6. ⚠️ Testando verificação de países de risco..." -ForegroundColor Yellow

$paises = @("BR", "CN", "RU", "US", "NG")
foreach ($pais in $paises) {
    try {
        $risco = Invoke-RestMethod -Uri "$baseUrl/geo/pais-risco/$pais" -Method GET
        $cor = if ($risco.altoRisco) { "Red" } else { "Green" }
        Write-Host "   - $($risco.codigoPais): $($risco.nivelRisco)" -ForegroundColor $cor
    } catch {
        Write-Host "   - $pais: Erro na consulta" -ForegroundColor Red
    }
}

# Teste 7: Teste completo
Write-Host "`n7. 🧪 Executando teste completo..." -ForegroundColor Yellow
try {
    $testeCompleto = Invoke-RestMethod -Uri "$baseUrl/geo/teste-completo" -Method GET
    Write-Host "✅ Status: $($testeCompleto.status)" -ForegroundColor Green
    Write-Host "   - Teste IP público válido: $($testeCompleto.teste1_ip_publico.valido)" -ForegroundColor Cyan
    Write-Host "   - Teste IP privado é padrão: $($testeCompleto.teste2_ip_privado.ehPadrao)" -ForegroundColor Cyan
    Write-Host "   - Teste geocoding válido: $($testeCompleto.teste3_geocoding.valido)" -ForegroundColor Cyan
    Write-Host "   - Distância SP-RJ: $($testeCompleto.teste4_distancia.distanciaKm) km" -ForegroundColor Cyan
    Write-Host "   - Brasil alto risco: $($testeCompleto.teste5_pais_risco.brasil_BR)" -ForegroundColor Cyan
    Write-Host "   - China alto risco: $($testeCompleto.teste5_pais_risco.china_CN)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Testes de geolocalização concluídos!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host "💡 Dicas:" -ForegroundColor Blue
Write-Host "- Para maior precisão, configure OPENCAGE_API_KEY" -ForegroundColor Blue  
Write-Host "- IPs privados sempre retornam localização padrão" -ForegroundColor Blue
Write-Host "- Distâncias são calculadas usando fórmula de Haversine" -ForegroundColor Blue 