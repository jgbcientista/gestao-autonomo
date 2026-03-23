
  1. Painel de Simulação de Ataques (alto impacto para banca)

  Uma tela onde você simula cenários de ataque e mostra como o sistema reage autonomamente:

  - Brute Force: Simular 10 tentativas de login rápidas do mesmo IP e mostrar o bloqueio automático
  - Credential Stuffing: Simular logins de IPs diferentes em curto intervalo
  - Impossible Travel: Login em São Paulo e 5 minutos depois em Tokyo - o sistema detecta e bloqueia
  - Device Hijacking: Acesso do mesmo usuário em dispositivo totalmente novo + localização nova

  Cada cenário mostraria em tempo real: score caindo, decisão mudando (PERMITIR -> MFA -> BLOQUEAR), transação registrada na blockchain.

  Por que é diferencial: Nenhum sistema de mercado oferece simulação visual de ataques com resposta autônoma. Isso demonstra concretamente a eficácia do sistema.

  ---
  2. Relatório de Auditoria Exportável (PDF)

  Gerar um relatório completo de um usuário com:

  - Score de confiança atual e histórico
  - Todos os acessos com localizações (mapa ou tabela)
  - Anomalias detectadas pela IA
  - Transações registradas na blockchain com hashes verificáveis
  - Decisões tomadas autonomamente pelo sistema

  Por que é diferencial: Atende requisitos de compliance (LGPD, ISO 27001). A banca vai ver aplicabilidade real no mercado.

  ---
  3. Dashboard de Comparação de Modelos de IA

  Uma visualização que mostra lado a lado como cada modelo (Isolation Forest, Random Forest, Deep Learning) classificou o mesmo acesso:

  - Gráfico radar/spider comparando os 3 modelos
  - Mostrar quando os modelos discordam entre si (ex: Random Forest diz normal, Isolation Forest diz anômalo)
  - Explicar por que o ensemble é mais robusto que qualquer modelo individual
  - Métricas: precisão, recall, F1-score de cada modelo

  Por que é diferencial: Demonstra profundidade acadêmica e justifica a abordagem ensemble.

  ---
  4. Mapa de Calor de Acessos Geográficos

  Visualização geográfica (pode ser um mapa simples com pontos) mostrando:

  - De onde o usuário acessa normalmente (pontos verdes)
  - Acessos suspeitos (pontos amarelos/vermelhos)
  - Linhas conectando acessos em sequência temporal
  - Detecção visual de "impossible travel"

  Por que é diferencial: Visual muito forte para apresentação. A banca entende instantaneamente o valor da geolocalização na segurança.

  ---
  5. Timeline de Eventos de Segurança

  Uma linha do tempo interativa mostrando todos os eventos de segurança de um usuário em ordem cronológica:

  - Login normal, MFA exigido, bloqueio, desbloqueio
  - Cada evento com o score da IA naquele momento
  - Hash da blockchain associado ao evento
  - Filtros por tipo de evento e período

  Por que é diferencial: Mostra a rastreabilidade completa - IA decidindo + blockchain registrando. É o core da dissertação em uma tela.

  ---
  Minha recomendação de prioridade:

  1. Simulação de Ataques - maior impacto na defesa, demonstra o sistema "em ação"
  2. Mapa de Calor Geográfico - visual forte, fácil de entender
  3. Comparação de Modelos IA - profundidade acadêmica