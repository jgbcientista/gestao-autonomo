Write-Host "DEMONSTRACAO DOS DADOS DA BLOCKCHAIN" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

Write-Host "Este script mostra como os dados da blockchain sao testados." -ForegroundColor Yellow
Write-Host "Aplicacao roda na porta 8081" -ForegroundColor Yellow

Write-Host "TESTE 1: HEALTH CHECK" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/actuator/health" -ForegroundColor Gray
Write-Host "Resposta esperada:" -ForegroundColor Yellow
Write-Host '{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "database": "PostgreSQL" },
    "blockchain": { "status": "UP", "mode": "REAL", "network": "hyperledger-fabric" }
  }
}' -ForegroundColor White

Write-Host "TESTE 2: CONEXAO BLOCKCHAIN" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/blockchain/test/connection" -ForegroundColor Gray
Write-Host "Resposta esperada:" -ForegroundColor Yellow
Write-Host '{
  "success": true,
  "connected": true,
  "mode": "REAL",
  "message": "Conectado ao Hyperledger Fabric",
  "details": {
    "network": "fabric-test-network",
    "channel": "mychannel",
    "organization": "Org1MSP",
    "ca_status": "ONLINE"
  }
}' -ForegroundColor White

Write-Host "TESTE 3: REGISTRAR EVENTO" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/blockchain/test/event" -ForegroundColor Gray
Write-Host "Metodo: POST" -ForegroundColor Gray
Write-Host "Body:" -ForegroundColor Yellow
Write-Host '{
  "userId": "usuario_teste",
  "action": "login",
  "status": "success",
  "details": "Teste via API"
}' -ForegroundColor White

Write-Host "Resposta esperada:" -ForegroundColor Yellow
Write-Host '{
  "success": true,
  "transactionId": "tx_123456",
  "dataHash": "hash_789abc",
  "message": "Evento registrado com sucesso",
  "blockchain": {
    "network": "hyperledger-fabric",
    "channel": "mychannel",
    "block_number": 1234
  }
}' -ForegroundColor White

Write-Host "TESTE 4: ESTATISTICAS" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/blockchain/test/statistics" -ForegroundColor Gray
Write-Host "Resposta esperada:" -ForegroundColor Yellow
Write-Host '{
  "success": true,
  "statistics": {
    "networkOnline": true,
    "simulationMode": false,
    "totalEvents": 25,
    "successfulEvents": 24,
    "failedEvents": 1,
    "successRate": 96.0,
    "connectionStatus": {
      "hyperledgerFabric": "ONLINE",
      "mode": "REAL"
    }
  }
}' -ForegroundColor White

Write-Host "RESUMO DOS DADOS:" -ForegroundColor Magenta
Write-Host "- Aplicacao na porta 8081" -ForegroundColor White
Write-Host "- Blockchain Hyperledger Fabric conectada" -ForegroundColor White
Write-Host "- Eventos registrados em tempo real" -ForegroundColor White
Write-Host "- Estatisticas coletadas automaticamente" -ForegroundColor White
Write-Host "- Relatorios detalhados disponiveis" -ForegroundColor White

Write-Host "ENDPOINTS DISPONIVEIS:" -ForegroundColor Magenta
Write-Host "- GET  http://localhost:8081/api/blockchain/test/connection" -ForegroundColor Gray
Write-Host "- GET  http://localhost:8081/api/blockchain/test/status" -ForegroundColor Gray
Write-Host "- POST http://localhost:8081/api/blockchain/test/event" -ForegroundColor Gray
Write-Host "- GET  http://localhost:8081/api/blockchain/test/statistics" -ForegroundColor Gray
Write-Host "- GET  http://localhost:8081/api/blockchain/test/report" -ForegroundColor Gray

Write-Host "COMO USAR:" -ForegroundColor Magenta
Write-Host "1. Inicie a aplicacao: cd api-author && ./mvnw spring-boot:run" -ForegroundColor White
Write-Host "2. Execute testes: .\scripts\test-simples.ps1" -ForegroundColor White
Write-Host "3. Ou use curl: curl http://localhost:8081/api/blockchain/test/connection" -ForegroundColor White

Write-Host "DADOS DA BLOCKCHAIN:" -ForegroundColor Magenta
Write-Host "- Eventos de autenticacao registrados" -ForegroundColor White
Write-Host "- Hashes SHA-256 dos dados sensiveis" -ForegroundColor White
Write-Host "- Transacoes imutaveis no Hyperledger Fabric" -ForegroundColor White
Write-Host "- Estatisticas em tempo real" -ForegroundColor White
Write-Host "- Auditoria completa de todas as acoes" -ForegroundColor White

Write-Host "DEMONSTRACAO CONCLUIDA!" -ForegroundColor Green 