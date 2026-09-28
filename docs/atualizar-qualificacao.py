# -*- coding: utf-8 -*-
# Aplica ao documento de qualificacao as correcoes da revisao pos-SBESC 2026:
# texto alinhado ao codigo e ao script, resultados novos (experimentos-revisao.json)
# e a Figura 5 regenerada. Gera um arquivo NOVO; o original fica intacto.
#   python atualizar-qualificacao.py
import re, zipfile, os
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "doc-qualificacao-ifba-REVISADO.docx")
DST = os.path.join(HERE, "doc-qualificacao-ifba-REVISADO-v2.docx")
FIG5 = os.path.join(HERE, "figura5-resultados.png")

zin = zipfile.ZipFile(SRC)
xml = zin.read("word/document.xml").decode("utf-8")
spans = [(m.start(), m.end()) for m in re.finditer(r"<w:p[ >].*?</w:p>", xml, re.S)]
P = [xml[a:b] for a, b in spans]


def texto(i):
    return "".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", P[i]))


def ppr(i):
    m = re.search(r"<w:pPr>.*?</w:pPr>", P[i], re.S)
    return m.group(0) if m else ""


def rpr(i):
    m = re.search(r"<w:r>(?:<w:rPr>(.*?)</w:rPr>)?", P[i], re.S)
    return (m.group(1) or "") if m else ""


def run(t, props=""):
    return '<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % (
        "<w:rPr>%s</w:rPr>" % props if props else "", escape(t))


def par(modelo, t):
    """Paragrafo com a formatacao do paragrafo `modelo`. '**X.** resto' vira negrito + normal."""
    props = rpr(modelo)
    m = re.match(r"\*\*(.+?)\*\*(.*)$", t, re.S)
    corpo = run(m.group(1), "<w:b/>" + props) + run(m.group(2), props) if m else run(t, props)
    return "<w:p>%s%s</w:p>" % (ppr(modelo), corpo)


# ------------------------------------------------------------------ tabelas
def tabela(linhas, pesos):
    total = 9064
    ws = [int(total * p / sum(pesos)) for p in pesos]
    grid = "".join('<w:gridCol w:w="%d"/>' % w for w in ws)

    def cel(t, w, neg):
        rp = "<w:b/><w:sz w:val=\"18\"/>" if neg else "<w:sz w:val=\"18\"/>"
        return ('<w:tc><w:tcPr><w:tcW w:w="%d" w:type="dxa"/></w:tcPr><w:p>%s</w:p></w:tc>'
                % (w, run(t, rp)))

    trs = "".join("<w:tr>%s</w:tr>" % "".join(cel(t, w, k == 0 or l[0].startswith("*"))
                                               for t, w in zip([c.lstrip("*") for c in l], ws))
                  for k, l in enumerate(linhas))
    borda = "".join('<w:%s w:val="single" w:sz="4" w:space="0" w:color="999999"/>' % b
                    for b in ("top", "left", "bottom", "right", "insideH", "insideV"))
    return ('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders>%s</w:tblBorders>'
            '<w:tblLook w:val="04A0" w:firstRow="1" w:lastRow="0" w:firstColumn="1" w:lastColumn="0" '
            'w:noHBand="0" w:noVBand="1"/></w:tblPr><w:tblGrid>%s</w:tblGrid>%s</w:tbl>' % (borda, grid, trs))


CAP = 588   # legenda "Tabela 2 — ..."
BODY = 622  # paragrafo de corpo (espacamento 1,5, recuo, justificado)
LISTA = 537  # item de lista
VAZIO = 611


def legenda(t):
    return par(CAP, t)


def espaco():
    return P[VAZIO]


# ------------------------------------------------------------------ operacoes
SUB, APOS, APAGAR = {}, {}, set()


def sub(i, novo, confere=None):
    if confere:
        assert confere in texto(i), (i, confere, texto(i)[:80])
    SUB[i] = par(i, novo)


def apos(i, *blocos):
    APOS.setdefault(i, []).extend(blocos)


ATUAL = {}


def troca(i, a, b):
    t = ATUAL.get(i, texto(i))
    assert a in t, (i, a)
    ATUAL[i] = t.replace(a, b)
    SUB[i] = par(i, ATUAL[i])


# ---- Resumo / Abstract
troca(69, "com acurácia de 98,1% e F1-score de 80,6% na classificação de risco —, validando a metodologia adotada; tais números são parciais e deverão ser confirmados com dados reais nas próximas etapas.",
      "com acurácia de 98,1% e F1-score de 80,2% na classificação de risco, e o ensemble superou cada modelo isolado —; a análise de evasão, contudo, mostrou que a maior parte dos critérios pode ser imitada por um atacante informado. Tais números são parciais, decorrem de dados sintéticos e deverão ser confirmados com dados reais nas próximas etapas.")
troca(82, "achieving 98.1% accuracy and an 80.6% F1-score in risk classification —, validating the adopted methodology; these figures are partial and shall be confirmed with real data in future stages.",
      "achieving 98.1% accuracy and an 80.2% F1-score in risk classification, with the ensemble outperforming every single model —; the evasion analysis, however, showed that most features can be mimicked by an informed attacker. These figures are partial, come from synthetic data, and shall be confirmed with real data in future stages.")

# ---- Cap. 1 e 2
troca(194, "o presente sistema utiliza Blockchain como camada de confiança", "o presente projeto propõe utilizar Blockchain como camada de confiança")
troca(251, "Por isso registra-se de forma imutável", "Por isso, propõe-se registrar de forma imutável")

# ---- Cap. 3
troca(312, "Composta por camadas densas com ativação ReLU e regularização por dropout", "Composta por camadas densas com ativação ReLU e otimizador Adam, sem dropout")
troca(312, "uma camada de saída com ativação sigmoid", "uma camada de saída logística")
troca(314, "Esta combinação proporciona um sistema mais robusto do que qualquer modelo individual.",
      "Nos experimentos da Seção 5.3, essa combinação superou cada modelo isolado em cerca de 3 pontos de F1 (Tabela 5); os pesos 0,4/0,3/0,3, porém, foram fixados a priori e não são os melhores entre as combinações avaliadas.")
troca(321, "(1) hora do acesso, normalizada em escala circular para capturar a periodicidade do comportamento; (2) dia da semana, codificado como variável categórica; (3) intervalo desde o último login, medido em segundos;",
      "(1) hora do acesso (tratada nesta fase como variável linear; a codificação circular, que captura a periodicidade do comportamento, é melhoria prevista); (2) dia da semana; (3) intervalo desde o último login;")
sub(342, "A quarta etapa é a implantação em produção. Na arquitetura proposta, o modelo validado recebe os 14 critérios comportamentais de cada nova tentativa de login e calcula o score de risco em tempo real, com meta de latência inferior a 200 milissegundos, classificando cada tentativa sem intervenção humana. Nesta fase do projeto, os modelos avaliados são executados em um pipeline offline e reprodutível (Seção 5.2); sua integração ao fluxo de login do protótipo é etapa seguinte.", "quarta etapa")
sub(344, "Um aspecto previsto para o sistema é o re-treinamento periódico. Os padrões de comportamento dos usuários evoluem naturalmente ao longo do tempo: um colaborador pode mudar de cidade, trocar de dispositivo ou alterar seus horários de trabalho. Este fenômeno, conhecido na literatura como concept drift (Gama et al., 2014), exige que os modelos sejam atualizados para manter sua eficácia. O re-treinamento periódico e o versionamento dos modelos, com possibilidade de rollback, são requisitos de projeto ainda não implementados.", "re-treinamento")
sub(350, "O treinamento do ensemble é realizado, nesta fase preliminar, sobre um conjunto de dados sintéticos (Seção 5.2). O Isolation Forest é treinado de forma não supervisionada, enquanto o Random Forest e a rede neural utilizam rótulos. Cabe explicitar que o rótulo de cada registro é o ramo do gerador que o produziu (legítimo ou perturbado); não houve revisão manual por especialistas, o que introduz uma circularidade discutida na Seção 5.4.", "semi-supervisionada")
sub(352, "O dataset de treinamento é composto por 50.000 registros com os 14 critérios comportamentais. A proporção de 95% de acessos legítimos e 5% suspeitos foi fixada como hipótese de trabalho, e não medida em ambiente de produção; a sensibilidade dos resultados a outras proporções é avaliada na Seção 5.3.", "90 dias")
sub(354, "O pré-processamento consiste na padronização (z-score) dos critérios, ajustada apenas na partição de treino; como o gerador não produz variáveis textuais nem valores ausentes, não há codificação one-hot nem imputação. Os hiperparâmetros foram fixados a priori (Tabela 3). A variabilidade é estimada por validação cruzada estratificada de 5 folds restrita à partição de treino, reajustando normalizador, modelos e limiar em cada fold.", "one-hot")
sub(356, "O re-treinamento periódico e sob demanda, disparado por métricas de monitoramento que indiquem degradação (concept drift), bem como o registro versionado dos modelos, permanecem como requisitos de projeto para as próximas etapas.", "semanalmente")
sub(361, "O sistema de decisão autônoma opera com base em três faixas de risco definidas pelos limiares do score do ensemble. Os cortes 0,3 e 0,7 são parâmetros de projeto, e não valores calibrados; o desempenho de cada faixa é medido na Seção 5.3 (Tabela 4), por meio da taxa de atrito (acessos legítimos desafiados ou bloqueados) e da taxa de passagem silenciosa (ataques liberados sem desafio).", "calibrados empiricamente")
apos(367, par(367, "No protótipo atual, a decisão em três faixas ainda não está ligada ao fluxo de login: o backend combina o escore do ensemble a um escore de confiança e exige MFA quando esse escore é inferior a 0,5, sem faixa de bloqueio. Alinhar o protótipo às três faixas é etapa seguinte do desenvolvimento."))
sub(372, "O pipeline de inferência foi projetado para operar em tempo real, com meta de latência de 200 milissegundos por requisição de autenticação. Na arquitetura proposta, o serviço de IA é exposto por uma API REST que recebe o vetor de 14 critérios e retorna o score de risco juntamente com a decisão recomendada.", "FastAPI")
sub(374, "No fluxo proposto, o backend Spring Boot intercepta uma tentativa de login, extrai os 14 critérios comportamentais do contexto da requisição HTTP e os envia ao serviço de IA, onde passam pelo mesmo pré-processamento utilizado no treinamento. No protótipo atual, esse serviço ainda não está implantado: o cliente HTTP existe no backend, mas a inferência é feita por implementações próprias dos modelos em Java.", "intercepta")
sub(376, "Os três componentes do ensemble são avaliados sequencialmente: o Isolation Forest calcula o score de anomalia, o Random Forest realiza a classificação probabilística e a rede neural gera sua predição, sendo os três scores combinados pela média ponderada. No pipeline offline, a latência medida foi de 4,0 ms para o Isolation Forest, 29,2 ms para o Random Forest e 0,05 ms para a rede neural, totalizando cerca de 33 ms por requisição (Figura 5).", "inferência paralela")
sub(378, "Está previsto um mecanismo de fallback que preserve a disponibilidade do sistema em caso de falha do serviço de IA, aplicando regras heurísticas simplificadas baseadas em critérios como geolocalização e histórico de falhas; no protótipo, esse mecanismo existe apenas em um caminho de código que ainda não é utilizado pelo fluxo de login.", "fallback")
apos(389, par(389, "Estado da implementação. A camada descrita nesta seção é o alvo do projeto. No protótipo atual, o serviço de integração com o Hyperledger Fabric é um simulador (as dependências do SDK estão desativadas e nenhuma rede Fabric foi implantada); o chaincode de registro existe no repositório, mas não é invocado. Cada decisão é persistida em uma tabela relacional (PostgreSQL) com encadeamento de hashes, cuja verificação de integridade ainda não é funcional. Enquanto a rede real não for implantada, nenhuma propriedade de imutabilidade é reivindicada, e o registro permanece sob controle de um único custodiante — exatamente o cenário que a Seção 2.4 considera insuficiente."))
troca(397, "O registro imutável em blockchain garante o não repúdio e a integridade da trilha de auditoria, impedindo",
      "O registro imutável em blockchain, uma vez implantado, deverá garantir o não repúdio e a integridade da trilha de auditoria, impedindo")
apos(398, par(398, "Dois limites do modelo de ameaças foram evidenciados pela avaliação. Primeiro, a faixa de MFA só contém um adversário que não detém o segundo fator; um adversário com credenciais e acesso ao segundo fator (por exemplo, por phishing em tempo real) só é contido pela faixa de bloqueio. Segundo, um atacante que conhece os 14 critérios pode imitar a maior parte deles — horário, ritmo das tentativas, rede e localização (por meio de proxy), fingerprint e dispositivo (por meio de navegador anti-detecção) —, o que reduz drasticamente a detecção (Tabela 7). Critérios difíceis de forjar, como o padrão de digitação e o histórico da conta, e defesas contra evasão são, portanto, requisitos do projeto."))

# ---- Cap. 4
troca(420, "O Keycloak atua como provedor de identidade", "Na arquitetura proposta, o Keycloak atua como provedor de identidade")
apos(420, par(420, "No protótipo atual, a integração com o Keycloak existe como perfil de configuração opcional, não utilizado na implantação; a autenticação é feita pelo próprio backend, com senha, emissão de JWT e MFA por TOTP."))
troca(430, "Cada decisão de acesso gera um hash criptográfico armazenado na blockchain", "Na proposta, cada decisão de acesso gera um hash criptográfico armazenado na blockchain")
apos(448, par(448, "Estado atual do protótipo. As cinco camadas descrevem o alvo do projeto. O protótipo público (Spring Boot, Angular e PostgreSQL, implantado com Docker) implementa hoje: login com senha e JWT próprios, MFA por TOTP, versões em Java dos três modelos (treinadas na inicialização) e a persistência das decisões em banco relacional. Três divergências são explicitadas: (i) os modelos avaliados no Capítulo 5 são os do pipeline offline em Python, e não os do protótipo Java, que usa outro conjunto de 14 atributos; (ii) no protótipo, o escore do ensemble ainda é combinado a um escore de confiança e o MFA é exigido quando esse escore é inferior a 0,5, sem faixa de bloqueio; e (iii) a Camada 5 não está conectada a uma rede Fabric. Resolver essas divergências é a próxima etapa de implementação."))
sub(457, "A containerização foi implementada com Docker (docker-compose), com integração e implantação contínuas por meio do GitHub Actions em um servidor virtual. A orquestração com Kubernetes não foi adotada nesta fase.", "Kubernetes")
APAGAR.add(461)  # paragrafo repetido sobre Docker/Kubernetes
troca(459, "adotou-se blockchain permissionada baseada em Hyperledger Fabric", "optou-se, no projeto, por blockchain permissionada baseada em Hyperledger Fabric")
sub(463, "Os modelos de risco avaliados foram implementados em Python com a biblioteca Scikit-learn, em um pipeline offline e reprodutível que gera o conjunto de dados, treina os modelos e calcula as métricas; sua exposição como serviço REST consumido pelo backend está prevista. No protótipo, o backend utiliza implementações próprias dos modelos em Java.", "Scikit-learn")
troca(470, "O backend valida a identidade no Keycloak", "Na arquitetura proposta, o backend valida a identidade no Keycloak")
troca(477, "O fluxo implementado compreende", "O fluxo proposto compreende")
sub(492, "Na arquitetura proposta, o serviço de IA é exposto como um microsserviço independente, consumido pelo backend por meio de uma API REST: a cada login, os 14 critérios extraídos são enviados ao modelo, que retorna o escore de risco utilizado na decisão de acesso. Nesta fase, a avaliação dos modelos foi feita no pipeline offline descrito na Seção 5.2.", "microsserviço")
troca(498, "aqui descreve-se sua implementação no ambiente do sistema.", "aqui descreve-se o seu estado no ambiente do sistema: a integração com o Fabric ainda é simulada, e as decisões são persistidas em banco relacional (Seção 3.7).")
troca(507, "sendo periodicamente reentreinado para acompanhar", "com re-treinamento periódico previsto para acompanhar")
sub(526, "Os experimentos foram conduzidos em um pipeline offline em Python (Scikit-learn), executado localmente a partir de uma semente fixa, que gera o conjunto de dados sintéticos, treina os modelos e calcula todas as métricas.", "Kubernetes")
sub(528, "As tentativas de acesso suspeitas são geradas perturbando critérios de perfis legítimos para faixas anômalas — horário de madrugada, rajadas de tentativas, distância e velocidade de deslocamento impossíveis, dispositivo e rede desconhecidos, automação e falhas recentes —, conforme descrito na Seção 5.2.", "credential stuffing")
apos(537, par(LISTA, "Área sob a curva precisão-recall (PR-AUC), mais informativa sob desbalanceamento de classes;"),
     par(LISTA, "Taxa de atrito e taxa de passagem silenciosa da decisão em três faixas;"),
     par(LISTA, "Robustez frente a um atacante que conhece os critérios (evasão);"))
troca(538, "Tempo de resposta do fluxo completo de autenticação.", "Tempo de resposta do fluxo completo de autenticação (planejado; ainda não medido).")

# ---- Cap. 5
sub(571, "A avaliação desta fase foi conduzida em um pipeline offline e reprodutível, escrito em Python com Scikit-learn, que gera o conjunto de dados sintéticos, treina os três modelos, calibra o limiar de decisão e calcula as métricas a partir de uma semente fixa. O código, o conjunto de dados, os modelos treinados e os arquivos de métricas estão publicados no repositório do projeto.", "Kubernetes")
sub(573, "Em paralelo, o protótipo da arquitetura foi implementado com: (1) backend Spring Boot com APIs RESTful protegidas por JWT e MFA por TOTP; (2) frontend Angular; (3) banco PostgreSQL; e (4) implantação com Docker e GitHub Actions. A integração com o Keycloak existe como perfil opcional, a camada Hyperledger Fabric é simulada e não há stack de monitoramento (ELK/Grafana) implantada; esses componentes são etapas seguintes.", "ELK Stack")
sub(577, "Os experimentos foram planejados em três eixos: (i) avaliação do modelo de Machine Learning para classificação de acessos legítimos e anômalos; (ii) teste da cadeia de autenticação contextual integrando identidade, dados contextuais e decisão automatizada; e (iii) verificação da integridade e imutabilidade dos registros na Blockchain. Nesta fase, apenas o eixo (i) foi avaliado; os eixos (ii) e (iii) dependem da integração do protótipo descrita no Capítulo 4.", "três eixos")
sub(579, "O modelo de ML foi treinado com um conjunto de dados sintéticos de tentativas de login, correspondente ao ensemble descrito no Capítulo 3 (Isolation Forest, Random Forest e rede neural). Além do desempenho no conjunto de teste, foram avaliados: a decisão em três faixas; cada modelo isolado; 66 combinações de pesos; proporções de ataques de 1% a 20%; uma variante corrigida do gerador; e a evasão por um atacante que conhece os critérios.", "OpenID Connect")
sub(581, "**Fonte e rotulagem dos dados.** Diante da ausência de bases públicas rotuladas para autenticação contextual, foi construído um conjunto de dados sintéticos com 50.000 tentativas de login, com proporção de 95% de acessos legítimos e 5% suspeitos fixada como hipótese de trabalho. Cada registro contém os 14 critérios descritos na Seção 3.2. As distribuições do gerador são constantes escolhidas pelos autores (por exemplo, hora do acesso gaussiana com média às 13 h, truncada entre 0 e 23), e não parâmetros estimados de dados reais. Os registros suspeitos são obtidos perturbando de dois a quatro critérios de um perfil legítimo; cerca de 5% dos legítimos recebem uma perturbação em um critério, sorteada das mesmas faixas dos ataques; por fim, adiciona-se ruído gaussiano (0,30 desvio-padrão). O rótulo de cada registro é o ramo do gerador que o produziu, sem revisão por especialistas.", "Fonte e rotulagem")
sub(583, "**Divisão dos dados e validação.** O conjunto foi particionado de forma estratificada em 70% para treinamento e 30% para teste. O único parâmetro ajustado é o limiar de decisão, escolhido na partição de treino como o que maximiza o F1 (0,54). A validação cruzada estratificada de 5 folds foi executada apenas sobre a partição de treino, reajustando normalizador, modelos e limiar em cada fold; a partição de teste é reservada à avaliação final.", "Divisão dos dados")
sub(584, "**Métricas de avaliação.** O desempenho foi medido por acurácia, precisão, revocação (recall), F1-score, taxa de falsos positivos, área sob a curva ROC (AUC) e área sob a curva precisão-recall (PR-AUC), além da latência de inferência. Para a decisão em três faixas, definem-se a taxa de atrito (acessos legítimos que recebem MFA ou bloqueio) e a taxa de passagem silenciosa (ataques liberados sem desafio).", "Métricas de avaliação")
sub(585, "**Reprodutibilidade.** Fixou-se a semente dos geradores aleatórios, e os hiperparâmetros são publicados na Tabela 3. Um único script regenera o conjunto de dados, os modelos e as métricas; diferenças de versão do Scikit-learn alteram apenas a terceira casa decimal.", "Reprodutibilidade")
apos(585, espaco(), legenda("Tabela 3 — Hiperparâmetros utilizados (fixados a priori)"),
     tabela([["Componente", "Configuração"],
             ["Pré-processamento", "Padronização (z-score) ajustada no treino"],
             ["Isolation Forest", "100 árvores; contaminação 0,05; escore normalizado (min-max) pelo treino"],
             ["Random Forest", "120 árvores; profundidade livre; pesos de classe balanceados"],
             ["Rede neural (MLP)", "Camadas ocultas 64 e 32, ReLU, Adam, 220 iterações, sem dropout"],
             ["Ensemble", "0,4 × IF + 0,3 × RF + 0,3 × rede neural"],
             ["Limiar", "Grade de 0,20 a 0,80 (passo 0,01); máximo F1 no treino: 0,54"],
             ["Dados", "50.000 registros; 5% suspeitos; ruído 0,30 σ; semente 42"]], [1, 3]),
     espaco())
troca(610, "Decisão adaptativa em 4 níveis, explicável e auditável", "Decisão em três faixas; dados sintéticos; auditoria ainda em projeto")
sub(619, "A Figura 5 resume, no painel à esquerda, as métricas do ensemble sobre o conjunto de teste e, no painel à direita, a latência de inferência de cada modelo e do ensemble, medida no pipeline offline (mediana de 300 chamadas com uma amostra por vez). Os três modelos são avaliados em sequência, de modo que a latência do ensemble (cerca de 33 ms) é aproximadamente a soma das latências individuais, dominada pelo Random Forest. Reforça-se que os valores decorrem de dados sintéticos e caracterizam uma avaliação preliminar, e não um estudo de campo.", "Figura 5 resume")
sub(622, "Sobre o conjunto de teste (30% dos dados, não vistos durante o treinamento), o ensemble alcançou acurácia de 98,1%, precisão de 83,0% e recall de 77,6% na detecção de acessos suspeitos, com F1-score de 80,2%, AUC de 0,99 e PR-AUC de 0,90. A taxa de falsos positivos foi de 0,8%. A validação cruzada restrita ao treino confirmou a ordem de grandeza (F1 de 80,3% ± 1,3%; AUC de 0,990 ± 0,002). A matriz de confusão no teste é de 14.131 verdadeiros negativos, 119 falsos positivos, 168 falsos negativos e 582 verdadeiros positivos.", "Os resultados parciais")
sub(624, "**Decisão em três faixas.** A Tabela 4 aplica as faixas 0,3/0,7 ao escore do teste. Entre os acessos legítimos, 97,3% são liberados sem desafio (taxa de atrito de 2,7%). Entre os ataques, 62,5% são bloqueados, 30,1% recebem MFA e 7,3% passam silenciosamente. Como a faixa de MFA só contém um adversário sem o segundo fator, a proteção efetiva contra quem detém credenciais e segundo fator é a fração bloqueada, e não o recall binário.", "cadeia de autenticação")
apos(624,
     espaco(), legenda("Tabela 4 — Decisão em três faixas no conjunto de teste"),
     tabela([["Classe real", "Permitir (S < 0,3)", "MFA", "Bloquear (S ≥ 0,7)"],
             ["Legítimo", "97,3%", "2,6%", "0,1%"],
             ["Suspeito", "7,3%", "30,1%", "62,5%"]], [2, 2, 1, 2]),
     espaco(),
     par(BODY, "**Contribuição de cada modelo e dos pesos.** A Tabela 5 compara o ensemble a cada modelo treinado isoladamente no mesmo pipeline. O ensemble ganha cerca de 3 pontos de F1 sobre o melhor modelo isolado (0,802 contra 0,775 do Random Forest), reduz a taxa de falsos positivos de 1,15% para 0,84% e eleva a PR-AUC de 0,86 para 0,90. Os pesos 0,4/0,3/0,3, contudo, não foram otimizados: entre 66 combinações avaliadas, o F1 no teste varia de 0,767 (apenas Isolation Forest) a 0,820, e a configuração adotada fica em 19.º lugar; as melhores combinações atribuem de 0,6 a 0,8 ao Isolation Forest e pouco ou nenhum peso à rede neural. Escolher os pesos por esse resultado seria ajustar ao teste; a seleção correta, em partição de validação, é etapa seguinte."),
     espaco(), legenda("Tabela 5 — Ensemble versus modelos isolados (conjunto de teste)"),
     tabela([["Modelo", "Limiar", "Precisão", "Recall", "F1", "FPR", "PR-AUC"],
             ["Isolation Forest", "0,45", "0,763", "0,772", "0,767", "1,26%", "0,855"],
             ["Random Forest", "0,60", "0,779", "0,771", "0,775", "1,15%", "0,857"],
             ["Rede neural", "0,48", "0,757", "0,792", "0,774", "1,34%", "0,860"],
             ["*Ensemble", "0,54", "0,830", "0,776", "0,802", "0,84%", "0,903"]], [3, 1, 1, 1, 1, 1, 1]),
     espaco(),
     par(BODY, "**Sensibilidade à proporção de ataques e ao gerador.** A Tabela 6 repete o experimento para outras proporções. A AUC permanece em 0,99, mas as métricas dependentes da prevalência mudam bastante: com 1% de ataques, o F1 cai para 0,63 e a passagem silenciosa sobe para 24,0%. Em produção, em que ataques tendem a ser mais raros que 5%, esse é o cenário relevante. Já uma variante do gerador sem ruído nas variáveis categóricas — que elimina valores fisicamente implausíveis, como velocidade de deslocamento negativa — quase não altera os resultados (F1 de 0,809 e AUC de 0,992)."),
     espaco(), legenda("Tabela 6 — Sensibilidade à proporção de ataques"),
     tabela([["Ataques", "F1", "AUC", "PR-AUC", "Atrito", "Passagem silenciosa"],
             ["1%", "0,632", "0,989", "0,718", "0,8%", "24,0%"],
             ["2%", "0,720", "0,991", "0,809", "1,1%", "16,7%"],
             ["5%", "0,802", "0,992", "0,903", "2,7%", "7,3%"],
             ["10%", "0,869", "0,992", "0,944", "3,1%", "6,4%"],
             ["20%", "0,917", "0,993", "0,976", "4,2%", "3,7%"]], [1, 1, 1, 1, 1, 2]),
     espaco(),
     par(BODY, "**Evasão adversarial.** A Tabela 7 mostra o efeito de um atacante que conhece os critérios e substitui os que controla por valores típicos de usuários legítimos. Imitar apenas o horário e o ritmo das tentativas reduz o recall de 77,6% para 50,7%; com um proxy residencial próximo à vítima, o recall cai para 18,0% e 64,4% dos ataques passam sem desafio; com um navegador anti-detecção, praticamente todos passam. O resultado é, em parte, consequência do gerador, pois os ataques sintéticos são definidos como desvios nesses critérios; mas também expõe um limite real da abordagem: a maior parte dos 14 critérios é controlável pelo adversário."),
     espaco(), legenda("Tabela 7 — Evasão por atacante que conhece os critérios (ataques do teste)"),
     tabela([["Critérios imitados (cumulativo)", "Recall", "Permitir", "MFA", "Bloquear"],
             ["Nenhum", "77,6%", "7,3%", "30,1%", "62,5%"],
             ["+ horário e ritmo", "50,7%", "28,4%", "33,1%", "38,5%"],
             ["+ rede, ASN e localização (proxy)", "18,0%", "64,4%", "25,3%", "10,3%"],
             ["+ fingerprint, SO, dispositivo, automação", "2,9%", "91,1%", "8,0%", "0,9%"],
             ["+ padrão de digitação (todos)", "0,3%", "98,1%", "1,9%", "0,0%"]], [4, 1, 1, 1, 1]),
     espaco(),
     par(BODY, "Quanto à cadeia de autenticação, não foram medidos nesta fase a latência do fluxo completo de login, o custo do registro de auditoria nem a vazão, pois dependem da integração do protótipo e da rede Fabric descritas no Capítulo 4."))
sub(628, "Os resultados parciais sugerem a viabilidade técnica do motor de risco; contudo, por decorrerem de dados sintéticos e de uma fase preliminar, não permitem ainda confirmar a hipótese central de forma conclusiva. A comparação com os baselines (Tabela 2) é qualitativa, e a validação com dados reais permanece necessária antes de qualquer afirmação quantitativa de superioridade. Em ambiente controlado, o ensemble separou os registros perturbados dos legítimos com AUC de 0,99, o que indica que os critérios escolhidos carregam sinal quando os ataques se manifestam nas faixas modeladas pelo gerador — ressalva que a análise de evasão torna concreta. Soma-se a isso a circularidade da rotulagem: como os rótulos derivam das mesmas regras que descrevem os ataques no gerador, o ensemble pode estar reaprendendo essas regras, risco que apenas dados reais eliminam.", "promissores")
sub(630, "Quanto à camada Blockchain, nenhuma afirmação sobre resistência à adulteração é feita nesta fase, pois a integração com uma rede real ainda não foi realizada. A avaliação planejada exige uma rede com ao menos duas organizações e mede a latência de commit, a vazão sob carga e a detecção de uma adulteração deliberada.", "Blockchain")
troca(637, "(cerca de 19 ms por requisição)", "(cerca de 33 ms por requisição no pipeline offline)")
sub(639, "A terceira diz respeito à complexidade operacional da rede Hyperledger Fabric (configuração de peers, orderers e MSP), que levou a manter a integração simulada nesta fase. A quarta refere-se à conformidade com a LGPD no contexto de coleta e processamento de dados contextuais dos usuários, exigindo definição cuidadosa de políticas de minimização e anonimização de dados pessoais antes da persistência em Blockchain.", "terceira")
apos(639, par(639, "Por fim, a submissão de um artigo ao SBESC 2026, não aceito, trouxe avaliações detalhadas: um dos revisores executou o código publicado e apontou divergências entre o texto e o artefato — em especial, a simulação da camada Fabric e a diferença entre o modelo avaliado e o implementado no protótipo. As correções e os experimentos adicionais incorporados a este documento decorrem dessas avaliações."))
sub(643, "Os objetivos propostos foram parcialmente atingidos nesta fase de qualificação. A arquitetura foi definida e o protótipo implementa parte de seus componentes (backend, frontend, MFA e modelos em Java), enquanto a integração com a rede Fabric e o alinhamento do protótipo ao modelo avaliado permanecem pendentes. Os experimentos preliminares, conduzidos com dados sintéticos, indicaram a viabilidade técnica do motor de risco, com acurácia de 98,1% e F1-score de 80,2%, e o ensemble superou cada modelo isolado; esses resultados constituem uma validação metodológica e não substituem a avaliação com dados reais.", "parcialmente atingidos")
sub(644, "Como contribuições parciais, destacam-se: (i) a proposta de acoplar, em um único fluxo, a decisão adaptativa por IA e sua auditoria em blockchain permissionada, com justificativa explícita de quando essa escolha se faz necessária; (ii) a definição de um conjunto de quatorze critérios contextuais e comportamentais; (iii) a formulação de um modelo de ameaças; e (iv) uma avaliação offline reprodutível, com código, dados e hiperparâmetros publicados, incluindo a análise da decisão em três faixas e da evasão adversarial.", "contribuições parciais")
sub(645, "Reconhecem-se, contudo, limitações importantes. A principal é o uso de dados sintéticos, com rotulagem derivada do próprio gerador, que não capturam toda a variabilidade do comportamento humano real. A segunda é a baixa robustez adversarial: a maior parte dos critérios pode ser imitada por um atacante informado. Somam-se a isso a camada Fabric ainda simulada e a necessidade de avaliar o desempenho do sistema sob carga realista.", "limitações importantes")
sub(646, "Como trabalhos futuros, até a defesa da dissertação, pretende-se: implantar uma rede Hyperledger Fabric com ao menos duas organizações, medindo latência de commit, vazão e detecção de adulteração; alinhar o protótipo ao ensemble avaliado e às três faixas de decisão; selecionar pesos e limiares em partição de validação; incorporar critérios difíceis de forjar e defesas contra evasão; substituir os dados sintéticos por dados reais ou semirreais coletados em ambiente de teste; e avaliar o desempenho e a escalabilidade sob carga. O cronograma detalhado dessas atividades é apresentado no capítulo a seguir.", "trabalhos futuros")

# ---- Cronograma
sub(686, "Artigo SBESC 2026 (submetido; não aceito em 24/09/2026; versão revisada em andamento)", "Publicação de artigo")
sub(687, "Em andamento", "Concluído")
sub(688, "24/09/2026", "05/03/2026")

# ------------------------------------------------------------------ montagem
out, pos = [], 0
for i, (a, b) in enumerate(spans):
    out.append(xml[pos:a])
    if i not in APAGAR:
        out.append(SUB.get(i, P[i]))
    out.extend(APOS.get(i, []))
    pos = b
out.append(xml[pos:])
novo = "".join(out)

with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        dados = zin.read(item.filename)
        if item.filename == "word/document.xml":
            dados = novo.encode("utf-8")
        elif item.filename == "word/media/image6.png":  # Figura 5
            dados = open(FIG5, "rb").read()
        zout.writestr(item, dados)
print("ok:", DST, "| substituidos", len(SUB), "| inseridos apos", len(APOS), "| apagados", len(APAGAR))
