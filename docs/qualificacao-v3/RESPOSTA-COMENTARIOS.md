# Resposta aos comentários do Prof. Cleber: qualificação v3

- Documento comentado: `docs/doc-qualificacao-ifba-REVISADO-para-ajustar-v1.docx` (18 comentários, 08–09/10/2026)
- Versão revisada: `docs/doc-qualificacao-ifba-v3.docx`
- PDF: `docs/doc-qualificacao-ifba-v3.pdf` (46 págs., ABNT), gerado por `gerar_pdf_v3.py` a partir do mesmo `conteudo.py`, via Edge headless, porque não há Word nesta máquina. O PDF não é exportado do `.docx`, então a paginação dos dois pode diferir um pouco.
- Fonte do texto: `docs/qualificacao-v3/conteudo.py`. O `.docx` é gerado por `gerar_qualificacao_v3.py`, e as figuras por `gerar_figuras.py`. Rodar sempre com `python -I`.

Além dos comentários, a v3 incorpora as correções feitas depois da avaliação do SBESC 2026 (`docs/artigo-revisao/RASTREABILIDADE-REVISOES.md`). O documento comentado era anterior a elas e ainda trazia números e afirmações que o código não sustenta: F1 de 80,6%, blockchain "validada", Kubernetes, ELK/Grafana e rótulos "revisados por especialistas".

| # | Comentário | O que foi feito | Onde (v3) |
|---|-----------|-----------------|-----------|
| 0 | "Rever toda a formatação. Tem certeza que está usando o padrão do PPGESP?" | Formatação refeita conforme a ABNT (NBR 14724, 6023 e 6024): capítulos numerados sem a faixa "CAPÍTULO N"; títulos de pré-textuais sem número e fora do sumário; legenda acima e fonte abaixo de figuras e tabelas; tabelas sem bordas laterais; alíneas a), b), c); referências alinhadas à esquerda, em espaço simples; data da capa como "Salvador / 2026"; palavras-chave do resumo em português; lista de tabelas incluída. **Pendente: confirmar com a secretaria se o PPGESP tem um modelo próprio.** Não o encontrei publicado. | Documento inteiro |
| 1 | Sugestão de organização (Cap. 1 Introdução c/ problema, hipóteses, objetivos, justificativa, metodologia e estrutura; Cap. 2 Revisão; Cap. 3 Trabalhos Relacionados c/ protocolo; Cap. 4 Proposta; Cap. 5 Avaliação; Cap. 6 Considerações finais + cronograma) | Estrutura adotada integralmente | Sumário |
| 2 | "A Introdução não motivou o trabalho de maneira adequada" | Introdução reescrita a partir de dados: DBIR 2025 (abuso de credenciais é o 1º vetor, com 22% de 12.195 violações), IBM 2024 (16% e 292 dias para identificar e conter), Thomas et al. 2017 (1,9 bilhão de credenciais vazadas), Das et al. 2014 (reutilização de senhas). Em seguida, as três lacunas da RBA: poucos sinais, regras heurísticas e falta de prestação de contas (art. 20 da LGPD) | Cap. 1 |
| 3 | Estrangeirismos em itálico | Todos os termos não incorporados ao português estão em itálico (*login*, *phishing*, *credential stuffing*, *ensemble*, *hash*, *blockchain*, *fingerprint*, *F1-score*, *overfitting* etc.). Quando havia equivalente em português, ele foi usado ("escore", "revocação", "conjunto de dados") | Documento inteiro |
| 4 | Relevância sem dados concretos nem fontes | Ver item 2. Os números estão citados com a fonte | 1 (intro), 1.3 |
| 5 | Melhorar a escrita do objetivo geral | O objetivo geral agora diz o que o artefato faz, como faz e o que é avaliado. Os objetivos específicos estão em alíneas e cada um é verificável | 1.2 |
| 6 | "De onde vem essa urgência? Quem afirma isso?" | Frase removida. A justificativa agora se apoia em fontes | 1.3 |
| 7 | "Isso não justifica o seu trabalho" | Justificativa reescrita em quatro eixos: científico (lacuna da revisão, Cap. 3), prático (dados do DBIR/IBM), regulatório (arts. 20 e 37 da LGPD) e aderência ao programa | 1.3 |
| 8 | Diferenciais sem referência | Os diferenciais saíram da justificativa. A diferença em relação ao estado da arte agora decorre da síntese da revisão (Tabela 3) | 1.3, 3.5 |
| 9 | Idem, sobre blockchain e microsserviços | Blockchain com fontes (Nakamoto; Yaga et al.; Androulaki et al.; Schneier e Kelsey; Crosby e Wallach; BlockAudit), comparada a logs encadeados. Microsserviços com Dragoni et al. e Newman | 2.5, 2.6 |
| 10 | Afirmação sem citação ("minimiza falsos positivos…") | Reescrita com citação (Dietterich, 2000). A afirmação passou a ser hipótese (H1) e é testada | 4.5, 1.1, 5.4 |
| 11 | "Precisa trazer as citações… rever em todo o texto" | Cada algoritmo, método e métrica tem fonte: Liu et al.; Breiman; Rumelhart et al.; Kingma e Ba; Kohavi; Hastie et al.; Chandola et al.; He e Garcia; Fawcett; Saito e Rehmsmeier; Biggio e Roli; Arp et al.; Sommer e Paxson. As referências passaram de 28 para 57, todas citadas no texto | Cap. 2 e documento inteiro |
| 12 | 3.3 deve ser contextualizada com o que foi feito no trabalho | A parte conceitual foi para a fundamentação, com referências (2.3). A seção de treinamento descreve só o que foi executado: gerador, rótulos, divisão 70/30, padronização no treino, limiar escolhido no treino e CV restrita ao treino | 2.3, 4.6 |
| 13 | Texto informal (analogia do segurança) | Analogia removida. A seção foi reescrita em linguagem técnica, com referências | 2.3 |
| 14 | Imagens que ilustrem o processo de treinamento | Nova Figura 4 com o processo executado | 4.6 |
| 15 | Vai e volta (3.4 remetia à 5.2) | Os dados e o processo agora são descritos no ponto em que são usados (4.6). O Cap. 5 só remete para trás | 4.6, 5.1 |
| 16 | Repensar a arquitetura em camadas conforme o avaliador da conferência | As camadas eram definidas por tecnologia e foram redefinidas por responsabilidade: a pergunta que cada uma responde, sua entrada e saída e a ameaça que trata. O estado de cada camada no protótipo está declarado (Figura 2 e Tabela 4). A seção também ganhou um modelo de ameaças explícito e uma seção que lista as divergências entre o protótipo e a arquitetura-alvo, que era o ponto central do revisor 1 (blockchain simulada, Eq. 1 diferente do código, faixas não ligadas) | 4.1, 4.2, 4.9 |
| 17 | "Tecnologias Utilizadas" pode ir para a fundamentação | Movida para 2.6, com referências (RFC 6749, OIDC Core, RFC 7519, Keycloak, Docker, scikit-learn) | 2.6 |

## Outras mudanças

- **Hipóteses H1–H3** foram definidas e são discutidas uma a uma no Cap. 5. H1 é apoiada pelos dados, H2 é parcialmente confirmada (falha com 1–2% de ataques e sob evasão) e H3 ainda não foi avaliada.
- **Cap. 5** recebeu os experimentos pós-SBESC: três faixas (Tabela 8), modelos isolados e pesos (Tabela 9), proporção de ataques (Tabela 10) e evasão (Tabela 11). Os números conferem com `artefatos-modelos/metrics.json` e `experimentos-revisao.json`.
- **Removido do texto**, por não existir no repositório: Kubernetes no experimento, ELK/Grafana, fallback heurístico, retreinamento semanal com rollback, "testes de imutabilidade com validação criptográfica", "latência de algumas centenas de ms com registro", hash SHA-3 (o código usa SHA-256), rótulos "revisados por especialistas", distribuição "observada em produção", hora "circular" e *dropout*.
- **Figuras** regeneradas sem título embutido, porque a legenda fica no texto. A antiga Figura 4 mostrava nomes reais ("user: joao") e foi refeita com identificadores pseudonimizados, coerente com a LGPD.
- **Referências** sem uso foram retiradas: CISA, Executive Order 14144, OWASP, SSDF, SP 800-53 e AI RMF.

## Pendências que dependem do João

1. **Modelo do PPGESP**: pedir à secretaria o template oficial de qualificação/dissertação, se houver.
2. **Revisão estruturada (3.3)**: o texto diz que ela está "em execução". Falta rodar a string nas quatro bases e registrar quantos estudos foram retornados, excluídos e incluídos em cada etapa. Nenhum número foi inventado.
3. **Cronograma (Tabela 12)**: datas propostas até jul./2027. Ajustar ao prazo do programa e combinar com o orientador.
4. **Abrir no Word**: o arquivo pede para atualizar os campos ao abrir. Responder "Sim" para que sumário, lista de figuras e lista de tabelas recebam as páginas. Depois, conferir a paginação e salvar ou exportar o PDF. Não há Word nesta máquina, então a paginação não foi conferida visualmente.
