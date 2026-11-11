# -*- coding: utf-8 -*-
"""Conteudo textual da qualificacao v3 (fonte unica do texto).

Marcacao inline: *italico*  **negrito**  _{subscrito}
Blocos: ("cap", titulo) ("sec", titulo) ("sub", titulo) ("p", texto) ("al", [itens])
        ("fig", arquivo, titulo, fonte, largura_cm) ("tab", titulo, cabecalho, linhas, larguras_cm, fonte)
        ("eq", texto, numero)
Termos estrangeiros nao incorporados ao portugues vao em italico (comentario do orientador).
"""

TITULO = "SISTEMA AUTÔNOMO DE AUTENTICAÇÃO CONTEXTUAL COM BLOCKCHAIN E IA"

RESUMO = (
    "O abuso de credenciais é o vetor de acesso inicial mais frequente em violações de dados: no relatório da "
    "Verizon de 2025, ele respondeu por 22% das violações cujo vetor inicial foi identificado. "
    "A autenticação baseada em risco responde a esse cenário ajustando a verificação ao contexto de cada "
    "tentativa de acesso, mas as implementações descritas na literatura usam poucos sinais, decidem por regras "
    "heurísticas e não oferecem meios para que terceiros verifiquem as decisões automatizadas, exigência "
    "reforçada pela Lei Geral de Proteção de Dados. Este projeto de mestrado propõe, implementa e avalia um "
    "mecanismo de autenticação contextual que estima o risco de cada *login* a partir de quatorze critérios "
    "contextuais e comportamentais, por meio de um *ensemble* que combina *Isolation Forest*, *Random Forest* e "
    "uma rede neural multicamada; decide de forma proporcional ao risco (permitir, exigir autenticação multifator "
    "ou bloquear); e registra cada decisão em uma trilha de auditoria baseada em *blockchain* permissionada. A "
    "pesquisa segue a abordagem *Design Science Research* e é apoiada por uma revisão estruturada da literatura. "
    "Nesta fase, o motor de risco foi avaliado de forma reprodutível sobre 50.000 registros sintéticos: o "
    "*ensemble* obteve *F1-score* de 80,2%, AUC de 0,99 e taxa de falsos positivos de 0,8%, superando cada modelo "
    "isolado em cerca de três pontos de *F1*; com as três faixas de decisão, 2,7% dos acessos legítimos recebem "
    "algum desafio e 7,3% dos ataques passam sem desafio. A análise de evasão mostrou, contudo, que um atacante "
    "que conhece os critérios reduz a detecção de 77,6% para 18,0% ao imitar horário, ritmo e rede da vítima, o "
    "que torna os critérios difíceis de forjar um requisito da próxima etapa. A camada de auditoria encontra-se "
    "em projeto, e sua implementação em uma rede Hyperledger Fabric com ao menos duas organizações, bem como a "
    "avaliação com dados reais ou semirreais, constituem as etapas seguintes até a defesa."
)
PALAVRAS_CHAVE = ("Autenticação contextual; Autenticação baseada em risco; Aprendizado de máquina; "
                  "Blockchain; Auditoria; Segurança da informação.")

ABSTRACT = (
    "Credential abuse is the most frequent initial access vector in data breaches: in Verizon's 2025 report, it "
    "accounted for 22% of the breaches whose initial vector was identified. Risk-based authentication addresses "
    "this scenario by adjusting verification to the context of each access attempt, but the implementations "
    "described in the literature rely on few signals, decide through heuristic rules, and offer no means for "
    "third parties to verify automated decisions, a requirement reinforced by Brazil's General Data Protection "
    "Law (LGPD). This master's project proposes, implements, and evaluates a contextual authentication "
    "mechanism that estimates the risk of each login from fourteen contextual and behavioral criteria through "
    "an ensemble combining Isolation Forest, Random Forest, and a multilayer neural network; decides in "
    "proportion to the risk (allow, require multi-factor authentication, or block); and records each decision "
    "in an audit trail based on a permissioned blockchain. The research follows the Design Science Research "
    "approach and is supported by a structured literature review. At this stage, the risk engine was evaluated "
    "reproducibly on 50,000 synthetic records: the ensemble achieved an F1-score of 80.2%, an AUC of 0.99, and "
    "a false positive rate of 0.8%, outperforming each single model by about three F1 points; with the three "
    "decision bands, 2.7% of legitimate accesses receive a challenge and 7.3% of attacks pass unchallenged. "
    "The evasion analysis showed, however, that an attacker who knows the criteria reduces detection from "
    "77.6% to 18.0% by mimicking the victim's time, pace, and network, which makes hard-to-forge criteria a "
    "requirement for the next stage. The audit layer is at the design stage; implementing it on a Hyperledger "
    "Fabric network with at least two organizations and evaluating the system on real or semi-real data are "
    "the next steps before the defense."
)
KEYWORDS = ("Contextual authentication; Risk-based authentication; Machine learning; Blockchain; Auditing; "
            "Information security.")

SIGLAS = [
    ("2FA", "Autenticação de dois fatores (*Two-Factor Authentication*)"),
    ("API", "Interface de programação de aplicações (*Application Programming Interface*)"),
    ("ASN", "Número de sistema autônomo (*Autonomous System Number*)"),
    ("AUC", "Área sob a curva (*Area Under the Curve*)"),
    ("DSR", "*Design Science Research*"),
    ("FPR", "Taxa de falsos positivos (*False Positive Rate*)"),
    ("IA", "Inteligência Artificial"),
    ("IF", "*Isolation Forest*"),
    ("IFBA", "Instituto Federal de Educação, Ciência e Tecnologia da Bahia"),
    ("IP", "Protocolo de internet (*Internet Protocol*)"),
    ("JWT", "*JSON Web Token*"),
    ("LGPD", "Lei Geral de Proteção de Dados Pessoais"),
    ("MFA", "Autenticação multifator (*Multi-Factor Authentication*)"),
    ("MLP", "Perceptron multicamada (*Multilayer Perceptron*)"),
    ("NIST", "*National Institute of Standards and Technology*"),
    ("k-NN", "*k* vizinhos mais próximos (*k-Nearest Neighbors*)"),
    ("OIDC", "*OpenID Connect*"),
    ("PPGESP", "Programa de Pós-Graduação em Engenharia de Sistemas e Produtos"),
    ("PR-AUC", "Área sob a curva precisão-revocação (*Precision-Recall AUC*)"),
    ("RBA", "Autenticação baseada em risco (*Risk-Based Authentication*)"),
    ("REST", "*Representational State Transfer*"),
    ("RF", "*Random Forest*"),
    ("ROC", "Característica de operação do receptor (*Receiver Operating Characteristic*)"),
    ("ReLU", "Unidade linear retificada (*Rectified Linear Unit*)"),
    ("SBESC", "Simpósio Brasileiro de Engenharia de Sistemas Computacionais"),
    ("SHA", "*Secure Hash Algorithm*"),
    ("SHAP", "*SHapley Additive exPlanations*"),
    ("SVM", "Máquina de vetores de suporte (*Support Vector Machine*)"),
    ("TOTP", "Senha de uso único baseada em tempo (*Time-based One-Time Password*)"),
    ("XAI", "IA explicável (*Explainable Artificial Intelligence*)"),
]

C = []  # blocos do corpo
add = C.append

# =============================================================================================
add(("cap", "INTRODUÇÃO"))
add(("p", "A autenticação de usuários é a primeira barreira de proteção de aplicações *web* e, ao mesmo tempo, "
    "o ponto mais explorado por atacantes. O relatório de investigações de violações de dados da Verizon, que "
    "analisou 12.195 violações confirmadas, aponta o abuso de credenciais como o vetor de acesso inicial mais "
    "frequente entre as violações cujo vetor foi identificado: 22% dos casos, à frente da exploração de "
    "vulnerabilidades (20%) e do *phishing* (16%) (VERIZON, 2025). O relatório da IBM sobre o custo de violações de dados chega a conclusão semelhante: "
    "credenciais roubadas ou comprometidas foram o vetor inicial mais comum (16% dos casos) e o que mais demorou "
    "a ser identificado e contido, com média de 292 dias, em um cenário em que o custo médio global de uma "
    "violação atingiu US$ 4,88 milhões (IBM, 2024)."))
add(("p", "A disponibilidade de credenciais válidas para atacantes não é episódica. Thomas et al. (2017) "
    "identificaram, ao longo de um ano, 1,9 bilhão de pares de usuário e senha expostos em vazamentos e "
    "negociados em fóruns clandestinos, além de milhões de vítimas de *kits* de *phishing* e de *keyloggers*. "
    "Como uma parcela expressiva dos usuários reutiliza a mesma senha, ou variações dela, em diferentes serviços "
    "(DAS et al., 2014), cada vazamento alimenta ataques de *credential stuffing* contra serviços que não foram "
    "violados. Nesse cenário, a senha deixa de ser evidência suficiente de identidade, limitação reconhecida há "
    "mais de uma década pela literatura sobre alternativas às senhas (BONNEAU et al., 2012)."))
add(("p", "A autenticação multifator (MFA) reduz esse risco, mas, quando aplicada de modo estático a todos os "
    "acessos, introduz atrito para o usuário legítimo e permanece vulnerável a *phishing* em tempo real, troca "
    "de *chip* (*SIM swapping*) e engenharia social (NIST, 2017). Por isso, as diretrizes de identidade digital "
    "do NIST e o modelo de confiança zero (*Zero Trust*) recomendam que o nível de verificação seja proporcional "
    "ao risco estimado de cada tentativa de acesso (NIST, 2017; ROSE et al., 2020). Essa é a ideia central da "
    "autenticação baseada em risco (RBA), adotada por grandes provedores de serviços *online* (WIEFLING; LO "
    "IACONO; DÜRMUTH, 2019; DOERFLER et al., 2019)."))
add(("p", "Os estudos empíricos sobre RBA revelam, porém, três lacunas. A primeira é a pobreza de sinais: em "
    "serviços reais, a decisão apoia-se predominantemente no endereço IP, com pouca transparência sobre os demais "
    "fatores (WIEFLING; LO IACONO; DÜRMUTH, 2019). A segunda é a forma de decidir: regras heurísticas ou modelos "
    "estatísticos simples, ajustados manualmente a cada serviço (FREEMAN et al., 2016; WIEFLING et al., 2023). A "
    "terceira é a prestação de contas: quando a decisão de conceder ou negar acesso é tomada por um modelo "
    "automatizado, o titular dos dados tem direito de solicitar a revisão dessa decisão (BRASIL, 2018, art. 20), "
    "o que pressupõe registros confiáveis do que foi decidido e com base em quê. Registros mantidos pelo próprio "
    "operador, entretanto, podem ser alterados por quem os administra (SCHNEIER; KELSEY, 1999)."))
add(("p", "Diante disso, este projeto propõe um sistema autônomo de autenticação contextual que estima, a cada "
    "tentativa de *login*, o risco do acesso a partir de quatorze critérios contextuais e comportamentais; decide "
    "de forma proporcional a esse risco, permitindo o acesso, exigindo um segundo fator ou bloqueando a tentativa; "
    "e registra cada decisão em uma trilha de auditoria verificável por terceiros, baseada em *blockchain* "
    "permissionada. As seções seguintes delimitam o problema de pesquisa e as hipóteses (Seção 1.1), os objetivos "
    "(Seção 1.2), a justificativa (Seção 1.3), a metodologia (Seção 1.4) e a estrutura do texto (Seção 1.5)."))

add(("sec", "Problema e hipóteses"))
add(("p", "O problema que orienta esta pesquisa pode ser formulado na seguinte questão: **como decidir, a cada "
    "tentativa de *login*, entre permitir o acesso, exigir um segundo fator ou bloquear a tentativa, com base no "
    "contexto do acesso, de modo que o atrito para usuários legítimos permaneça baixo e que as decisões "
    "automatizadas possam ser verificadas por terceiros?**"))
add(("p", "A partir dessa questão, formulam-se três hipóteses, cada uma associada a uma avaliação específica "
    "(Capítulo 5):"))
add(("al", [
    "**H1** — um *ensemble* que combina um detector de anomalias não supervisionado (*Isolation Forest*) e "
    "classificadores supervisionados (*Random Forest* e rede neural multicamada) obtém maior *F1-score* e menor "
    "taxa de falsos positivos na classificação de risco de *login* do que cada modelo isolado, treinado e avaliado "
    "nas mesmas condições;",
    "**H2** — uma decisão em três faixas de risco (permitir, exigir MFA ou bloquear) mantém a taxa de atrito "
    "imposta a usuários legítimos abaixo de 5% e, ao mesmo tempo, bloqueia a maioria das tentativas maliciosas, "
    "com taxa de passagem silenciosa de ataques inferior a 10%;",
    "**H3** — o registro de cada decisão em uma *blockchain* permissionada com ao menos duas organizações permite "
    "detectar a adulteração da trilha de auditoria por um administrador de uma das organizações, com latência "
    "adicional compatível com um *login* interativo.",
]))
add(("p", "As hipóteses H1 e H2 foram avaliadas de forma preliminar nesta qualificação, sobre dados sintéticos; "
    "a hipótese H3 depende da implementação da camada de auditoria, prevista no cronograma (Seção 6.3). Os "
    "limites de 5% e 10% em H2 são metas de projeto. Como foram fixados nesta revisão, depois do experimento "
    "preliminar, o resultado sobre dados sintéticos não basta para confirmar H2: a hipótese será testada "
    "novamente com os dados semirreais da próxima etapa, sem alterar esses limites."))

add(("sec", "Objetivos"))
add(("sub", "Objetivo geral"))
add(("p", "Propor, implementar e avaliar um mecanismo de autenticação contextual para aplicações *web* que, a "
    "cada tentativa de *login*, estime o risco do acesso por meio de um *ensemble* de aprendizado de máquina, "
    "decida de forma proporcional a esse risco e registre cada decisão em uma trilha de auditoria verificável por "
    "terceiros, baseada em *blockchain* permissionada."))
add(("sub", "Objetivos específicos"))
add(("al", [
    "identificar, por meio de uma revisão estruturada da literatura, os sinais contextuais e comportamentais, os "
    "mecanismos de decisão e os mecanismos de auditoria empregados em autenticação baseada em risco;",
    "definir uma arquitetura em camadas e um modelo de ameaças para a autenticação contextual auditável;",
    "desenvolver um motor de risco baseado em *ensemble* e avaliá-lo quanto a desempenho de classificação, taxa "
    "de atrito, taxa de passagem silenciosa e robustez à evasão;",
    "implementar a camada de auditoria em uma rede Hyperledger Fabric com ao menos duas organizações e avaliar "
    "latência de confirmação, vazão e detecção de adulteração;",
    "integrar os componentes ao protótipo e avaliá-lo com dados reais ou semirreais, coletados conforme a LGPD.",
]))

add(("sec", "Justificativa"))
add(("p", "Do ponto de vista científico, a revisão da literatura apresentada no Capítulo 3 mostra que as linhas "
    "de pesquisa relacionadas ao tema evoluíram de forma isolada. Os estudos de RBA tratam da decisão, mas com "
    "poucos sinais e regras ajustadas manualmente (WIEFLING; LO IACONO; DÜRMUTH, 2019; FREEMAN et al., 2016); os "
    "estudos de biometria comportamental demonstram o poder discriminativo de uma única modalidade, como a "
    "digitação ou o toque, sem tratar da decisão de acesso nem de sua auditoria (ACIEN et al., 2022; FRANK et "
    "al., 2013); e os estudos de auditoria baseada em *blockchain* registram eventos de forma desacoplada do "
    "mecanismo que decide (AHMAD; SAAD; MOHAISEN, 2019). Entre os estudos analisados até o momento, nenhum "
    "combina, em um mesmo fluxo, a fusão de múltiplos sinais por aprendizado de máquina, a decisão proporcional "
    "ao risco e o registro verificável da decisão. Este trabalho busca preencher essa lacuna e, além disso, "
    "avaliar a abordagem com métricas que a literatura de RBA raramente reporta, como a taxa de passagem "
    "silenciosa e a robustez a um atacante que conhece os critérios, evitando armadilhas comuns na avaliação de "
    "aprendizado de máquina em segurança, como a ausência de um modelo de ameaças realista (ARP et al., 2022)."))
add(("p", "Do ponto de vista prático, o abuso de credenciais é o vetor de acesso inicial mais frequente em "
    "violações de dados (VERIZON, 2025) e o de maior tempo médio de identificação e contenção (IBM, 2024). Uma "
    "camada de decisão contextual reduz o valor de uma credencial vazada sem impor um segundo fator a todos os "
    "acessos, o que diminui o atrito para os usuários legítimos."))
add(("p", "Do ponto de vista regulatório, a LGPD assegura ao titular o direito de solicitar a revisão de "
    "decisões tomadas unicamente com base em tratamento automatizado (BRASIL, 2018, art. 20) e exige que o "
    "controlador mantenha registro das operações de tratamento (BRASIL, 2018, art. 37). Uma decisão de "
    "bloqueio tomada por um modelo de IA enquadra-se nessa situação. Daí a necessidade de uma trilha de "
    "auditoria que não dependa da confiança no próprio operador, o que justifica avaliar a *blockchain* "
    "permissionada frente a alternativas mais simples (Seção 2.5)."))
add(("p", "Por fim, a proposta alinha-se à linha de pesquisa do PPGESP/IFBA voltada a sistemas computacionais "
    "e segurança da informação, e seus artefatos (código, conjunto de dados e *scripts* de experimento) são "
    "disponibilizados abertamente, o que permite a verificação independente dos resultados."))

add(("sec", "Metodologia"))
add(("p", "Trata-se de uma pesquisa aplicada, de abordagem quantitativa, que tem como produto um artefato "
    "computacional. Por essa razão, adota-se a *Design Science Research* (DSR), paradigma voltado à construção e "
    "à avaliação de artefatos que resolvem problemas relevantes (HEVNER et al., 2004). O processo segue as seis "
    "atividades propostas por Peffers et al. (2007), mapeadas nos capítulos deste documento conforme a Figura 1."))
add(("fig", "fig1-dsr.png", "Atividades da *Design Science Research* e capítulos correspondentes",
     "Elaborado pelo autor (2026), com base em Peffers et al. (2007).", 15.5))
add(("p", "Na **identificação do problema** e na **definição dos objetivos**, o problema foi caracterizado com "
    "dados de relatórios do setor e da literatura científica (Seções 1.1 e 1.2). O **projeto e desenvolvimento** "
    "apoia-se na fundamentação teórica (Capítulo 2) e em uma revisão estruturada da literatura, conduzida segundo "
    "as diretrizes de Kitchenham e Charters (2007) e complementada por *snowballing* (WOHLIN, 2014), cujo "
    "protocolo é descrito no Capítulo 3. O artefato resultante (arquitetura, motor de risco e camada de "
    "auditoria) é descrito no Capítulo 4, que também registra o estado de cada componente no protótipo, como "
    "forma de **demonstração**."))
add(("p", "A **avaliação** é experimental e incremental. Na primeira etapa, já concluída, o motor de risco foi "
    "avaliado *offline* sobre um conjunto de dados sintéticos gerado de forma determinística, com divisão "
    "estratificada entre treino e teste, validação cruzada restrita ao treino e análises de sensibilidade e de "
    "evasão (Capítulo 5). Na segunda etapa, a camada de auditoria será avaliada em uma rede Hyperledger Fabric "
    "quanto a latência, vazão e detecção de adulteração (hipótese H3). Na terceira, o sistema integrado será "
    "avaliado com dados reais ou semirreais coletados em ambiente controlado, conforme os princípios de "
    "minimização e pseudonimização da LGPD. A **comunicação** compreende este documento, a dissertação e a "
    "submissão de artigos; uma primeira versão do trabalho foi submetida ao SBESC 2026, e as avaliações recebidas "
    "orientaram a revisão aqui apresentada."))

add(("sec", "Estrutura do texto"))
add(("p", "Além desta introdução, o documento está organizado em cinco capítulos. O Capítulo 2 apresenta a "
    "revisão bibliográfica sobre autenticação, autenticação baseada em risco, aprendizado de máquina para "
    "detecção de anomalias, métricas de avaliação, *blockchain* como mecanismo de auditoria e as tecnologias de "
    "suporte. O Capítulo 3 descreve o protocolo da revisão estruturada e a síntese dos trabalhos relacionados. "
    "O Capítulo 4 detalha a proposta: modelo de ameaças, arquitetura em camadas, critérios, motor de risco, "
    "processo de treinamento, decisão adaptativa, camada de auditoria e estado do protótipo. O Capítulo 5 "
    "apresenta a avaliação e os resultados parciais, discutidos à luz das hipóteses. O Capítulo 6 traz as "
    "considerações finais, os próximos passos e o cronograma."))

# =============================================================================================
add(("cap", "REVISÃO BIBLIOGRÁFICA"))
add(("p", "Este capítulo apresenta os conceitos em que a proposta se apoia: autenticação e controle de acesso, "
    "autenticação contextual e baseada em risco, aprendizado de máquina para detecção de anomalias, métricas de "
    "avaliação de classificadores, *blockchain* como mecanismo de auditoria e as tecnologias que dão suporte à "
    "implementação."))

add(("sec", "Autenticação e controle de acesso"))
add(("p", "A autenticação é o processo de verificar se a identidade alegada por um usuário é legítima antes de "
    "lhe conceder acesso a um recurso (NIST, 2017). As diretrizes de identidade digital do NIST classificam os "
    "fatores de autenticação em três categorias: algo que o usuário sabe (senha), algo que possui (dispositivo ou "
    "*token*) e algo que ele é (característica biométrica). A senha, fator de conhecimento mais difundido, tem "
    "fragilidades amplamente documentadas: é escolhida de forma previsível, reutilizada entre serviços (DAS et "
    "al., 2014) e exposta em vazamentos e campanhas de *phishing* (THOMAS et al., 2017). Bonneau et al. (2012), "
    "ao comparar dezenas de esquemas propostos para substituí-la, concluíram que nenhum supera a senha em todos "
    "os critérios de usabilidade, implantação e segurança, razão pela qual ela continua presente e precisa ser "
    "complementada."))
add(("p", "A autenticação multifator (MFA) combina fatores de categorias distintas e eleva o custo do ataque, "
    "mas, quando exigida de forma estática em todos os acessos, aumenta o atrito e não impede ataques em que o "
    "segundo fator também é obtido, como o *phishing* em tempo real e a troca de *chip* (NIST, 2017). O modelo "
    "de confiança zero (*Zero Trust*) generaliza essa preocupação ao estabelecer que nenhum acesso deve ser "
    "considerado confiável por padrão e que cada solicitação deve ser avaliada com base na identidade, no "
    "dispositivo e no contexto (ROSE et al., 2020). Essa perspectiva fundamenta a autenticação contextual."))

add(("sec", "Autenticação contextual e baseada em risco"))
add(("p", "A autenticação baseada em risco (RBA) avalia, a cada tentativa de *login*, um conjunto de sinais "
    "implícitos do contexto da requisição, como endereço IP, geolocalização, dispositivo, navegador e horário, "
    "para estimar a probabilidade de que o acesso seja legítimo. Se o risco for baixo, o acesso é concedido "
    "apenas com a senha; se for intermediário, solicita-se um fator adicional (*step-up*); se for alto, a "
    "tentativa é bloqueada (WIEFLING; LO IACONO; DÜRMUTH, 2019). Freeman et al. (2016) formalizaram essa "
    "estimativa como um problema estatístico, calculando a verossimilhança de cada atributo do *login* em "
    "relação ao histórico do usuário e à população, com avaliação sobre dados reais do LinkedIn."))
add(("p", "Wiefling, Lo Iacono e Dürmuth (2019) realizaram o primeiro estudo empírico sobre RBA em serviços de "
    "grande porte e observaram que a decisão se apoia predominantemente no endereço IP. Em estudo posterior, "
    "conduzido sobre os registros de *login* de um serviço *online* de grande escala, Wiefling et al. (2023) "
    "mostraram que a escolha e a ponderação dos atributos precisam ser ajustadas a cada serviço para equilibrar "
    "segurança e usabilidade. Do lado do usuário, Wiefling, Dürmuth e Lo Iacono (2020) constataram que a RBA é "
    "percebida como mais usável do que a MFA aplicada a todos os acessos, desde que os desafios sejam raros."))
add(("p", "Os sinais utilizados em RBA podem ser divididos em contextuais e comportamentais. Entre os "
    "contextuais, destaca-se o *fingerprint* do dispositivo, isto é, a combinação de atributos do navegador e do "
    "sistema que identifica um dispositivo com razoável estabilidade (ALACA; VAN OORSCHOT, 2016; ANDRIAMILANTO "
    "et al., 2021). Entre os comportamentais, a biometria comportamental infere a identidade a partir de padrões "
    "de interação, como a dinâmica de digitação (*keystroke dynamics*) e o uso de telas sensíveis ao toque "
    "(STYLIOS et al., 2021; ABUHAMAD et al., 2021). Killourhy e Maxion (2009) estabeleceram uma referência de "
    "avaliação ao comparar quatorze detectores de anomalias aplicados à digitação, e trabalhos mais recentes, "
    "como o TypeNet (ACIEN et al., 2022) e o TypeFormer (STRAGAPEDE et al., 2024), aplicaram aprendizado profundo "
    "a bases com grande número de usuários."))

add(("sec", "Aprendizado de máquina para detecção de anomalias"))
add(("p", "O aprendizado de máquina estuda algoritmos que melhoram seu desempenho em uma tarefa a partir de "
    "exemplos. No aprendizado supervisionado, o modelo é ajustado a partir de exemplos rotulados, isto é, "
    "registros para os quais a classe correta (por exemplo, acesso legítimo ou suspeito) é conhecida; no não "
    "supervisionado, o modelo descobre a estrutura dos dados sem rótulos (HASTIE; TIBSHIRANI; FRIEDMAN, 2009). A "
    "detecção de anomalias busca identificar observações que se desviam do padrão esperado, problema em que os "
    "exemplos anômalos costumam ser raros, heterogêneos e, muitas vezes, desconhecidos no momento do treinamento "
    "(CHANDOLA; BANERJEE; KUMAR, 2009). Três algoritmos são utilizados neste trabalho."))
add(("p", "O *Isolation Forest* (LIU; TING; ZHOU, 2008) é um método não supervisionado que constrói árvores de "
    "partições aleatórias e mede quantas partições são necessárias para isolar cada observação: anomalias, por "
    "serem raras e diferentes, são isoladas com menos partições. O *Random Forest* (BREIMAN, 2001) é um "
    "classificador supervisionado formado por muitas árvores de decisão, cada uma treinada sobre uma amostra "
    "aleatória dos dados e dos atributos, cujas previsões são combinadas por votação; é robusto a relações não "
    "lineares e permite estimar a importância de cada atributo. A rede neural multicamada (*Multilayer "
    "Perceptron*, MLP) é composta por camadas de neurônios artificiais com funções de ativação não lineares, cujos "
    "pesos são ajustados pelo algoritmo de retropropagação do erro (RUMELHART; HINTON; WILLIAMS, 1986), "
    "atualmente combinado a otimizadores como o Adam (KINGMA; BA, 2015) e a ativações ReLU (GOODFELLOW; BENGIO; "
    "COURVILLE, 2016)."))
add(("p", "Um *ensemble* combina as saídas de vários modelos. A combinação tende a superar os modelos "
    "individuais quando estes erram de forma diferente, o que ocorre quando têm vieses distintos (DIETTERICH, "
    "2000). Combinar um detector não supervisionado, sensível a desvios ainda não rotulados, com classificadores "
    "supervisionados, que aprendem padrões de fraude conhecidos, é uma forma de obter essa diversidade."))
add(("p", "O treinamento de um modelo consiste em ajustar seus parâmetros para minimizar o erro sobre os dados "
    "de treino. O risco principal é o sobreajuste (*overfitting*), em que o modelo memoriza particularidades do "
    "treino e generaliza mal para dados novos. Por isso, o desempenho deve ser estimado sobre dados não "
    "utilizados no ajuste, seja por uma partição de teste reservada, seja por validação cruzada, em que os dados "
    "são divididos em *k* partes e o modelo é treinado *k* vezes, cada vez avaliado em uma parte diferente "
    "(KOHAVI, 1995; HASTIE; TIBSHIRANI; FRIEDMAN, 2009). Qualquer etapa ajustada aos dados, como a normalização "
    "dos atributos ou a escolha de um limiar de decisão, deve ser feita apenas com os dados de treino, para "
    "evitar o vazamento de informação do teste (ARP et al., 2022)."))
add(("p", "Em segurança, três dificuldades adicionais são recorrentes. A primeira é o desbalanceamento: ataques "
    "são raros em relação aos acessos legítimos, o que distorce métricas como a acurácia e exige técnicas e "
    "métricas próprias (HE; GARCIA, 2009). A segunda é a deriva de conceito (*concept drift*): o comportamento "
    "dos usuários e dos atacantes muda ao longo do tempo, e os modelos precisam ser monitorados e retreinados "
    "(GAMA et al., 2014). A terceira é a presença de um adversário que se adapta ao detector e manipula os "
    "atributos sob seu controle para evadi-lo (BIGGIO; ROLI, 2018). Sommer e Paxson (2010) e Arp et al. (2022) "
    "apontam, ainda, que avaliações baseadas em dados sintéticos ou pouco representativos tendem a superestimar o "
    "desempenho, o que exige cautela na interpretação dos resultados."))
add(("p", "Por fim, decisões automatizadas que afetam pessoas devem ser explicáveis. Métodos de IA explicável "
    "(XAI), como o SHAP (LUNDBERG; LEE, 2017), atribuem a cada atributo uma contribuição para a previsão de um "
    "modelo, o que permite indicar, para cada bloqueio, quais critérios mais pesaram no escore de risco."))

add(("sec", "Métricas de avaliação de classificadores"))
add(("p", "Em um classificador binário que separa acessos suspeitos (classe positiva) de legítimos, cada "
    "previsão é um verdadeiro positivo (VP), falso positivo (FP), verdadeiro negativo (VN) ou falso negativo "
    "(FN). A partir dessas contagens definem-se a precisão, VP/(VP+FP), que indica a fração de alertas corretos; "
    "a revocação (*recall*), VP/(VP+FN), que indica a fração de ataques detectados; o *F1-score*, média harmônica "
    "entre precisão e revocação; e a taxa de falsos positivos (FPR), FP/(FP+VN), que indica a fração de acessos "
    "legítimos incorretamente sinalizados (FAWCETT, 2006)."))
add(("p", "Quando o modelo produz um escore contínuo, a curva ROC relaciona a revocação à FPR para todos os "
    "limiares possíveis, e a área sob ela (AUC) mede a capacidade de ordenar positivos acima de negativos "
    "independentemente do limiar (FAWCETT, 2006). Sob forte desbalanceamento, a curva de precisão-revocação e "
    "sua área (PR-AUC) são mais informativas, pois a precisão depende diretamente da proporção de ataques "
    "(SAITO; REHMSMEIER, 2015)."))
add(("p", "Como a decisão proposta tem três saídas, e não duas, este trabalho adota também duas métricas "
    "operacionais: a **taxa de atrito**, fração de acessos legítimos que recebem MFA ou bloqueio, e a **taxa de "
    "passagem silenciosa**, fração de ataques liberados sem nenhum desafio. A primeira mede o custo para o "
    "usuário legítimo; a segunda, o risco residual."))

add(("sec", "*Blockchain* como mecanismo de auditoria"))
add(("p", "Uma *blockchain* é um registro distribuído em que as transações são agrupadas em blocos encadeados "
    "por funções de *hash* criptográficas: cada bloco contém o *hash* do anterior, de modo que alterar um registro "
    "invalida todos os blocos seguintes (NAKAMOTO, 2008). A cópia do registro é mantida por vários participantes, "
    "que concordam sobre seu conteúdo por meio de um protocolo de consenso. Nas *blockchains* permissionadas, "
    "apenas participantes identificados e autorizados mantêm o registro, o que as torna adequadas a ambientes "
    "corporativos e governamentais que exigem governança e controle de acesso (YAGA et al., 2018). O Hyperledger "
    "Fabric é uma plataforma permissionada de código aberto em que as transações são executadas por contratos "
    "inteligentes (*chaincode*), endossadas por organizações definidas em uma política e ordenadas por um serviço "
    "de consenso (ANDROULAKI et al., 2018)."))
add(("p", "A escolha da *blockchain* para auditoria deve ser justificada frente a alternativas mais simples. "
    "Registros assinados e encadeados por *hash* já permitem detectar alterações posteriores (SCHNEIER; KELSEY, "
    "1999), e estruturas de dados específicas tornam essa verificação eficiente (CROSBY; WALLACH, 2009). Essas "
    "técnicas bastam quando existe uma autoridade central confiável que custodia o registro. A diferença "
    "essencial da *blockchain* permissionada está em não depender de um único custodiante: o registro é "
    "replicado e validado por várias organizações, de modo que um administrador privilegiado de uma delas não "
    "consegue, sozinho, reescrever a trilha sem que as demais detectem a divergência. Ahmad, Saad e Mohaisen "
    "(2019) aplicaram essa ideia a registros de auditoria com o BlockAudit."))
add(("p", "Essa garantia tem custo: maior complexidade operacional e menor vazão do que um banco de dados com "
    "inserção apenas (*append-only*). O custo se justifica quando as decisões precisam ser verificadas por "
    "terceiros que não confiam no operador, como em fiscalizações de conformidade com a LGPD; em implantações com "
    "um único operador plenamente confiável, mecanismos mais simples podem bastar. Essa delimitação define o "
    "escopo em que a camada de auditoria proposta se faz necessária e será verificada na avaliação da hipótese "
    "H3."))

add(("sec", "Tecnologias de suporte"))
add(("p", "A delegação da autenticação a um provedor de identidade apoia-se em protocolos padronizados. O OAuth "
    "2.0 define como um cliente obtém autorização de acesso a recursos em nome de um usuário (HARDT, 2012); o "
    "OpenID Connect (OIDC) acrescenta a ele uma camada de identidade, permitindo que a aplicação verifique quem é "
    "o usuário autenticado (SAKIMURA et al., 2014); e o *JSON Web Token* (JWT) é o formato compacto e assinado "
    "em que essas informações são transmitidas (JONES; BRADLEY; SAKIMURA, 2015). O Keycloak é um provedor de "
    "identidade de código aberto que implementa esses protocolos e oferece MFA, federação e gestão de sessões "
    "(KEYCLOAK, 2024)."))
add(("p", "A arquitetura de microsserviços decompõe a aplicação em serviços pequenos e independentes, que se "
    "comunicam por interfaces bem definidas e podem ser implantados e escalados separadamente (DRAGONI et al., "
    "2017; NEWMAN, 2021). Esse isolamento permite que o serviço de avaliação de risco evolua, ou seja retreinado, "
    "sem alterar o provedor de identidade. A conteinerização com Docker empacota cada serviço com suas "
    "dependências, o que favorece a reprodutibilidade dos ambientes (MERKEL, 2014). No protótipo, o *back-end* é "
    "implementado em Spring Boot e o *front-end* em Angular; o motor de risco avaliado neste documento foi "
    "implementado em Python com a biblioteca scikit-learn (PEDREGOSA et al., 2011); e a camada de auditoria "
    "será implementada em Hyperledger Fabric (HYPERLEDGER FOUNDATION, 2024)."))

# =============================================================================================
add(("cap", "TRABALHOS RELACIONADOS"))
add(("p", "Este capítulo descreve o protocolo de revisão estruturada adotado para identificar os trabalhos "
    "relacionados e sintetiza os estudos selecionados. O protocolo segue as etapas propostas por Kitchenham e "
    "Charters (2007): definição dos critérios de inclusão e exclusão, busca, seleção, extração dos dados e "
    "síntese. A busca em bases foi complementada por *snowballing* (WOHLIN, 2014), isto é, pela análise das "
    "referências citadas pelos estudos selecionados e dos estudos que os citam."))
add(("p", "A revisão busca responder a três questões: (QP1) quais sinais contextuais e comportamentais são "
    "utilizados para estimar o risco de uma tentativa de *login*?; (QP2) como esses sinais são combinados para "
    "produzir a decisão de acesso e como essa decisão é avaliada?; e (QP3) como as decisões de autenticação são "
    "registradas para fins de auditoria e verificação por terceiros?"))

add(("sec", "Definição dos critérios de inclusão e exclusão"))
add(("p", "Os critérios utilizados para decidir quais estudos compõem a revisão são apresentados na Tabela 1."))
add(("tab", "Critérios de inclusão e exclusão",
     ["Código", "Critério"],
     [["CI1", "Propõe ou avalia autenticação baseada em risco, contextual ou comportamental aplicada ao *login* em serviços *web* ou móveis."],
      ["CI2", "Propõe ou avalia mecanismo de registro à prova de adulteração aplicável a eventos de autenticação ou de controle de acesso."],
      ["CI3", "Publicado como artigo completo em periódico ou evento com revisão por pares."],
      ["CI4", "Escrito em inglês ou português."],
      ["CE1", "Trata exclusivamente de biometria fisiológica (face, impressão digital, íris)."],
      ["CE2", "Trata de detecção de intrusão em rede sem relação com a decisão de autenticação."],
      ["CE3", "Não apresenta avaliação experimental, estudo empírico nem análise comparativa de métodos."],
      ["CE4", "Versão preliminar ou duplicada de estudo já incluído."]],
     [2.0, 13.5], "Elaborado pelo autor (2026)."))
add(("p", "Um estudo é incluído quando atende a CI3, a CI4 e a pelo menos um dentre CI1 e CI2, e excluído quando "
    "se enquadra em qualquer critério de exclusão. Revisões da literatura (*surveys*) que comparam métodos são "
    "incluídas e servem também como fonte de *snowballing*."))

add(("sec", "Busca por estudos relevantes"))
add(("p", "A busca é realizada nas bases IEEE Xplore, ACM Digital Library, Scopus e SpringerLink, que indexam os "
    "principais periódicos e eventos de segurança da computação, no período de 2008 a 2025. O limite inferior "
    "corresponde à publicação do *Isolation Forest*, algoritmo central do motor de risco. A expressão de busca, "
    "aplicada ao título, ao resumo e às palavras-chave e adaptada à sintaxe de cada base, é:"))
add(("cod", "(\"risk-based authentication\" OR \"adaptive authentication\" OR \"contextual authentication\" "
     "OR \"implicit authentication\" OR \"behavioral biometrics\") AND (login OR \"account takeover\" OR "
     "\"web authentication\") AND (\"machine learning\" OR \"anomaly detection\" OR ensemble OR blockchain OR "
     "\"audit log\")"))
add(("p", "Antes da busca nas bases, foi definido um conjunto inicial de estudos de referência (sementes), "
    "conhecido a partir da fundamentação teórica: Wiefling, Lo Iacono e Dürmuth (2019), Freeman et al. (2016), "
    "Killourhy e Maxion (2009) e Ahmad, Saad e Mohaisen (2019). Esses estudos servem para calibrar a expressão de "
    "busca, que deve recuperá-los, e como ponto de partida do *snowballing*."))

add(("sec", "Seleção dos estudos"))
add(("p", "A seleção ocorre em três etapas. Na primeira, são removidas as duplicatas entre bases e lidos o "
    "título e o resumo de cada estudo, aplicando-se os critérios da Tabela 1. Na segunda, os estudos remanescentes "
    "são lidos na íntegra e os critérios são reaplicados. Na terceira, aplica-se o *snowballing* para trás e para "
    "frente sobre os estudos incluídos, até que nenhum estudo novo seja encontrado. A quantidade de estudos "
    "retornados, excluídos e incluídos em cada etapa é registrada, de modo que a revisão possa ser reproduzida."))
add(("p", "Nesta qualificação, a revisão encontra-se em execução. Foram incluídos até o momento os onze estudos "
    "sintetizados na Seção 3.5, obtidos a partir das sementes e do *snowballing*; a execução completa da busca "
    "nas quatro bases, com o registro quantitativo de cada etapa, será concluída até a defesa, conforme o "
    "cronograma (Seção 6.3)."))

add(("sec", "Extração dos dados"))
add(("p", "De cada estudo incluído, extraem-se os campos da Tabela 2, definidos para responder às três questões "
    "de pesquisa e para comparar os estudos com a proposta."))
add(("tab", "Formulário de extração de dados",
     ["Campo", "Descrição", "Questão"],
     [["Sinais", "Sinais contextuais e comportamentais utilizados na estimativa de risco.", "QP1"],
      ["Técnica", "Modelo ou regra que combina os sinais (heurística, estatística, aprendizado de máquina).", "QP2"],
      ["Decisão", "Saídas possíveis (binária, *step-up*, bloqueio) e forma de definir os limiares.", "QP2"],
      ["Avaliação", "Origem dos dados (reais, públicos, sintéticos) e métricas reportadas.", "QP2"],
      ["Auditoria", "Existência e forma de registro verificável das decisões.", "QP3"],
      ["Limitação", "Principal limitação apontada pelos autores ou observada na leitura.", "—"]],
     [2.6, 10.6, 2.3], "Elaborado pelo autor (2026)."))

add(("sec", "Síntese dos resultados"))
add(("p", "A Tabela 3 sintetiza os estudos incluídos até o momento, organizados em quatro eixos: autenticação "
    "baseada em risco, sinais de dispositivo, biometria comportamental e auditoria."))
add(("tab", "Síntese dos trabalhos relacionados",
     ["Estudo", "Sinais", "Decisão", "Auditoria", "Avaliação", "Limitação principal"],
     [["Freeman et al. (2016)", "IP, *user agent*, histórico", "Estatística (verossimilhança)", "Não", "Dados reais (LinkedIn)", "Poucos sinais; sem registro verificável"],
      ["Wiefling, Lo Iacono e Dürmuth (2019)", "Predominantemente IP", "Heurística (*step-up*)", "Não", "Testes em serviços reais", "Sinal único; pouca transparência"],
      ["Wiefling et al. (2023)", "IP, *user agent*, histórico", "Estatística ajustada", "Não", "Dados reais de grande escala", "Ajuste manual por serviço"],
      ["Alaca e van Oorschot (2016)", "*Fingerprint* do dispositivo", "Não decide", "Não", "Análise de métodos", "Sinal isolado; falsificável"],
      ["Andriamilanto et al. (2021)", "*Fingerprint* do navegador", "Não decide", "Não", "Base real de *fingerprints*", "Sinal isolado"],
      ["Killourhy e Maxion (2009)", "Digitação", "Detector de anomalias", "Não", "Base pública", "Fator único; avaliação *offline*"],
      ["Acien et al. (2022)", "Digitação", "Rede neural recorrente", "Não", "Base pública de grande escala", "Modalidade única"],
      ["Stragapede et al. (2024)", "Digitação (móvel)", "*Transformer*", "Não", "Base pública", "Modalidade única"],
      ["Frank et al. (2013)", "Toque em tela", "SVM / *k*-NN", "Não", "Coleta própria", "Restrito a dispositivos móveis"],
      ["Abuhamad et al. (2021)", "Sensores de *smartphone*", "Revisão", "Não", "*Survey*", "Não propõe mecanismo"],
      ["Ahmad, Saad e Mohaisen (2019)", "—", "—", "*Blockchain*", "Protótipo", "Registro desacoplado da decisão"],
      ["**Proposta**", "**14 critérios contextuais e comportamentais**", "***Ensemble* + três faixas**", "***Blockchain* (em projeto)**", "**Dados sintéticos (até o momento)**", "**Dados sintéticos; evasão**"]],
     [3.0, 2.7, 2.5, 1.9, 2.5, 2.9], "Elaborado pelo autor (2026)."))
add(("p", "Em relação à QP1, os estudos de RBA aplicados a serviços reais utilizam poucos sinais, com predomínio "
    "do endereço IP e do *user agent*, enquanto os estudos de *fingerprint* e de biometria comportamental "
    "demonstram o poder discriminativo de sinais mais ricos, mas os tratam de forma isolada. Em relação à QP2, a "
    "decisão é tomada por regras heurísticas ou modelos estatísticos ajustados a cada serviço; nenhum dos estudos "
    "de RBA avaliados combina modelos supervisionados e não supervisionados, e as métricas reportadas raramente "
    "separam o custo para o usuário legítimo (atrito) do risco residual (passagem silenciosa), nem consideram um "
    "atacante que conhece os sinais. Em relação à QP3, apenas o BlockAudit trata do registro verificável, e de "
    "forma desacoplada do mecanismo que decide."))
add(("p", "A síntese evidencia, portanto, a lacuna que motiva este trabalho: nenhum dos estudos reúne "
    "simultaneamente (i) a fusão de múltiplos sinais contextuais e comportamentais, (ii) a decisão proporcional "
    "ao risco por um *ensemble* de aprendizado de máquina e (iii) o registro verificável da decisão, acoplado ao "
    "momento em que ela é tomada. O próximo capítulo descreve a proposta que busca preencher essa lacuna."))

# =============================================================================================
add(("cap", "PROPOSTA"))
add(("p", "Este capítulo descreve a proposta: o modelo de ameaças que orienta o projeto, a arquitetura em "
    "camadas, o fluxo de autenticação, os critérios utilizados, o motor de risco e seu processo de treinamento, "
    "a decisão adaptativa e a camada de auditoria. A última seção registra o estado de cada componente no "
    "protótipo atual, distinguindo o que foi implementado do que está em projeto."))

add(("sec", "Modelo de ameaças"))
add(("p", "O sistema considera um adversário que já possui credenciais válidas da vítima, obtidas por "
    "vazamento, *phishing* ou reutilização de senhas, e que pode tentar o *login* a partir de redes e "
    "dispositivos arbitrários, de forma manual ou automatizada. Assume-se que o adversário não controla o "
    "provedor de identidade, não observa o estado interno dos modelos de risco e não controla a maioria das "
    "organizações que mantêm a rede de auditoria. Admite-se, porém, que ele conheça os critérios utilizados, pois "
    "a segurança não deve depender do sigilo do projeto; esse adversário informado é avaliado explicitamente na "
    "Seção 5.6."))
add(("p", "Sob esse modelo, consideram-se as seguintes classes de ataque:"))
add(("al", [
    "*credential stuffing* e força bruta: tentativas automatizadas com credenciais vazadas, caracterizadas por "
    "frequência elevada, redes atípicas e ausência de padrões humanos de digitação;",
    "viagem impossível: acessos a partir de locais incompatíveis com o tempo decorrido desde o último acesso;",
    "uso de dispositivo desconhecido: acesso com credenciais corretas a partir de dispositivo, sistema "
    "operacional ou navegador diferentes dos habituais;",
    "automação: uso de navegadores sem interface gráfica (*headless*) e de *bots*;",
    "adulteração da trilha de auditoria: tentativa, por um administrador mal-intencionado, de apagar ou alterar "
    "o registro de decisões após o fato.",
]))
add(("p", "Ficam fora do escopo a exploração de vulnerabilidades da própria aplicação, o comprometimento do "
    "provedor de identidade e a engenharia social que leve a vítima a aprovar o segundo fator. Este último ponto "
    "tem uma consequência importante: a faixa de MFA só contém um adversário que não possui o segundo fator; "
    "contra quem o possui, a proteção efetiva é a faixa de bloqueio."))

add(("sec", "Arquitetura em camadas"))
add(("p", "A arquitetura foi reorganizada a partir das avaliações recebidas na submissão ao SBESC 2026. Na versão "
    "anterior, as camadas eram definidas pelas tecnologias empregadas (aplicação, Keycloak, IA, limiares e "
    "*blockchain*), sem explicitar a responsabilidade de cada uma, a interface entre elas nem o que de fato "
    "estava implementado. Na versão atual, cada camada é definida pela pergunta que responde sobre a tentativa de "
    "acesso, pela entrada que recebe, pela saída que produz e pela ameaça que trata, e o estado de cada uma no "
    "protótipo é declarado. A Figura 2 apresenta a arquitetura, e a Tabela 4 detalha cada camada."))
add(("fig", "fig2-arquitetura.png", "Arquitetura em camadas e estado de cada camada no protótipo",
     "Elaborado pelo autor (2026).", 15.5))
add(("tab", "Responsabilidade, interface e ameaça tratada por camada",
     ["Camada", "Entrada → saída", "Ameaça tratada", "Estado no protótipo"],
     [["C1 — Captura de contexto", "Requisição de *login* → vetor com 14 critérios", "Fornece a evidência usada pelas camadas C3 e C4", "Parcial: extrai um conjunto de 14 atributos diferente do avaliado"],
      ["C2 — Identidade", "Credencial → identidade verificada e *token*", "Acesso sem credencial válida", "Parcial: senha, JWT próprio e MFA por TOTP; Keycloak disponível como perfil opcional"],
      ["C3 — Avaliação de risco", "Vetor → escore S ∈ [0, 1]", "Uso de credencial válida em contexto anômalo", "Parcial: modelos avaliados *offline* em Python; versão em Java com outro escore"],
      ["C4 — Decisão adaptativa", "Escore → permitir, exigir MFA ou bloquear", "Atrito excessivo e passagem silenciosa", "Parcial: MFA exigido abaixo de um limiar; sem faixa de bloqueio"],
      ["C5 — Auditoria verificável", "Decisão, *hash* dos critérios e escore → transação confirmada", "Repúdio e adulteração da trilha", "Projetada: registro relacional com encadeamento de *hash*; rede Fabric não integrada"]],
     [3.2, 3.9, 3.7, 4.7], "Elaborado pelo autor (2026)."))
add(("p", "A separação entre as camadas C3 e C4 é deliberada: a estimativa de risco e a política que a converte "
    "em ação podem evoluir de forma independente, de modo que os limiares possam ser recalibrados, ou os modelos "
    "retreinados, sem alterar o provedor de identidade. Já o acoplamento entre C4 e C5 é o que diferencia a "
    "proposta: cada decisão é registrada no momento em que é tomada, junto com sua justificativa (o *hash* dos "
    "critérios e o escore), de modo que um auditor possa verificar se a ação registrada corresponde à política "
    "vigente sem precisar confiar nos registros do operador."))

add(("sec", "Fluxo de autenticação contextual"))
add(("p", "A Figura 3 apresenta, na forma de um diagrama de sequência, o fluxo de uma tentativa de autenticação na "
    "arquitetura-alvo. O usuário submete suas credenciais pelo *front-end*, que as encaminha ao *back-end* "
    "juntamente com os dados de contexto. O *back-end* valida a credencial no Keycloak (C2), extrai os quatorze "
    "critérios (C1) e os envia ao serviço de risco (C3), que devolve o escore. O *back-end* aplica as faixas de "
    "decisão (C4), registra a decisão na rede de auditoria (C5) e só então devolve ao usuário a concessão, a "
    "exigência de MFA ou o bloqueio. A borda tracejada indica que a rede de auditoria ainda não está integrada."))
add(("fig", "fig3-sequencia.png", "Fluxo de autenticação contextual na arquitetura-alvo (diagrama de sequência)",
     "Elaborado pelo autor (2026).", 14.0))

add(("sec", "Critérios contextuais e comportamentais"))
add(("p", "Cada tentativa de *login* é representada por quatorze critérios, organizados em quatro famílias e "
    "selecionados a partir dos sinais identificados na revisão da literatura (Capítulo 3). A Tabela 5 apresenta os "
    "critérios, a ameaça que cada família visa detectar e se o critério pode ser controlado por um atacante que o "
    "conhece, aspecto avaliado na Seção 5.6."))
add(("tab", "Critérios utilizados pelo motor de risco",
     ["Família", "Critérios", "Ameaça visada", "Controlável pelo atacante?"],
     [["Temporal", "(1) hora do acesso; (2) dia da semana; (3) intervalo desde o último *login*; (4) frequência de acessos em 24 h", "*Credential stuffing*, *bots*", "Sim: basta imitar horário e ritmo"],
      ["Geográfica e de rede", "(5) distância do local habitual; (6) tipo de rede; (7) ASN; (8) velocidade de deslocamento", "Viagem impossível, redes atípicas", "Sim, com *proxy* residencial próximo à vítima"],
      ["Dispositivo e navegador", "(9) *fingerprint* do navegador; (10) sistema operacional; (11) tipo de dispositivo; (12) indício de automação", "Dispositivo desconhecido, automação", "Sim, com navegador antidetecção"],
      ["Comportamento de sessão", "(13) padrão de digitação; (14) falhas recentes de *login*", "Força bruta, uso por terceiro", "Parcialmente: a digitação exige amostras da vítima"]],
     [3.0, 5.6, 3.4, 3.5], "Elaborado pelo autor (2026), com base em Wiefling, Lo Iacono e Dürmuth (2019), Alaca e van Oorschot (2016) e Killourhy e Maxion (2009)."))

add(("sec", "Motor de avaliação de risco"))
add(("p", "O motor de risco combina os três algoritmos apresentados na Seção 2.3. O *Isolation Forest* detecta "
    "tentativas cujo vetor de critérios é globalmente raro, sem depender de rótulos, o que o torna sensível a "
    "padrões de ataque ainda não observados. O *Random Forest* e a rede neural MLP aprendem, a partir de exemplos "
    "rotulados, a fronteira entre acessos legítimos e suspeitos. Cada modelo produz um escore entre 0 (normal) e "
    "1 (altamente anômalo), e o escore final *S* é a média ponderada da Equação 1:"))
add(("eq", "S = 0,4 · S_{IF} + 0,3 · S_{RF} + 0,3 · S_{MLP}", "1"))
add(("p", "em que S_{IF}, S_{RF} e S_{MLP} são os escores do *Isolation Forest*, do *Random Forest* e da rede "
    "neural. O peso maior atribuído ao *Isolation Forest* reflete a intenção de valorizar desvios não rotulados; "
    "esses pesos foram fixados *a priori* e sua sensibilidade é analisada na Seção 5.4. A Tabela 6 apresenta os "
    "hiperparâmetros utilizados, também fixados *a priori*, sem busca."))
add(("tab", "Hiperparâmetros do motor de risco",
     ["Componente", "Configuração"],
     [["Pré-processamento", "Padronização (*z-score*) ajustada apenas no treino"],
      ["*Isolation Forest*", "100 árvores; contaminação de 0,05; escore normalizado entre 0 e 1 com os limites observados no treino"],
      ["*Random Forest*", "120 árvores; profundidade livre; pesos de classe balanceados"],
      ["Rede neural (MLP)", "Duas camadas ocultas (64 e 32 neurônios), ativação ReLU, otimizador Adam, 220 iterações, sem *dropout*; saída logística"],
      ["*Ensemble*", "Equação 1 (pesos 0,4, 0,3 e 0,3)"],
      ["Limiar binário (τ)", "Valor entre 0,20 e 0,80 (passo de 0,01) que maximiza o *F1-score* no treino: τ = 0,54"],
      ["Reprodutibilidade", "Semente aleatória fixa (42)"]],
     [3.6, 11.9], "Elaborado pelo autor (2026)."))

add(("sec", "Processo de treinamento do modelo"))
add(("p", "Esta seção descreve como os conceitos de treinamento e validação apresentados na Seção 2.3 foram "
    "aplicados neste trabalho. A Figura 4 resume o processo, implementado em um único *script* que regenera, a "
    "partir de uma semente fixa, o conjunto de dados, os modelos treinados e as métricas reportadas."))
add(("fig", "fig4-treinamento.png", "Processo de treinamento e avaliação do motor de risco",
     "Elaborado pelo autor (2026).", 15.5))
add(("p", "**Dados.** Como não há bases públicas rotuladas para autenticação contextual com os quatorze "
    "critérios, construiu-se um conjunto de dados sintéticos com 50.000 registros de *login*, dos quais 95% são "
    "legítimos e 5% suspeitos. Essa proporção é uma hipótese de trabalho, e não uma medida de produção; por isso, "
    "outras proporções são avaliadas na Seção 5.5. As distribuições de cada critério são constantes escolhidas "
    "para representar um uso corporativo plausível: a hora do acesso segue uma distribuição normal com média de "
    "13 h e desvio-padrão de 3,2 h, truncada entre 0 e 23 h e tratada como variável linear; o intervalo entre "
    "*logins* segue uma distribuição exponencial; a frequência em 24 horas, uma distribuição de Poisson; e a "
    "distância do local habitual, uma distribuição meia-normal. Os critérios de dispositivo, sistema "
    "operacional, ASN e tipo de rede são categóricos, com frequências fixadas no gerador."))
add(("p", "**Rotulagem.** Os registros suspeitos são gerados a partir de um perfil legítimo, alterando-se de dois "
    "a quatro critérios, escolhidos ao acaso, para faixas anômalas, como horário de madrugada, rajada de *logins*, "
    "deslocamento impossível, rede atípica, dispositivo desconhecido, digitação divergente ou indício de "
    "automação. Para que o problema não fosse trivial, cerca de 5% dos registros legítimos também recebem uma "
    "alteração em um único critério, sorteada das mesmas faixas. O rótulo de cada registro é, portanto, o ramo "
    "do gerador que o produziu, sem revisão manual. Por fim, adiciona-se a todos os critérios um ruído gaussiano "
    "com desvio de 0,30 vez o desvio-padrão da coluna. Como esse ruído atinge também os critérios categóricos e "
    "os não negativos, o conjunto contém alguns valores fisicamente implausíveis, limitação analisada na Seção "
    "5.5."))
add(("p", "**Treinamento.** O conjunto é dividido de forma estratificada em 70% para treino e 30% para teste, "
    "preservando a proporção de classes. A padronização dos critérios é ajustada apenas sobre o treino. O "
    "*Isolation Forest* é treinado sem usar os rótulos; o *Random Forest* e a rede neural, com os rótulos. Os "
    "três escores são combinados pela Equação 1, e o limiar τ é escolhido no próprio treino, como o valor que "
    "maximiza o *F1-score*."))
add(("p", "**Validação.** A partição de teste é usada uma única vez, na avaliação final. Para estimar a "
    "variabilidade dos resultados, aplica-se uma validação cruzada estratificada com cinco partições apenas sobre "
    "o treino, reajustando, em cada partição, a padronização, os três modelos e o limiar. Os conjuntos de dados, "
    "os modelos treinados e as métricas são publicados no repositório do projeto."))
add(("p", "Os modelos são treinados uma única vez, de forma *offline*. Retreinamento periódico diante da deriva "
    "de conceito (GAMA et al., 2014) e versionamento dos modelos em produção são requisitos de projeto ainda não "
    "implementados."))

add(("sec", "Decisão adaptativa"))
add(("p", "A camada C4 converte o escore *S* em uma de três ações. Se *S* < 0,3, o acesso é permitido sem etapas "
    "adicionais; se 0,3 ≤ *S* < 0,7, exige-se um segundo fator (MFA); se *S* ≥ 0,7, a tentativa é bloqueada, o "
    "evento é registrado e o administrador é notificado. Os cortes 0,3 e 0,7 são parâmetros de projeto, e não "
    "valores calibrados. Observa-se que o limiar binário τ = 0,54, usado para calcular precisão e revocação, cai "
    "dentro da faixa de MFA; por isso, essas métricas não descrevem sozinhas quantos ataques são liberados ou "
    "bloqueados, e o desempenho por faixa é avaliado separadamente (Seção 5.3)."))

add(("sec", "Camada de auditoria"))
add(("p", "A camada C5 registra, para cada decisão, o identificador pseudonimizado do usuário, o *hash* SHA-256 "
    "dos critérios de entrada (e não os dados brutos, em atenção ao princípio de minimização da LGPD), o escore, "
    "a decisão, a versão do modelo e o instante da transação, encadeados ao registro anterior (Figura 5). O "
    "registro da versão do modelo permite que um auditor reproduza a decisão a partir dos critérios originais, "
    "quando estes lhe forem fornecidos, e verifique se a ação registrada corresponde à política vigente."))
add(("fig", "fig5-registro-auditoria.png", "Estrutura dos registros de decisão na trilha de auditoria",
     "Elaborado pelo autor (2026).", 15.5))
add(("p", "No projeto, os registros são mantidos em uma rede Hyperledger Fabric com ao menos duas organizações, "
    "por exemplo o operador do serviço e um auditor independente, e a política de endosso exige a assinatura de "
    "ambas. Assim, um administrador de uma única organização não consegue reescrever a trilha sem que a outra "
    "detecte a divergência, propriedade que justifica a escolha da *blockchain* frente a um banco *append-only* "
    "(Seção 2.5). A avaliação planejada mede a latência de confirmação, a vazão sob carga e a detecção de uma "
    "adulteração deliberada (hipótese H3)."))

add(("sec", "Estado atual do protótipo"))
add(("p", "A arquitetura descrita neste capítulo é o alvo do projeto. O protótipo público, implementado em "
    "Spring Boot e Angular, contempla até o momento: *login* com senha e emissão de JWT próprio, com integração ao "
    "Keycloak disponível como perfil de configuração opcional; MFA por TOTP, com desafio de uso único vinculado à "
    "verificação de senha; uma versão em Java dos três modelos, treinada na inicialização; e a persistência de "
    "cada decisão em uma tabela relacional (PostgreSQL) com encadeamento de *hash*."))
add(("p", "Três divergências entre o protótipo e a arquitetura-alvo são registradas para orientar as próximas "
    "etapas: (i) os modelos avaliados no Capítulo 5 são os do *pipeline* *offline* em Python, e não os do "
    "protótipo em Java, que usa outro conjunto de quatorze atributos; (ii) no protótipo, o escore do *ensemble* é "
    "combinado a um escore de confiança com outros pesos, e o MFA é exigido abaixo de um limiar único, sem faixa "
    "de bloqueio; e (iii) a camada C5 não está conectada a uma rede Fabric, pois o serviço correspondente é um "
    "simulador, e a verificação de integridade da cadeia de *hash* ainda não é funcional. Alinhar o protótipo à "
    "Equação 1 e às três faixas e implementar a rede Fabric são os próximos passos de implementação (Seção 6.2)."))

# =============================================================================================
add(("cap", "AVALIAÇÃO E RESULTADOS"))
add(("p", "Este capítulo apresenta a avaliação preliminar do motor de risco, realizada *offline* sobre o "
    "conjunto de dados sintéticos descrito na Seção 4.6, e discute os resultados à luz das hipóteses H1 e H2. A "
    "hipótese H3 depende da camada de auditoria e será avaliada na próxima etapa."))

add(("sec", "Questões de avaliação e protocolo"))
add(("p", "A avaliação busca responder às seguintes questões: (QA1) qual é o desempenho do *ensemble* na "
    "classificação de risco?; (QA2) como as três faixas distribuem acessos legítimos e ataques? (H2); (QA3) o "
    "*ensemble* supera cada modelo isolado e quão sensível é aos pesos? (H1); (QA4) como o desempenho varia com "
    "a proporção de ataques?; e (QA5) qual é o efeito de um atacante que conhece os critérios?"))
add(("p", "Todas as análises usam o mesmo *pipeline* e a mesma divisão entre treino e teste descritos na Seção 4.6. "
    "Para a QA3, cada modelo isolado é treinado com seu próprio limiar, escolhido no treino, e os pesos da "
    "Equação 1 são variados em passos de 0,1, gerando 66 combinações, como análise de sensibilidade e não de "
    "seleção. Para a QA4, o conjunto é regenerado com 1%, 2%, 5%, 10% e 20% de ataques. Para a QA5, simula-se "
    "um atacante que substitui os critérios que controla por valores sorteados da distribuição legítima, em "
    "quatro níveis cumulativos: horário e ritmo; rede, ASN e localização (*proxy* residencial); *fingerprint*, "
    "sistema operacional, dispositivo e automação (navegador antidetecção); e padrão de digitação. Os *scripts* "
    "e os resultados estão disponíveis no repositório do projeto."))

add(("sec", "Desempenho do *ensemble*"))
add(("p", "Sobre o conjunto de teste, que corresponde a 30% dos dados e não foi utilizado no treinamento, o "
    "*ensemble* obteve os resultados da Tabela 7. A matriz de confusão é composta por 14.131 verdadeiros "
    "negativos, 119 falsos positivos, 168 falsos negativos e 582 verdadeiros positivos."))
add(("tab", "Desempenho do *ensemble* no conjunto de teste",
     ["Métrica", "Valor"],
     [["Acurácia", "98,1%"], ["Precisão", "83,0%"], ["Revocação (*recall*)", "77,6%"], ["*F1-score*", "80,2%"],
      ["Taxa de falsos positivos (FPR)", "0,8%"], ["AUC-ROC", "0,99"], ["PR-AUC", "0,90"],
      ["*F1-score* — validação cruzada (5 partições, só treino)", "80,3% ± 1,3%"],
      ["AUC — validação cruzada (5 partições, só treino)", "0,990 ± 0,002"]],
     [10.0, 5.5], "Elaborado pelo autor (2026)."))
add(("p", "A AUC de 0,99 indica que o escore ordena quase perfeitamente os registros suspeitos acima dos "
    "legítimos, e a taxa de falsos positivos de 0,8% é baixa, o que é relevante para a usabilidade. A acurácia "
    "de 98,1%, por outro lado, pouco informa, pois um classificador que declarasse todos os acessos legítimos "
    "teria 95% de acurácia; por isso, a discussão se apoia no *F1-score*, na PR-AUC e nas métricas por faixa. A "
    "validação cruzada confirma a ordem de grandeza dos resultados, com limiares entre 0,42 e 0,56 conforme a "
    "partição. A Figura 6 resume as métricas e a latência de inferência."))
add(("fig", "fig6-resultados.png", "Métricas do *ensemble* no conjunto de teste e latência de inferência *offline*",
     "Elaborado pelo autor (2026).", 15.5))

add(("sec", "Decisão em três faixas"))
add(("p", "A Tabela 8 aplica as faixas de decisão ao escore do conjunto de teste."))
add(("tab", "Distribuição das decisões por faixa no conjunto de teste",
     ["Classe real", "Permitir (S < 0,3)", "Exigir MFA", "Bloquear (S ≥ 0,7)"],
     [["Legítimo", "97,3%", "2,6%", "0,1%"], ["Suspeito", "7,3%", "30,1%", "62,5%"]],
     [3.9, 3.9, 3.8, 3.9], "Elaborado pelo autor (2026)."))
add(("p", "Entre os acessos legítimos, 97,3% são liberados sem desafio, o que corresponde a uma taxa de atrito de "
    "2,7%, e apenas 0,1% são bloqueados. Entre os ataques, 62,5% são bloqueados, 30,1% recebem MFA e 7,3% passam "
    "sem desafio. Como a faixa de MFA só contém o adversário que não possui o segundo fator (Seção 4.1), a "
    "proteção efetiva contra quem o possui corresponde à fração bloqueada, de 62,5%, e não à revocação binária."))

add(("sec", "Contribuição de cada modelo e sensibilidade aos pesos"))
add(("p", "A Tabela 9 compara o *ensemble* a cada modelo treinado isoladamente no mesmo *pipeline*."))
add(("tab", "*Ensemble* versus modelos isolados no conjunto de teste",
     ["Modelo", "τ", "Precisão", "Revocação", "*F1*", "FPR", "PR-AUC"],
     [["*Isolation Forest*", "0,45", "0,763", "0,772", "0,767", "1,26%", "0,855"],
      ["*Random Forest*", "0,60", "0,779", "0,771", "0,775", "1,15%", "0,857"],
      ["Rede neural (MLP)", "0,48", "0,757", "0,792", "0,774", "1,34%", "0,860"],
      ["***Ensemble***", "0,54", "**0,830**", "0,776", "**0,802**", "**0,84%**", "**0,903**"]],
     [3.8, 1.4, 2.0, 2.1, 1.8, 1.9, 2.5], "Elaborado pelo autor (2026)."))
add(("p", "O *ensemble* ganha cerca de três pontos de *F1-score* sobre o melhor modelo isolado (0,802 contra "
    "0,775 do *Random Forest*), reduz a taxa de falsos positivos de 1,15% para 0,84% e eleva a PR-AUC de 0,86 "
    "para 0,90. Os pesos adotados, porém, não são os melhores: nas 66 combinações avaliadas, o *F1-score* no teste "
    "varia de 0,767 (apenas *Isolation Forest*) a 0,820, e a configuração 0,4/0,3/0,3 ocupa a 19.ª posição. As "
    "melhores combinações atribuem de 0,6 a 0,8 ao *Isolation Forest* e pouco ou nenhum peso à rede neural. "
    "Escolher os pesos com base nesse resultado seria ajustar o modelo ao teste; a seleção correta, em uma "
    "partição de validação separada, faz parte da próxima etapa."))

add(("sec", "Sensibilidade à proporção de ataques e ao gerador"))
add(("p", "A Tabela 10 repete o experimento para outras proporções de ataques."))
add(("tab", "Sensibilidade à proporção de ataques",
     ["Ataques", "*F1*", "AUC", "PR-AUC", "Atrito", "Passagem silenciosa"],
     [["1%", "0,632", "0,989", "0,718", "0,8%", "24,0%"], ["2%", "0,720", "0,991", "0,809", "1,1%", "16,7%"],
      ["5%", "0,802", "0,992", "0,903", "2,7%", "7,3%"], ["10%", "0,869", "0,992", "0,944", "3,1%", "6,4%"],
      ["20%", "0,917", "0,993", "0,976", "4,2%", "3,7%"]],
     [2.2, 2.2, 2.2, 2.4, 2.4, 4.1], "Elaborado pelo autor (2026)."))
add(("p", "A AUC permanece em 0,99, mas as métricas que dependem da prevalência mudam bastante: com 1% de ataques, "
    "o *F1-score* cai para 0,63 e a passagem silenciosa sobe para 24,0%, porque o *Isolation Forest*, cuja "
    "contaminação acompanha a proporção, e o limiar escolhido por *F1* passam a favorecer a classe majoritária. "
    "Em produção, em que os ataques tendem a ser mais raros que 5%, esse é o cenário relevante. Já a correção do "
    "ruído do gerador, em uma variante sem ruído nos critérios categóricos e de contagem, quase não altera os "
    "resultados (*F1* de 0,809, AUC de 0,992 e passagem silenciosa de 6,9%), o que indica que os valores "
    "implausíveis não inflavam as métricas."))

add(("sec", "Evasão por atacante informado"))
add(("p", "A Tabela 11 mostra o efeito de um atacante que conhece os critérios e imita, de forma cumulativa, os "
    "que estão sob seu controle."))
add(("tab", "Evasão por atacante que conhece os critérios (ataques do conjunto de teste)",
     ["Critérios imitados (cumulativo)", "Revocação", "Permitir", "Exigir MFA", "Bloquear"],
     [["Nenhum", "77,6%", "7,3%", "30,1%", "62,5%"],
      ["+ horário e ritmo", "50,7%", "28,4%", "33,1%", "38,5%"],
      ["+ rede, ASN e localização (*proxy*)", "18,0%", "64,4%", "25,3%", "10,3%"],
      ["+ *fingerprint*, sistema, dispositivo e automação", "2,9%", "91,1%", "8,0%", "0,9%"],
      ["+ padrão de digitação (todos)", "0,3%", "98,1%", "1,9%", "0,0%"]],
     [6.3, 2.2, 2.3, 2.4, 2.3], "Elaborado pelo autor (2026). Revocação no limiar τ = 0,54; demais colunas com as faixas 0,3 e 0,7."))
add(("p", "Imitar apenas o horário e o ritmo das tentativas, algo trivial, já reduz a revocação de 77,6% para "
    "50,7% e eleva a passagem silenciosa de 7,3% para 28,4%. Com um *proxy* residencial próximo à vítima, a "
    "revocação cai para 18,0% e 64,4% dos ataques passam sem desafio; com um navegador antidetecção, praticamente "
    "todos passam. Parte desse efeito decorre do próprio gerador, pois os ataques sintéticos são definidos como "
    "desvios nesses critérios, e removê-los apaga o sinal. O resultado expõe, porém, um limite real da "
    "abordagem: a maioria dos critérios é controlável pelo adversário, e apenas o padrão de digitação e o "
    "histórico da conta resistem a um atacante informado. Critérios difíceis de forjar e defesas contra evasão "
    "são, portanto, requisito da próxima etapa, e não refinamento (BIGGIO; ROLI, 2018)."))

add(("sec", "Latência de inferência"))
add(("p", "A latência foi medida apenas no *pipeline* *offline* (scikit-learn, uma amostra por chamada, mediana "
    "de 300 chamadas): 4,0 ms para o *Isolation Forest*, 29,2 ms para o *Random Forest* e 0,05 ms para a rede "
    "neural, executados em sequência, totalizando cerca de 33 ms. O valor depende da máquina e é dominado pelo "
    "*Random Forest*. A latência do fluxo completo de *login*, o custo do registro de auditoria e a vazão não "
    "foram medidos, pois dependem da integração descrita na Seção 4.9."))

add(("sec", "Discussão"))
add(("p", "Em relação à hipótese **H1**, os resultados são favoráveis: o *ensemble* superou cada modelo isolado "
    "em *F1-score*, taxa de falsos positivos e PR-AUC. O ganho, de cerca de três pontos de *F1*, é consistente "
    "com a expectativa de que modelos com vieses diferentes errem de forma diferente (DIETTERICH, 2000). A análise "
    "de pesos mostra, contudo, que a contribuição da rede neural é pequena e que os pesos precisam ser "
    "selecionados em uma partição de validação."))
add(("p", "Em relação à hipótese **H2**, os resultados também são favoráveis na proporção de 5% de ataques: taxa "
    "de atrito de 2,7%, abaixo da meta de 5%, bloqueio de 62,5% dos ataques e passagem silenciosa de 7,3%, abaixo "
    "da meta de 10%. A hipótese não se sustenta, porém, em dois cenários: com 1% e 2% de ataques, a passagem "
    "silenciosa supera 10%; e, diante de um atacante que imita horário e ritmo, ela chega a 28,4%. A hipótese "
    "H2 é, portanto, parcialmente confirmada, e sua confirmação plena depende de limiares calibrados para baixa "
    "prevalência e de critérios mais difíceis de forjar."))
add(("p", "Os números devem ser lidos como avaliação de viabilidade, e não como desempenho esperado em produção. "
    "Os baselines da Tabela 3 não foram reexecutados sobre os mesmos dados, de modo que não se afirma "
    "superioridade quantitativa sobre eles. Nenhuma afirmação sobre resistência à adulteração é feita nesta "
    "fase, pois a camada de auditoria não foi avaliada."))

add(("sec", "Limitações e ameaças à validade"))
add(("p", "Quatro limitações delimitam o alcance das conclusões. Primeiro, a validade externa é restringida pelo "
    "uso de dados sintéticos, que não capturam a cauda longa de comportamentos legítimos atípicos nem a "
    "criatividade de atacantes reais; as métricas devem ser lidas como limite superior otimista (SOMMER; PAXSON, "
    "2010; ARP et al., 2022). Segundo, a validade de construção depende da rotulagem: como os rótulos derivam das "
    "mesmas regras que geram os ataques, o *ensemble* pode estar reaprendendo as fronteiras codificadas no "
    "gerador, risco que apenas a rotulagem de incidentes reais elimina. Terceiro, a comparação com os baselines é "
    "qualitativa. Quarto, a robustez adversarial é baixa, como mostrou a Tabela 11. Essas ameaças são mitigadas "
    "parcialmente pela fixação das sementes, pela publicação dos hiperparâmetros e pela disponibilização do "
    "código e dos dados; sua superação plena exige a coleta de dados reais sob a arquitetura proposta."))

# =============================================================================================
add(("cap", "CONSIDERAÇÕES FINAIS"))
add(("p", "Este documento apresentou o projeto de um sistema autônomo de autenticação contextual que combina a "
    "estimativa de risco por um *ensemble* de aprendizado de máquina, a decisão proporcional ao risco e o registro "
    "verificável de cada decisão em *blockchain* permissionada. A pesquisa foi delimitada por uma questão e três "
    "hipóteses, conduzida segundo a *Design Science Research* e apoiada por uma revisão estruturada da literatura, "
    "que evidenciou a ausência de trabalhos que reúnam essas três dimensões."))

add(("sec", "Contribuições e resultados parciais"))
add(("p", "Até esta fase, destacam-se como contribuições: (i) uma arquitetura em cinco camadas definidas por "
    "responsabilidade, interface e ameaça tratada, com o estado de cada uma declarado; (ii) um motor de risco "
    "avaliado de forma reprodutível, com código, dados e modelos publicados; e (iii) uma avaliação que vai além "
    "das métricas binárias, reportando atrito, passagem silenciosa, sensibilidade à prevalência e evasão. Os "
    "resultados preliminares, obtidos sobre dados sintéticos, apoiam a hipótese H1 e confirmam parcialmente a "
    "hipótese H2, mas revelam que a maioria dos critérios pode ser imitada por um atacante informado."))

add(("sec", "Próximos passos"))
add(("p", "Até a defesa, pretende-se: implementar a camada de auditoria em uma rede Hyperledger Fabric com ao "
    "menos duas organizações e avaliar a hipótese H3; alinhar o protótipo à Equação 1 e às três faixas de "
    "decisão; concluir a revisão estruturada nas quatro bases; selecionar pesos e limiares em uma partição de "
    "validação, com atenção ao cenário de baixa prevalência; incorporar critérios mais difíceis de forjar e "
    "avaliar defesas contra evasão; substituir os dados sintéticos por dados reais ou semirreais, coletados em "
    "ambiente controlado conforme a LGPD; e avaliar a explicabilidade das decisões com SHAP e a usabilidade "
    "percebida pelos usuários."))

add(("sec", "Cronograma"))
add(("p", "A Tabela 12 apresenta as atividades concluídas e as planejadas até a defesa da dissertação."))
add(("tab", "Cronograma de atividades",
     ["Atividade", "Período", "Situação"],
     [["Definição da arquitetura e desenvolvimento do *back-end* e do *front-end*", "mai./2025 – nov./2025", "Concluída"],
      ["Avaliação preliminar do motor de risco (dados sintéticos)", "dez./2025 – fev./2026", "Concluída"],
      ["Submissão de artigo ao SBESC 2026", "jul./2026", "Concluída (não aceito)"],
      ["Revisão do artigo e novos experimentos a partir das avaliações", "set./2026 – out./2026", "Concluída"],
      ["Revisão do documento de qualificação e exame de qualificação", "nov./2026 – dez./2026", "Em andamento"],
      ["Execução completa da revisão estruturada nas bases", "dez./2026 – jan./2027", "Planejada"],
      ["Implementação da rede Hyperledger Fabric e avaliação da H3", "dez./2026 – fev./2027", "Planejada"],
      ["Alinhamento do protótipo à Equação 1 e às três faixas", "jan./2027 – fev./2027", "Planejada"],
      ["Critérios difíceis de forjar, defesas contra evasão e seleção de pesos", "fev./2027 – abr./2027", "Planejada"],
      ["Coleta de dados semirreais e avaliação do sistema integrado", "mar./2027 – mai./2027", "Planejada"],
      ["Submissão de artigo revisado", "mai./2027", "Planejada"],
      ["Escrita da dissertação", "mar./2027 – jun./2027", "Planejada"],
      ["Registro de *software*", "jun./2027", "Planejada"],
      ["Defesa da dissertação", "jul./2027", "Planejada"]],
     [9.0, 3.6, 2.9], "Elaborado pelo autor (2026)."))

# =============================================================================================
REFERENCIAS = [
    "ABUHAMAD, M.; ABUSNAINA, A.; NYANG, D.; MOHAISEN, D. Sensor-based continuous authentication of smartphones' users using behavioral biometrics: a contemporary survey. **IEEE Internet of Things Journal**, v. 8, n. 1, p. 65-84, 2021.",
    "ACIEN, A.; MORALES, A.; VERA-RODRIGUEZ, R.; FIERREZ, J.; MONACO, J. V. TypeNet: deep learning keystroke biometrics. **IEEE Transactions on Biometrics, Behavior, and Identity Science**, v. 4, n. 1, p. 57-70, 2022.",
    "AHMAD, A.; SAAD, M.; MOHAISEN, A. Secure and transparent audit logs with BlockAudit. **Journal of Network and Computer Applications**, v. 145, art. 102406, 2019.",
    "ALACA, F.; VAN OORSCHOT, P. C. Device fingerprinting for augmenting web authentication: classification and analysis of methods. In: ANNUAL COMPUTER SECURITY APPLICATIONS CONFERENCE, 32., 2016, Los Angeles. **Proceedings** [...]. New York: ACM, 2016. p. 289-301.",
    "ANDRIAMILANTO, N.; ALLARD, T.; LE GUELVOUIT, G.; GAREL, A. A large-scale empirical analysis of browser fingerprints properties for web authentication. **ACM Transactions on the Web**, v. 16, n. 1, p. 1-62, 2021.",
    "ANDROULAKI, E. et al. Hyperledger Fabric: a distributed operating system for permissioned blockchains. In: EUROSYS CONFERENCE, 13., 2018, Porto. **Proceedings** [...]. New York: ACM, 2018. p. 1-15.",
    "ARP, D. et al. Dos and don'ts of machine learning in computer security. In: USENIX SECURITY SYMPOSIUM, 31., 2022, Boston. **Proceedings** [...]. Berkeley: USENIX Association, 2022.",
    "BIGGIO, B.; ROLI, F. Wild patterns: ten years after the rise of adversarial machine learning. **Pattern Recognition**, v. 84, p. 317-331, 2018.",
    "BONNEAU, J.; HERLEY, C.; VAN OORSCHOT, P. C.; STAJANO, F. The quest to replace passwords: a framework for comparative evaluation of web authentication schemes. In: IEEE SYMPOSIUM ON SECURITY AND PRIVACY, 2012, San Francisco. **Proceedings** [...]. Los Alamitos: IEEE, 2012. p. 553-567.",
    "BRASIL. Lei nº 13.709, de 14 de agosto de 2018. Lei Geral de Proteção de Dados Pessoais (LGPD). **Diário Oficial da União**, Brasília, DF, 15 ago. 2018. Disponível em: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm. Acesso em: 11 nov. 2026.",
    "BREIMAN, L. Random forests. **Machine Learning**, v. 45, n. 1, p. 5-32, 2001.",
    "CHANDOLA, V.; BANERJEE, A.; KUMAR, V. Anomaly detection: a survey. **ACM Computing Surveys**, v. 41, n. 3, p. 1-58, 2009.",
    "CROSBY, S. A.; WALLACH, D. S. Efficient data structures for tamper-evident logging. In: USENIX SECURITY SYMPOSIUM, 18., 2009, Montreal. **Proceedings** [...]. Berkeley: USENIX Association, 2009.",
    "DAS, A.; BONNEAU, J.; CAESAR, M.; BORISOV, N.; WANG, X. The tangled web of password reuse. In: NETWORK AND DISTRIBUTED SYSTEM SECURITY SYMPOSIUM, 2014, San Diego. **Proceedings** [...]. Reston: Internet Society, 2014.",
    "DIETTERICH, T. G. Ensemble methods in machine learning. In: INTERNATIONAL WORKSHOP ON MULTIPLE CLASSIFIER SYSTEMS, 2000, Cagliari. **Proceedings** [...]. Berlin: Springer, 2000. p. 1-15. (Lecture Notes in Computer Science, v. 1857).",
    "DOERFLER, P. et al. Evaluating login challenges as a defense against account takeover. In: THE WEB CONFERENCE, 2019, San Francisco. **Proceedings** [...]. New York: ACM, 2019.",
    "DRAGONI, N. et al. Microservices: yesterday, today, and tomorrow. In: MAZZARA, M.; MEYER, B. (ed.). **Present and ulterior software engineering**. Cham: Springer, 2017. p. 195-216.",
    "FAWCETT, T. An introduction to ROC analysis. **Pattern Recognition Letters**, v. 27, n. 8, p. 861-874, 2006.",
    "FRANK, M.; BIEDERT, R.; MA, E.; MARTINOVIC, I.; SONG, D. Touchalytics: on the applicability of touchscreen input as a behavioral biometric for continuous authentication. **IEEE Transactions on Information Forensics and Security**, v. 8, n. 1, p. 136-148, 2013.",
    "FREEMAN, D.; JAIN, S.; DÜRMUTH, M.; BIGGIO, B.; GIACINTO, G. Who are you? A statistical approach to measuring user authenticity. In: NETWORK AND DISTRIBUTED SYSTEM SECURITY SYMPOSIUM, 2016, San Diego. **Proceedings** [...]. Reston: Internet Society, 2016.",
    "GAMA, J.; ŽLIOBAITĖ, I.; BIFET, A.; PECHENIZKIY, M.; BOUCHACHIA, A. A survey on concept drift adaptation. **ACM Computing Surveys**, v. 46, n. 4, p. 1-37, 2014.",
    "GOODFELLOW, I.; BENGIO, Y.; COURVILLE, A. **Deep learning**. Cambridge: MIT Press, 2016.",
    "HARDT, D. (ed.). **The OAuth 2.0 authorization framework**. RFC 6749. Fremont: Internet Engineering Task Force, 2012. Disponível em: https://www.rfc-editor.org/rfc/rfc6749. Acesso em: 11 nov. 2026.",
    "HASTIE, T.; TIBSHIRANI, R.; FRIEDMAN, J. **The elements of statistical learning**: data mining, inference, and prediction. 2. ed. New York: Springer, 2009.",
    "HE, H.; GARCIA, E. A. Learning from imbalanced data. **IEEE Transactions on Knowledge and Data Engineering**, v. 21, n. 9, p. 1263-1284, 2009.",
    "HEVNER, A. R.; MARCH, S. T.; PARK, J.; RAM, S. Design science in information systems research. **MIS Quarterly**, v. 28, n. 1, p. 75-105, 2004.",
    "HYPERLEDGER FOUNDATION. **Hyperledger Fabric documentation**: versão 2.5. San Francisco: Linux Foundation, 2024. Disponível em: https://hyperledger-fabric.readthedocs.io. Acesso em: 11 nov. 2026.",
    "IBM. **Cost of a data breach report 2024**. Armonk: IBM Security, 2024. Disponível em: https://www.ibm.com/reports/data-breach. Acesso em: 11 nov. 2026.",
    "JONES, M.; BRADLEY, J.; SAKIMURA, N. **JSON Web Token (JWT)**. RFC 7519. Fremont: Internet Engineering Task Force, 2015. Disponível em: https://www.rfc-editor.org/rfc/rfc7519. Acesso em: 11 nov. 2026.",
    "KEYCLOAK. **Keycloak documentation**: server administration guide. Versão 24. Raleigh: Red Hat, 2024. Disponível em: https://www.keycloak.org/documentation. Acesso em: 11 nov. 2026.",
    "KILLOURHY, K. S.; MAXION, R. A. Comparing anomaly-detection algorithms for keystroke dynamics. In: IEEE/IFIP INTERNATIONAL CONFERENCE ON DEPENDABLE SYSTEMS AND NETWORKS, 2009, Lisboa. **Proceedings** [...]. Los Alamitos: IEEE, 2009. p. 125-134.",
    "KINGMA, D. P.; BA, J. Adam: a method for stochastic optimization. In: INTERNATIONAL CONFERENCE ON LEARNING REPRESENTATIONS, 3., 2015, San Diego. **Proceedings** [...]. 2015.",
    "KITCHENHAM, B.; CHARTERS, S. **Guidelines for performing systematic literature reviews in software engineering**. Keele: Keele University; Durham: Durham University, 2007. (EBSE Technical Report EBSE-2007-01).",
    "KOHAVI, R. A study of cross-validation and bootstrap for accuracy estimation and model selection. In: INTERNATIONAL JOINT CONFERENCE ON ARTIFICIAL INTELLIGENCE, 14., 1995, Montreal. **Proceedings** [...]. San Francisco: Morgan Kaufmann, 1995. p. 1137-1143.",
    "LIU, F. T.; TING, K. M.; ZHOU, Z.-H. Isolation forest. In: IEEE INTERNATIONAL CONFERENCE ON DATA MINING, 8., 2008, Pisa. **Proceedings** [...]. Los Alamitos: IEEE, 2008. p. 413-422.",
    "LUNDBERG, S. M.; LEE, S.-I. A unified approach to interpreting model predictions. In: ADVANCES IN NEURAL INFORMATION PROCESSING SYSTEMS, 30., 2017, Long Beach. **Proceedings** [...]. Red Hook: Curran Associates, 2017. p. 4765-4774.",
    "MERKEL, D. Docker: lightweight Linux containers for consistent development and deployment. **Linux Journal**, v. 2014, n. 239, art. 2, 2014.",
    "NAKAMOTO, S. **Bitcoin**: a peer-to-peer electronic cash system. 2008. Disponível em: https://bitcoin.org/bitcoin.pdf. Acesso em: 11 nov. 2026.",
    "NATIONAL INSTITUTE OF STANDARDS AND TECHNOLOGY (NIST). **Digital identity guidelines**: authentication and lifecycle management. Gaithersburg: NIST, 2017. (NIST Special Publication 800-63B). DOI: https://doi.org/10.6028/NIST.SP.800-63b.",
    "NEWMAN, S. **Building microservices**: designing fine-grained systems. 2. ed. Sebastopol: O'Reilly Media, 2021.",
    "PEDREGOSA, F. et al. Scikit-learn: machine learning in Python. **Journal of Machine Learning Research**, v. 12, p. 2825-2830, 2011.",
    "PEFFERS, K.; TUUNANEN, T.; ROTHENBERGER, M. A.; CHATTERJEE, S. A design science research methodology for information systems research. **Journal of Management Information Systems**, v. 24, n. 3, p. 45-77, 2007.",
    "ROSE, S.; BORCHERT, O.; MITCHELL, S.; CONNELLY, S. **Zero trust architecture**. Gaithersburg: NIST, 2020. (NIST Special Publication 800-207). DOI: https://doi.org/10.6028/NIST.SP.800-207.",
    "RUMELHART, D. E.; HINTON, G. E.; WILLIAMS, R. J. Learning representations by back-propagating errors. **Nature**, v. 323, p. 533-536, 1986.",
    "SAITO, T.; REHMSMEIER, M. The precision-recall plot is more informative than the ROC plot when evaluating binary classifiers on imbalanced datasets. **PLoS ONE**, v. 10, n. 3, e0118432, 2015.",
    "SAKIMURA, N.; BRADLEY, J.; JONES, M.; DE MEDEIROS, B.; MORTIMORE, C. **OpenID Connect Core 1.0**. [S. l.]: OpenID Foundation, 2014. Disponível em: https://openid.net/specs/openid-connect-core-1_0.html. Acesso em: 11 nov. 2026.",
    "SCHNEIER, B.; KELSEY, J. Secure audit logs to support computer forensics. **ACM Transactions on Information and System Security**, v. 2, n. 2, p. 159-176, 1999.",
    "SOMMER, R.; PAXSON, V. Outside the closed world: on using machine learning for network intrusion detection. In: IEEE SYMPOSIUM ON SECURITY AND PRIVACY, 2010, Oakland. **Proceedings** [...]. Los Alamitos: IEEE, 2010. p. 305-316.",
    "STRAGAPEDE, G.; DELGADO-SANTOS, P.; TOLOSANA, R.; VERA-RODRIGUEZ, R.; GUEST, R.; MORALES, A. TypeFormer: transformers for mobile keystroke biometrics. **Neural Computing and Applications**, v. 36, p. 18531-18545, 2024.",
    "STYLIOS, I.; KOKOLAKIS, S.; THANOU, O.; CHATZIS, S. Behavioral biometrics & continuous user authentication on mobile devices: a survey. **Information Fusion**, v. 66, p. 76-99, 2021.",
    "THOMAS, K. et al. Data breaches, phishing, or malware? Understanding the risks of stolen credentials. In: ACM CONFERENCE ON COMPUTER AND COMMUNICATIONS SECURITY, 2017, Dallas. **Proceedings** [...]. New York: ACM, 2017. p. 1421-1434.",
    "VERIZON. **2025 Data breach investigations report**. Basking Ridge: Verizon Business, 2025. Disponível em: https://www.verizon.com/business/resources/reports/dbir/. Acesso em: 11 nov. 2026.",
    "WIEFLING, S.; DÜRMUTH, M.; LO IACONO, L. More than just good passwords? A study on usability and security perceptions of risk-based authentication. In: ANNUAL COMPUTER SECURITY APPLICATIONS CONFERENCE, 36., 2020, Austin. **Proceedings** [...]. New York: ACM, 2020.",
    "WIEFLING, S.; JØRGENSEN, P. R.; THUNEM, S.; LO IACONO, L. Pump up password security! Evaluating and enhancing risk-based authentication on a real-world large-scale online service. **ACM Transactions on Privacy and Security**, v. 26, n. 1, 2023.",
    "WIEFLING, S.; LO IACONO, L.; DÜRMUTH, M. Is this really you? An empirical study on risk-based authentication applied in the wild. In: IFIP INTERNATIONAL CONFERENCE ON ICT SYSTEMS SECURITY AND PRIVACY PROTECTION, 34., 2019, Lisboa. **Proceedings** [...]. Cham: Springer, 2019. p. 134-148.",
    "WOHLIN, C. Guidelines for snowballing in systematic literature studies and a replication in software engineering. In: INTERNATIONAL CONFERENCE ON EVALUATION AND ASSESSMENT IN SOFTWARE ENGINEERING, 18., 2014, London. **Proceedings** [...]. New York: ACM, 2014. p. 1-10.",
    "YAGA, D.; MELL, P.; ROBY, N.; SCARFONE, K. **Blockchain technology overview**. Gaithersburg: NIST, 2018. (NISTIR 8202). DOI: https://doi.org/10.6028/NIST.IR.8202.",
]
