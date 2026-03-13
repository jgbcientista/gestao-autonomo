**Roteiro de Apresentação**

**1. O Problema**

  - Sistemas de autenticação tradicionais (login/senha) são estáticos e vulneráveis
  - Não aprendem com o comportamento do usuário
  - Não possuem trilha de auditoria imutável

**2. A Solução - Arquitetura do Sistema**

  Mostre o fluxo completo que acontece em cada login:
  Login → Coleta de 14 features → IA Ensemble (3 modelos) → Trust Score → Decisão → Blockchain

**3. Demonstração ao Vivo (tela por tela)**

  **Tela 1 - Dashboard: Visão geral do sistema funcionando**

  **Tela 2 - Score de Confiança:**
  - Selecionar usuário com score alto (ex: Maria Silva, 1.00) → PERMITIR
  - Selecionar usuário com score baixo → BLOQUEAR ou EXIGIR MFA
  - Mostrar que o score é adaptativo (muda com o histórico)

  **Tela 3 - Análise Comportamental IA:**
  - Selecionar um usuário e executar análise
  - Mostrar os 3 algoritmos rodando: Isolation Forest (40%), Random Forest (30%), Deep Learning (30%)
  - Mostrar as 14 features coletadas (horário, IP, dispositivo, geolocalização, etc.)
  - Mostrar como o Trust Score influencia a classificação (score 1.0 → difícil ser suspeito)
  - Mostrar a análise contextual explicando o porquê do resultado

  **Tela 4 - Blockchain:**
  - Mostrar as 604+ transações registradas automaticamente
  - Selecionar um usuário → listar todos os hashes dele
  - Clicar em um hash → mostrar os dados completos (IP, risco, decisão, SHA-256)
  - Verificar integridade de uma transação → provar que os dados não foram alterados
  - Explicar: "Se alguém alterar qualquer dado, o hash muda e a cadeia inteira é invalidada"

**4. Pontos Fortes para Destacar**

  **IA:**
  - Ensemble de 3 algoritmos (não depende de um só)
  - Decisão baseada em 14 features comportamentais reais
  - Trust Score adaptativo que aprende com o histórico
  - Fórmula: scoreAjustado = scoreAnomalia × (1 - trustScore × 0.6)

  **Blockchain:**
  - Cada evento de autenticação é registrado de forma imutável
  - Hash SHA-256 garante integridade e não-repúdio
  - Suporte a 3 redes: Java nativo (PoW), Hyperledger Fabric, Ethereum
  - Permite auditoria forense — rastrear quem acessou, quando, de onde

  **Integração IA + Blockchain:**
  - A IA decide (permitir/MFA/bloquear)
  - A blockchain registra a decisão de forma imutável
  - O Trust Score aprende com o histórico para futuras decisões
  - Ciclo fechado: decisão → registro → aprendizado → melhor decisão

**5. Perguntas Comuns da Banca**

  ┌───────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────┐
  │             ** Pergunta  **               │                                   **Resposta      **                          │
  ├───────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────┤
  │ Por que 3 algoritmos e não 1?             │ Ensemble reduz viés e aumenta acurácia — se um falha, os outros compensam     │
  ├───────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────┤
  │ Por que blockchain e não um banco normal? │ Imutabilidade — ninguém pode alterar logs de auditoria, nem um DBA            │
  ├───────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────┤
  │ O que acontece se a IA errar?             │ O sistema de feedback permite corrigir falsos positivos/negativos e retreinar │
  ├───────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────┤
  │ Como o trust score é calculado?           │ 40% histórico + 30% IA + 20% comportamento recente + 10% fatores externos     │
  ├───────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────┤
  │ A blockchain não é lenta?                 │ Operações assíncronas — o login não espera a confirmação do bloco             │
  └───────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────┘