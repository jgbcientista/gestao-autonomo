 ---
  **1. Explicabilidade da IA (XAI) - "Por que fui bloqueado?"**

  O que nao existe no mercado: Sistemas de autenticacao bloqueiam usuarios sem explicar o motivo. O seu sistema poderia gerar um relatorio visual explicando cada fator que contribuiu para a decisao.

  Implementacao:
  - Tela mostrando: "Seu acesso foi classificado como SUSPEITO porque: horario incomum (peso 35%), dispositivo novo (peso 25%), localizacao diferente (peso 40%)"
  - Grafico de barras com a contribuicao de cada fator
  - Comparacao com o perfil habitual do usuario

  Diferencial academico: Explainable AI (XAI) em autenticacao e um tema quente em pesquisa e quase inexistente em producao.

  ---
  **2. Deteccao de Deriva Comportamental (Concept Drift)**

  O que nao existe: Sistemas atuais tratam anomalias de forma estatica. O seu poderia detectar quando o comportamento do usuario muda gradualmente (mudou de emprego, mudou de cidade) vs mudanca abrupta (conta
  comprometida).

  **Implementacao:
  - Janela deslizante comparando perfil dos ultimos 7 dias vs ultimos 30 dias
  - Classificacao: "Evolucao natural" vs "Ruptura comportamental"
  - Ajuste automatico do baseline quando a mudanca e gradual

  **Diferencial academico:** Concept drift em autenticacao adaptativa e pouco explorado na literatura.

  ---
  **3. Keystroke Dynamics (Biometria de Digitacao)**

  O que nao existe integrado com IA: Capturar o ritmo de digitacao do usuario (tempo entre teclas, tempo de pressao) na tela de login e usar como fator adicional de autenticacao.

  Implementacao:
  - JavaScript no frontend captura: keyDown, keyUp timestamps durante a digitacao da senha
  - Backend compara o padrao com o historico do usuario
  - Adiciona como mais um fator no ensemble de IA

  Diferencial academico: Biometria comportamental sem hardware adicional. Muito citado em papers mas raramente implementado em sistemas reais.

  ---
  **4. Dashboard de Transparencia para o Usuario**

  O que nao existe: Um painel onde o proprio usuario ve seu perfil comportamental, historico de acessos, score de confianca e pode contestar decisoes.

  **Implementacao:**
  - Tela "Meu Perfil de Seguranca" mostrando:
    - Mapa com seus acessos
    - Dispositivos reconhecidos
    - Horarios habituais
    - Score atual e historico
    - Botao "Contestar bloqueio"

  **Diferencial academico:** Transparencia e controle do usuario sobre seus dados (alinhado com LGPD/GDPR).

  ---
  **5. Analise de Rede Social de Dispositivos**

  O que nao existe: Detectar quando multiplos usuarios acessam do mesmo dispositivo/IP e criar um grafo de relacoes.

  Implementacao:
  - Grafo visual mostrando conexoes entre usuarios, IPs e dispositivos
  - Deteccao de clusters suspeitos (mesma maquina acessando 10 contas)
  - Alerta de "compartilhamento de credenciais"

  Diferencial academico: Graph analysis aplicado a autenticacao.

  ---
  Minha recomendacao para a banca:

  Implementar as opcoes 1 (Explicabilidade) e 3 (Keystroke Dynamics) teria o maior impacto:

  - Explicabilidade porque a banca pode VISUALIZAR o que a IA esta fazendo — nao e uma caixa preta
  - Keystroke Dynamics porque e uma biometria invisivel ao usuario, sem hardware adicional, e integravel com os 3 modelos de IA que voce ja tem