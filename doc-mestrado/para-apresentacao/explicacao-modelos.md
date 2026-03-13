  **Os 3 Modelos de IA do Sistema**

  ---
  **1. Isolation Forest (Floresta de Isolamento)**

  O que é: Algoritmo de detecção de anomalias não-supervisionado — não precisa saber de antemão o que é "normal" ou "anômalo".

  Analogia: Imagine que você tem uma sala cheia de pessoas. Uma pessoa vestida de palhaço seria fácil de separar do grupo com poucas perguntas ("está usando nariz vermelho?"). Uma pessoa normal precisaria de
  muitas perguntas para ser isolada. O Isolation Forest funciona assim — anomalias são fáceis de isolar.

  **Como funciona:**
  1. Pega os dados e escolhe uma feature aleatória (ex: "hora do acesso")
  2. Escolhe um ponto de corte aleatório entre o mínimo e máximo dessa feature
  3. Divide os dados em dois grupos: quem ficou acima e quem ficou abaixo
  4. Repete recursivamente até isolar cada ponto
  5. Quanto menos divisões precisou para isolar um ponto, mais anômalo ele é

  **No sistema:**
  - 100 árvores, cada uma com amostra de até 256 registros
  - Score perto de 1.0 = anômalo (isolado rápido)
  - Score perto de 0.0 = normal (difícil de isolar)

  Por que é útil: Detecta ataques nunca vistos antes. Não precisa de exemplos de ataques para aprender — apenas identifica o que foge do padrão. Por isso tem o maior peso (40%) no ensemble.


  ---  
  **2. Random Forest (Floresta Aleatória)**

  O que é: Algoritmo de classificação supervisionado — aprende com exemplos rotulados ("este acesso foi normal", "este foi anômalo").

  Analogia: Imagine 50 juízes avaliando se um acesso é suspeito. Cada juiz vê apenas parte das evidências (60% das features) e analisa casos diferentes (bootstrap). No final, a decisão é a média dos votos.
  Isso evita que um único juiz enviesado domine a decisão.

  Como funciona:
  1. Cria 50 árvores de decisão independentes
  2. Cada árvore recebe uma amostra diferente dos dados (bootstrap — sorteio com reposição)
  3. Cada árvore vê apenas 60% das 14 features (seleção aleatória)
  4. Cada árvore faz perguntas do tipo "hora > 22?", "IP novo?" e divide os dados
  5. A melhor pergunta é escolhida pela impureza de Gini — busca a divisão que melhor separa normais de anômalos
  6. Na predição, cada árvore dá seu voto e o resultado final é a média

  No sistema:
  - 50 árvores, profundidade máxima 10, mínimo 5 amostras por folha
  - Label vem do campo classificacaoAcesso: ESPERADO = normal, qualquer outro = anômalo
  - Score = proporção de árvores que votaram "anômalo"

  Por que é útil: É robusto e interpretável. Aprende padrões específicos do histórico real — por exemplo, que determinado usuário sempre loga entre 8h-18h de segunda a sexta. Peso de 30% no ensemble.


  ---
  **3. Deep Learning (Rede Neural Profunda)**

  O que é: Rede neural artificial com múltiplas camadas que captura padrões complexos e não-lineares nos dados.

  Analogia: Funciona como camadas de filtros. A primeira camada detecta padrões simples ("horário incomum"), a segunda combina padrões simples ("horário incomum + IP novo"), a terceira identifica combinações
  ainda mais complexas ("horário incomum + IP novo + localização diferente + pouca frequência"). Cada camada abstrai mais.

  **Arquitetura:**
  
  Entrada (14 features)
      ↓
  Camada 1: 32 neurônios (ReLU) — detecta padrões simples
      ↓
  Camada 2: 16 neurônios (ReLU) — combina padrões
      ↓
  Camada 3: 8 neurônios (ReLU)  — abstrações complexas
      ↓
  Saída: 1 neurônio (Sigmoid)   — probabilidade 0 a 1

  **Como funciona:**
  
  1. As 14 features são normalizadas para [0, 1] (Min-Max)
  2. Os pesos iniciais são definidos por Xavier initialization (valores proporcionais ao tamanho da camada)
  3. Treina por 100 epochs (100 passadas completas pelos dados)
  4. Cada passada divide os dados em mini-batches de 32
  5. Para cada batch: faz o forward pass (calcula a previsão), compara com o label real (MSE loss), e ajusta os pesos via backpropagation
  6. Ativação ReLU nas camadas ocultas (se x > 0 passa, senão 0)
  7. Ativação Sigmoid na saída (comprime qualquer valor para entre 0 e 1)

  **No sistema:**
  - Learning rate: 0.001 (passos pequenos para não desestabilizar)
  - Label: 1.0 = anômalo, 0.0 = normal
  - Score de saída = probabilidade de ser anomalia

  Por que é útil: Captura relações não-lineares complexas que os outros dois modelos não conseguem. Por exemplo, um acesso às 3h da manhã pode ser normal para um programador que trabalha de noite, mas anômalo
  para um funcionário de escritório — a rede neural aprende essas nuances. Peso de 30% no ensemble.

  ---
  **Comparação resumida**

  ┌────────────────────────┬────────────────────┬───────────────────┬───────────────────┐
  │        Aspecto         │  Isolation Forest  │   Random Forest   │   Deep Learning   │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Tipo                   │ Não-supervisionado │ Supervisionado    │ Supervisionado    │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Precisa de labels?     │ Não                │ Sim               │ Sim               │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Detecta ataques novos? │ Sim                │ Parcialmente      │ Parcialmente      │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Interpretabilidade     │ Média              │ Alta              │ Baixa             │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Padrões complexos      │ Não                │ Moderado          │ Sim               │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Peso no ensemble       │ 40%                │ 30%               │ 30%               │
  ├────────────────────────┼────────────────────┼───────────────────┼───────────────────┤
  │ Estrutura              │ 100 árvores        │ 50 árvores + Gini │ 4 camadas neurais │
  └────────────────────────┴────────────────────┴───────────────────┴───────────────────┘

  **Por que usar os 3 juntos?**

  **Cada modelo tem pontos cegos que os outros cobrem:**
  - O Isolation Forest detecta o que nunca foi visto, mas não usa o histórico rotulado
  - O Random Forest aprende bem com histórico, mas pode não pegar ataques inéditos
  - O Deep Learning captura relações complexas, mas precisa de muitos dados

  Juntos (ensemble), a decisão é mais confiável e resiliente do que qualquer modelo sozinho.