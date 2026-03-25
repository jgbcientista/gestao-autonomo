**Parar:**
  docker-compose down

**Iniciar:**
  docker-compose up -d
  
  docker-compose up -d --build 

**Ver logs em tempo real:**
  docker-compose logs -f

**Reiniciar tudo:**
  docker-compose restart

_______________________________________________________
COMANDOS
_______________________________________________________
    
  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
  https://github.com/settings/tokens
    
  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
    
  docker compose -f docker-compose.prod.yml up -d
  
  docker-compose up -d --build --force-recreate frontend
  
  ssh root@209.50.240.20

  ***REMOVIDO***
  
  claude --dangerously-skip-permissions
  _______________________________________________________
  BANCO DE DADOS
  _______________________________________________________
  
  psql -h localhost -U pcp_remoto_db -d pcp_remoto_db
  Senha: ***REMOVIDO***
  Depois execute:

  DROP SCHEMA login_inteligente CASCADE;
  CREATE SCHEMA login_inteligente;
  \q

  E reinicie o container da API:

  cd /home/login-inteligente
  docker compose -f docker-compose.prod.yml restart api
  
  Usuário: jgbcientista
  Senha: o token PAT
  
  echo "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" | docker login ghcr.io -u jgbcientista --password-stdin
  
  docker build -t ghcr.io/jgbcientista/sistema-autonomo-autenticacao-blockchain-ia/api:latest -f
  api-auditoria/Dockerfile api-auditoria/
  
  docker build -t ghcr.io/jgbcientista/sistema-autonomo/frontend:latest -f front-auditoria/Dockerfile.frontend front-auditoria/
  
  docker build -t ghcr.io/jgbcientista/sistema-autonomo/api:latest -f api-auditoria/Dockerfile api-auditoria/
  
  
  **quando fizer build direto na VPS, use o nome completo:**

  docker build -t ghcr.io/jgbcientista/sistema-autonomo/api:latest -f api-auditoria/Dockerfile api-auditoria/
  docker build -t ghcr.io/jgbcientista/sistema-autonomo/frontend:latest -f front-auditoria/Dockerfile.frontend front-auditoria/
  
  