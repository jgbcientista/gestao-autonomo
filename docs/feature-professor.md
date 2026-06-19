Olá, João.

Fiz alguns comentários diretamente no texto da sua qualificação e, por isso, peço que os verifique com atenção. Além dessas observações, seguem algumas considerações gerais que considero importantes para o aprimoramento do trabalho.

Identifiquei uma inconsistência conceitual entre o que o título propõe e o que é efetivamente desenvolvido ao longo do texto. Em diversos momentos, o trabalho aborda a autenticação contextual de usuários; em outros, passa a descrever um pipeline DevSecOps voltado à liberação de deploys de software, utilizando mecanismos como SAST, DAST e evidências de build. Embora ambos os temas estejam relacionados à segurança da informação, tratam de problemas de pesquisa distintos. Dessa forma, é fundamental definir claramente o foco principal do trabalho e garantir a coerência entre os objetivos, a fundamentação teórica, a proposta e os resultados apresentados.

Também identifiquei indícios de trechos que parecem ter sido reaproveitados sem a devida adequação ao contexto da pesquisa. Um exemplo é a presença, na seção de abstract, de palavras-chave relacionadas a “temperature control” e “Peltier effect”, termos que claramente não possuem relação com o tema investigado. Recomendo uma revisão cuidadosa do texto para eliminar inconsistências editoriais, repetições e possíveis desalinhamentos conceituais.

A utilização de blockchain como mecanismo de auditoria e garantia de integridade dos registros é uma proposta interessante e coerente com o domínio do problema. No entanto, os benefícios dessa escolha precisam ser melhor justificados ao longo do texto, evitando a percepção de que a tecnologia está sendo utilizada apenas por seu apelo tecnológico. Em muitos cenários, mecanismos como logs assinados digitalmente, bancos de dados imutáveis ou estruturas append-only poderiam atender parte dos requisitos propostos. Assim, a dissertação deve explicitar de forma clara por que o blockchain é necessário neste contexto específico e em quais aspectos ele supera alternativas mais simples. Considero que essa justificativa já foi parcialmente fortalecida na apresentação dos trabalhos correlatos, mas ela ainda precisa aparecer de forma mais explícita e consistente no texto.

Ajustes Prioritários
Unificar o objeto de estudo, definindo claramente se o foco é autenticação contextual ou governança de pipelines DevSecOps.
Corrigir inconsistências textuais e editoriais, especialmente no abstract e em trechos que aparentam estar desalinhados com o tema central.
Explicitar o desenho experimental, incluindo fonte dos dados, processo de rotulagem, critérios éticos, divisão entre conjuntos de treinamento e teste, métricas utilizadas e aspectos de reprodutibilidade.
Justificar de forma mais robusta a adoção de blockchain em comparação com alternativas como logs assinados digitalmente ou bancos append-only.
Apresentar os resultados com maior rigor metodológico, incluindo comparação com abordagens de referência (baselines) e análise crítica das diferenças entre os objetivos propostos e os resultados obtidos.
Sugestão de Estrutura

1. Introdução

Apresentar o problema de pesquisa, hipótese, objetivos, justificativa, delimitação do escopo e organização do documento.

2. Fundamentação Teórica

Consolidar os conceitos que sustentam a proposta, tais como:

Autenticação contextual;
Segurança da informação;
Inteligência Artificial aplicada à cibersegurança;
Blockchain e mecanismos de auditoria;
Microserviços, DevSecOps e observabilidade (caso sejam efetivamente elementos centrais da solução).

3. Trabalhos Correlatos

Este capítulo deve responder às seguintes questões:

O que já existe na literatura?
Quais abordagens utilizam Inteligência Artificial?
Quais utilizam blockchain?
Quais combinam IA e blockchain?
Quais lacunas e limitações permanecem abertas?

Uma possível organização é:

Soluções de autenticação contextual;
Uso de IA em controle de acesso;
Blockchain para auditoria e rastreabilidade;
Sistemas híbridos IA + blockchain aplicados à segurança;
Comparação entre abordagens.

Recomendo finalizar o capítulo com uma tabela comparativa contendo, para cada trabalho:

Objetivo;
Técnicas empregadas;
Tipo de dados utilizados;
Uso de blockchain;
Uso de IA;
Principais limitações;
Diferenças em relação à proposta desta dissertação.

4. Proposta da Solução

Descrever a contribuição original da pesquisa, incluindo:

Arquitetura geral;
Fluxo de autenticação;
Papéis da IA, do Keycloak e do blockchain;
Critérios de decisão;
Requisitos funcionais e não funcionais.

5. Implementação

Apresentar os aspectos técnicos do protótipo:

Tecnologias utilizadas;
Arquitetura de microsserviços;
Integração entre os componentes;
Pipeline de inferência;
Mecanismos de armazenamento e auditoria.

6. Metodologia de Avaliação

Capítulo fundamental para a qualificação, contendo:

Cenário experimental;
Dataset utilizado;
Métricas de avaliação;
Critérios de comparação;
Baselines adotados;
Hipóteses de avaliação.

7. Resultados e Discussão

Apresentar e analisar:

Resultados obtidos;
Desempenho da solução;
Latência;
Métricas como precisão, recall e F1-score;
Limitações identificadas;
Interpretação crítica dos resultados.

8. Considerações Finais

Apresentar:

Principais contribuições da pesquisa;
Limitações do estudo;
Trabalhos futuros;
Cronograma de execução das etapas restantes até a defesa.

Atenciosamente

Cleber Jorge Lira de Santana, D.Sc.
Professor de Ciência da Computação
Instituto Federal da Bahia
http://lattes.cnpq.br/4293687336599116