import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.shared import Pt, Cm, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH
from lxml import etree

DOCX_PATH = 'doc-mestrado/doc-qualificacao-ifba.docx'
doc = Document(DOCX_PATH)
body = doc.element.body
numbering = doc.part.numbering_part.numbering_definitions._numbering

# ============================================================
# CHAPTER CONTENT - MOTOR DE INTELIGÊNCIA ARTIFICIAL
# ============================================================

chapter_title = "MOTOR DE INTELIGÊNCIA ARTIFICIAL: ARQUITETURA, TREINAMENTO E DECISÃO"
chapter_box_desc = "Neste capítulo será apresentado em profundidade o motor de Inteligência Artificial, incluindo a arquitetura do ensemble de modelos, os 14 critérios comportamentais, o processo de treinamento com dados reais, os limiares de decisão e a integração com blockchain para auditoria."

subcapitulos = [
    ("Arquitetura do Ensemble de Modelos", [
        "O motor de Inteligência Artificial proposto neste trabalho fundamenta-se em uma arquitetura de ensemble que combina três algoritmos de aprendizado de máquina complementares: Isolation Forest, Random Forest e Deep Learning (rede neural multicamada). A escolha dessa abordagem visa maximizar a capacidade de detecção de comportamentos anômalos enquanto minimiza falsos positivos, um requisito crítico em sistemas de autenticação.",
        "O Isolation Forest atua como primeiro nível de análise, sendo particularmente eficaz na detecção de outliers em espaços multidimensionais. Sua contribuição ao score final corresponde a 40% do peso total, refletindo sua importância na identificação rápida de padrões claramente anômalos. O algoritmo opera isolando observações através de partições aleatórias, onde anomalias são identificadas por requererem menos partições para serem isoladas.",
        "O Random Forest constitui o segundo componente do ensemble, contribuindo com 30% do peso final. Este classificador opera através de múltiplas árvores de decisão treinadas em subconjuntos aleatórios dos dados, proporcionando robustez contra overfitting e capacidade de capturar relações não-lineares entre os critérios comportamentais. Sua natureza interpretável permite auditoria das decisões tomadas.",
        "A rede neural multicamada (Deep Learning) complementa o ensemble com os 30% restantes do peso. Composta por camadas densas com ativação ReLU e regularização por dropout, esta rede é capaz de aprender representações complexas dos padrões comportamentais que escapam aos métodos tradicionais. A arquitetura inclui uma camada de entrada com 14 neurônios (correspondendo aos critérios), duas camadas ocultas de 64 e 32 neurônios respectivamente, e uma camada de saída com ativação sigmoid.",
        "O score final do ensemble é calculado como a média ponderada: S = 0.4 × S_IF + 0.3 × S_RF + 0.3 × S_DL, onde cada componente retorna um valor normalizado entre 0.0 (comportamento normal) e 1.0 (comportamento altamente anômalo). Esta combinação proporciona um sistema mais robusto do que qualquer modelo individual.",
    ]),
    ("Critérios Comportamentais (14 Dimensões)", [
        "O sistema de autenticação contextual extrai 14 critérios comportamentais de cada tentativa de login, formando um vetor multidimensional que alimenta o ensemble de modelos. Estes critérios foram selecionados com base na literatura de segurança comportamental e na capacidade de distinguir usuários legítimos de atacantes.",
        "Os critérios temporais incluem: (1) hora do acesso, normalizada em escala circular para capturar a periodicidade do comportamento; (2) dia da semana, codificado como variável categórica; (3) intervalo desde o último login, medido em segundos; e (4) frequência de acessos nas últimas 24 horas. Estes critérios permitem identificar acessos em horários atípicos ou padrões de frequência incomuns.",
        "Os critérios geográficos e de rede compreendem: (5) geolocalização por IP, convertida em distância geodésica do local habitual; (6) tipo de rede (residencial, corporativa, VPN, Tor); (7) provedor de serviço de internet (ASN); e (8) velocidade de deslocamento impossível, calculada entre o último acesso e o atual. Estes critérios são particularmente eficazes contra ataques originados de localizações geográficas suspeitas.",
        "Os critérios de dispositivo e navegador incluem: (9) fingerprint do navegador, computado via hash de características como User-Agent, resolução de tela e plugins instalados; (10) sistema operacional; (11) tipo de dispositivo (desktop, mobile, tablet); e (12) presença de automação ou headless browser. A mudança de dispositivo habitual é um forte indicador de comprometimento de credenciais.",
        "Os critérios comportamentais de sessão são: (13) padrão de digitação, analisando intervalos entre teclas durante a inserção de credenciais; e (14) histórico de falhas recentes, contabilizando tentativas mal-sucedidas nos últimos 30 minutos. Estes critérios capturam tanto biometria comportamental quanto ataques de força bruta.",
    ]),
    ("Processo de Treinamento com Dados Reais", [
        "O treinamento do ensemble é realizado utilizando dados reais coletados durante a operação do sistema em ambiente de homologação. O pipeline de treinamento segue uma abordagem semi-supervisionada, onde o Isolation Forest é treinado de forma não-supervisionada enquanto o Random Forest e a rede neural utilizam labels gerados a partir de regras heurísticas validadas por especialistas em segurança.",
        "O dataset de treinamento é composto por registros de autenticação que incluem os 14 critérios comportamentais normalizados, totalizando aproximadamente 50.000 registros coletados ao longo de 90 dias de operação. A distribuição inclui aproximadamente 95% de acessos legítimos e 5% de acessos suspeitos ou maliciosos, refletindo a distribuição real em ambientes de produção.",
        "O pré-processamento dos dados inclui normalização min-max para critérios numéricos, codificação one-hot para variáveis categóricas e tratamento de valores ausentes por imputação baseada na mediana. A validação cruzada estratificada com 5 folds é utilizada para avaliar o desempenho do ensemble, garantindo que a proporção de classes seja mantida em cada partição.",
        "O re-treinamento do modelo ocorre de forma periódica (semanalmente) e sob demanda quando métricas de monitoramento indicam degradação do desempenho (concept drift). O sistema mantém um registro versionado de todos os modelos treinados, permitindo rollback em caso de regressão de desempenho.",
    ]),
    ("Limiares de Decisão Autônoma", [
        "O sistema de decisão autônoma opera com base em três faixas de risco definidas pelos limiares do score do ensemble. Estes limiares foram calibrados empiricamente para otimizar o equilíbrio entre segurança e usabilidade, minimizando tanto falsos positivos (usuários legítimos bloqueados) quanto falsos negativos (atacantes permitidos).",
        "A faixa de baixo risco (score < 0.3) resulta em autenticação permitida sem etapas adicionais. Nesta faixa, o comportamento do usuário é consistente com seu perfil histórico em todos os critérios avaliados. Aproximadamente 85% dos acessos legítimos caem nesta categoria, proporcionando uma experiência de autenticação transparente.",
        "A faixa de risco moderado (0.3 ≤ score < 0.7) aciona a autenticação multifator (MFA) como camada adicional de verificação. O sistema solicita um segundo fator de autenticação (código por e-mail, aplicativo autenticador ou biometria) antes de conceder acesso. Esta faixa captura situações ambíguas como acesso de novo dispositivo ou localização incomum.",
        "A faixa de alto risco (score ≥ 0.7) resulta em bloqueio automático da tentativa de autenticação. O evento é registrado na blockchain para auditoria, uma notificação é enviada ao administrador de segurança e a conta pode ser temporariamente suspensa após múltiplos bloqueios consecutivos. Esta faixa é acionada em cenários de alta probabilidade de ataque.",
    ]),
    ("Pipeline de Inferência em Tempo Real", [
        "O pipeline de inferência foi projetado para operar em tempo real, com latência máxima de 200 milissegundos por requisição de autenticação. A arquitetura utiliza o framework FastAPI em Python, expondo uma API REST que recebe o vetor de 14 critérios e retorna o score de risco juntamente com a decisão recomendada.",
        "O fluxo de inferência inicia quando o backend Spring Boot intercepta uma tentativa de login e extrai os 14 critérios comportamentais do contexto da requisição HTTP. Estes critérios são então enviados ao serviço de IA via chamada HTTP síncrona, onde passam pelo mesmo pipeline de pré-processamento utilizado durante o treinamento.",
        "O serviço de IA mantém os três modelos do ensemble carregados em memória, permitindo inferência paralela nos três componentes. O Isolation Forest calcula o score de anomalia, o Random Forest realiza a classificação probabilística e a rede neural gera sua predição independentemente. Os três scores são então combinados pela fórmula de média ponderada para produzir o score final.",
        "Um mecanismo de fallback garante a disponibilidade do sistema mesmo em caso de falha do serviço de IA. Quando o serviço não está acessível, o backend aplica regras heurísticas simplificadas baseadas em critérios como geolocalização e histórico de falhas, garantindo que o processo de autenticação não seja interrompido.",
    ]),
    ("Integração com Blockchain para Auditoria", [
        "Cada decisão de autenticação tomada pelo motor de IA é registrada de forma imutável na blockchain, criando uma trilha de auditoria completa e verificável. O registro inclui o hash SHA-3 dos critérios de entrada, o score de risco calculado, a decisão tomada (permitido, MFA ou bloqueado) e o timestamp da transação.",
        "A integração utiliza uma blockchain permissionada baseada em Hyperledger Besu, configurada com o algoritmo de consenso IBFT 2.0 (Istanbul Byzantine Fault Tolerance). Esta escolha prioriza a finalidade transacional e o throughput necessário para suportar o volume de autenticações em tempo real, mantendo as garantias de imutabilidade e integridade.",
        "O smart contract responsável pelo registro implementa uma estrutura de dados que associa cada evento de autenticação ao hash do bloco anterior, criando uma cadeia cronológica de evidências. Os campos registrados incluem: identificador anonimizado do usuário, hash dos critérios comportamentais, score de risco, decisão, timestamp e assinatura digital do serviço autenticador.",
        "A consulta aos registros de auditoria é disponibilizada através de uma API REST dedicada, permitindo que administradores de segurança verifiquem a integridade das decisões tomadas pelo sistema. Relatórios periódicos podem ser gerados para conformidade regulatória, demonstrando que as decisões de autenticação foram tomadas de forma consistente e auditável.",
    ]),
]

# ============================================================
# STEP 1: Create numbering definition for MOTOR chapter (cap 3)
# ============================================================
print("=== 1. Criando definição de numeração para MOTOR (cap 3) ===")

# Find max abstractNumId and numId
max_abstract = 0
for abstract in numbering.findall(qn('w:abstractNum')):
    aid = int(abstract.get(qn('w:abstractNumId')))
    if aid > max_abstract:
        max_abstract = aid

max_num = 0
for num in numbering.findall(qn('w:num')):
    nid = int(num.get(qn('w:numId')))
    if nid > max_num:
        max_num = nid

new_abstract_id = str(max_abstract + 1)
new_num_id = str(max_num + 1)

# Copy abstractNum 12 (used by DESENVOLVIMENTO) as template
template_abstract = None
for abstract in numbering.findall(qn('w:abstractNum')):
    if abstract.get(qn('w:abstractNumId')) == '12':
        template_abstract = copy.deepcopy(abstract)
        break

template_abstract.set(qn('w:abstractNumId'), new_abstract_id)
# Set lvl 0 start = 3 (chapter 3)
for lvl in template_abstract.findall(qn('w:lvl')):
    if lvl.get(qn('w:ilvl')) == '0':
        start = lvl.find(qn('w:start'))
        start.set(qn('w:val'), '3')

# Add to numbering
numbering.append(template_abstract)

# Create num reference
new_num = OxmlElement('w:num')
new_num.set(qn('w:numId'), new_num_id)
abstract_ref = OxmlElement('w:abstractNumId')
abstract_ref.set(qn('w:val'), new_abstract_id)
new_num.append(abstract_ref)
numbering.append(new_num)

print(f"  abstractNum {new_abstract_id} (start=3), num {new_num_id}")

# ============================================================
# STEP 2: Update DESENVOLVIMENTO numbering start from 3 to 4
# ============================================================
print("\n=== 2. Atualizando numeração: DESENVOLVIMENTO 3→4, RESULTADOS 4→5, CRONOGRAMA 5→6 ===")

# DESENVOLVIMENTO uses numId=8 -> abstractNum 12 (start=3 -> 4)
# RESULTADOS uses numId=4 -> find its abstractNum
# CRONOGRAMA uses numId=3 -> find its abstractNum

chapter_numids = {'DESENVOLVIMENTO': '8', 'RESULTADOS': '4', 'CRONOGRAMA': '3'}
target_starts = {'DESENVOLVIMENTO': '4', 'RESULTADOS': '5', 'CRONOGRAMA': '6'}

for chapter_name, num_id in chapter_numids.items():
    for num in numbering.findall(qn('w:num')):
        if num.get(qn('w:numId')) == num_id:
            aid = num.find(qn('w:abstractNumId')).get(qn('w:val'))
            for abstract in numbering.findall(qn('w:abstractNum')):
                if abstract.get(qn('w:abstractNumId')) == aid:
                    for lvl in abstract.findall(qn('w:lvl')):
                        if lvl.get(qn('w:ilvl')) == '0':
                            start = lvl.find(qn('w:start'))
                            old = start.get(qn('w:val'))
                            start.set(qn('w:val'), target_starts[chapter_name])
                            print(f"  {chapter_name}: abstractNum {aid} start {old} → {target_starts[chapter_name]}")
            break

# ============================================================
# STEP 3: Insert chapter content before DESENVOLVIMENTO
# ============================================================
print("\n=== 3. Inserindo conteúdo do capítulo MOTOR DE IA ===")

# Insertion point: before paragraph 284 (empty before CAPÍTULO 3 box)
# Pattern: ..., empty, empty, BOX, empty, desc, empty, HEADING, empty, empty, H2...

insert_ref = doc.paragraphs[284]._element  # empty before the box

# Build list of elements to insert
elements_to_insert = []

# Copy the CAPÍTULO 3 box (para 285) as template for new box
box_template = copy.deepcopy(doc.paragraphs[285]._element)
# Change "3" to "3" and "CAPÍTULO" stays - actually we need it to say CAPÍTULO 3
# Since we're inserting BEFORE development, our chapter becomes 3 and development becomes 4
# The existing box at 285 says "CAPÍTULO 3" - we'll use it as-is for MOTOR
# But we need to update the EXISTING box to say "CAPÍTULO 4" later

# For the new MOTOR box, keep "CAPÍTULO 3"
elements_to_insert.append(('box', box_template))

# Empty paragraph after box
def make_body_text_para(text='', style='Body Text'):
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), style)
    pPr.append(pStyle)
    p.append(pPr)
    if text:
        r = OxmlElement('w:r')
        rPr = OxmlElement('w:rPr')
        rFonts = OxmlElement('w:rFonts')
        rFonts.set(qn('w:ascii'), 'Arial')
        rFonts.set(qn('w:hAnsi'), 'Arial')
        rPr.append(rFonts)
        sz = OxmlElement('w:sz')
        sz.set(qn('w:val'), '24')
        rPr.append(sz)
        r.append(rPr)
        t = OxmlElement('w:t')
        t.set(qn('xml:space'), 'preserve')
        t.text = text
        r.append(t)
        p.append(r)
    return p

def make_desc_para(text):
    """Italic description paragraph (Normal style)"""
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), 'Normal')
    pPr.append(pStyle)
    jc = OxmlElement('w:jc')
    jc.set(qn('w:val'), 'both')
    pPr.append(jc)
    p.append(pPr)

    r = OxmlElement('w:r')
    rPr = OxmlElement('w:rPr')
    rFonts = OxmlElement('w:rFonts')
    rFonts.set(qn('w:ascii'), 'Arial')
    rFonts.set(qn('w:hAnsi'), 'Arial')
    rPr.append(rFonts)
    italic = OxmlElement('w:i')
    rPr.append(italic)
    sz = OxmlElement('w:sz')
    sz.set(qn('w:val'), '24')
    rPr.append(sz)
    r.append(rPr)
    t = OxmlElement('w:t')
    t.set(qn('xml:space'), 'preserve')
    t.text = text
    r.append(t)
    p.append(r)
    return p

def make_heading1(text):
    """Create Heading 1 paragraph"""
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), 'Heading1')
    pPr.append(pStyle)
    p.append(pPr)

    r = OxmlElement('w:r')
    t = OxmlElement('w:t')
    t.set(qn('xml:space'), 'preserve')
    t.text = text
    r.append(t)
    p.append(r)
    return p

def make_heading2(text, num_id):
    """Create Heading 2 paragraph with numbering"""
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), 'Heading2')
    pPr.append(pStyle)

    # Add numbering reference
    numPr = OxmlElement('w:numPr')
    ilvl = OxmlElement('w:ilvl')
    ilvl.set(qn('w:val'), '1')
    numPr.append(ilvl)
    numId = OxmlElement('w:numId')
    numId.set(qn('w:val'), num_id)
    numPr.append(numId)
    pPr.append(numPr)

    p.append(pPr)

    r = OxmlElement('w:r')
    t = OxmlElement('w:t')
    t.set(qn('xml:space'), 'preserve')
    t.text = text
    r.append(t)
    p.append(r)
    return p

def make_body_paragraph(text):
    """Create body text paragraph with ABNT formatting"""
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), 'BodyText')
    pPr.append(pStyle)

    # Justify
    jc = OxmlElement('w:jc')
    jc.set(qn('w:val'), 'both')
    pPr.append(jc)

    # First line indent 1.25cm
    ind = OxmlElement('w:ind')
    ind.set(qn('w:firstLine'), '709')  # 1.25cm in twips
    pPr.append(ind)

    # Line spacing 1.5
    spacing = OxmlElement('w:spacing')
    spacing.set(qn('w:line'), '360')  # 1.5 * 240
    spacing.set(qn('w:lineRule'), 'auto')
    pPr.append(spacing)

    p.append(pPr)

    r = OxmlElement('w:r')
    rPr = OxmlElement('w:rPr')
    rFonts = OxmlElement('w:rFonts')
    rFonts.set(qn('w:ascii'), 'Arial')
    rFonts.set(qn('w:hAnsi'), 'Arial')
    rPr.append(rFonts)
    sz = OxmlElement('w:sz')
    sz.set(qn('w:val'), '24')  # 12pt
    rPr.append(sz)
    szCs = OxmlElement('w:szCs')
    szCs.set(qn('w:val'), '24')
    rPr.append(szCs)
    r.append(rPr)

    t = OxmlElement('w:t')
    t.set(qn('xml:space'), 'preserve')
    t.text = text
    r.append(t)
    p.append(r)
    return p

# Build the chapter elements in order
chapter_elements = []

# Empty before box
chapter_elements.append(make_body_text_para())
# Box
chapter_elements.append(box_template)
# Empty after box
chapter_elements.append(make_body_text_para())
# Description
chapter_elements.append(make_desc_para(chapter_box_desc))
# Empty
chapter_elements.append(make_body_text_para())
# Heading 1
chapter_elements.append(make_heading1(chapter_title))
# Empty
chapter_elements.append(make_body_text_para())
# Empty
chapter_elements.append(make_body_text_para())

# Subcapítulos
for sub_title, paragraphs in subcapitulos:
    # H2
    chapter_elements.append(make_heading2(sub_title, new_num_id))
    # Empty
    chapter_elements.append(make_body_text_para())
    # Empty
    chapter_elements.append(make_body_text_para())
    # Body paragraphs
    for para_text in paragraphs:
        chapter_elements.append(make_body_paragraph(para_text))
        chapter_elements.append(make_body_text_para())  # empty between paragraphs

# Final empty paragraphs before next chapter
chapter_elements.append(make_body_text_para())

# Insert all elements before the reference point
for elem in chapter_elements:
    insert_ref.addprevious(elem)

print(f"  {len(chapter_elements)} parágrafos inseridos")

# ============================================================
# STEP 4: Update chapter box numbers
# ============================================================
print("\n=== 4. Atualizando numeração dos boxes ===")

# Re-find all boxes and update numbers
for i, p in enumerate(doc.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' not in xml_str:
        continue

    texts = [t_el.text for t_el in p._element.iter(qn('w:t')) if t_el.text and t_el.text.strip()]
    combined = ' '.join(texts)
    if 'CAPÍTULO' not in combined:
        continue

    # Find next H1
    next_h1 = ''
    for j in range(i+1, min(i+15, len(doc.paragraphs))):
        if doc.paragraphs[j].style and doc.paragraphs[j].style.name == 'Heading 1' and doc.paragraphs[j].text.strip():
            next_h1 = doc.paragraphs[j].text.strip()
            break

    # Determine correct number
    target_num = None
    if 'INTRODUÇÃO' in next_h1:
        target_num = '1'
    elif 'FUNDAMENTAÇÃO' in next_h1:
        target_num = '2'
    elif 'MOTOR' in next_h1:
        target_num = '3'
    elif 'DESENVOLVIMENTO' in next_h1:
        target_num = '4'
    elif 'RESULTADOS' in next_h1:
        target_num = '5'
    elif 'CRONOGRAMA' in next_h1:
        target_num = '6'
    elif 'REFER' in next_h1:
        target_num = '7'

    if target_num is None:
        continue

    # Update the number in the box
    for t_el in p._element.iter(qn('w:t')):
        if t_el.text and t_el.text.strip().isdigit():
            old_num = t_el.text.strip()
            if old_num != target_num:
                t_el.text = target_num
                print(f"  CAPÍTULO {old_num} → {target_num} ({next_h1[:40]})")
            else:
                print(f"  CAPÍTULO {target_num} OK ({next_h1[:40]})")

# ============================================================
# STEP 5: Update chapter box descriptions
# ============================================================
print("\n=== 5. Atualizando descrições dos boxes ===")

# The existing description at (now shifted) position for DESENVOLVIMENTO
# needs to be updated since it was originally for cap 3
# Find the description paragraph after each box
for i, p in enumerate(doc.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' not in xml_str:
        continue

    texts = [t_el.text for t_el in p._element.iter(qn('w:t')) if t_el.text and t_el.text.strip()]
    combined = ' '.join(texts)
    if 'CAPÍTULO' not in combined:
        continue

    # Find next H1
    next_h1 = ''
    for j in range(i+1, min(i+15, len(doc.paragraphs))):
        if doc.paragraphs[j].style and doc.paragraphs[j].style.name == 'Heading 1' and doc.paragraphs[j].text.strip():
            next_h1 = doc.paragraphs[j].text.strip()
            break

    # Only need to update DESENVOLVIMENTO description
    if 'DESENVOLVIMENTO' not in next_h1:
        continue

    # Find description paragraph (Normal style with text, within 5 paras after box)
    for j in range(i+1, min(i+5, len(doc.paragraphs))):
        pj = doc.paragraphs[j]
        if pj.style and pj.style.name == 'Normal' and pj.text.strip():
            print(f"  DESENVOLVIMENTO desc: {pj.text[:60]}... (mantida)")
            break

# ============================================================
# STEP 6: Update TOC
# ============================================================
print("\n=== 6. Atualizando sumário (TOC) ===")

for sdt in body.findall(qn('w:sdt')):
    sdtPr = sdt.find(qn('w:sdtPr'))
    if sdtPr is None:
        continue
    docPartObj = sdtPr.find(qn('w:docPartObj'))
    if docPartObj is None:
        continue
    gallery = docPartObj.find(qn('w:docPartGallery'))
    if gallery is None or 'Table of Contents' not in gallery.get(qn('w:val'), ''):
        continue

    sdtContent = sdt.find(qn('w:sdtContent'))
    paras = list(sdtContent.findall(qn('w:p')))

    # Find DESENVOLVIMENTO TOC entry to insert MOTOR entries before it
    dev_toc_idx = None
    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'DESENVOLVIMENTO' in text:
            dev_toc_idx = idx
            break

    if dev_toc_idx is None:
        print("  DESENVOLVIMENTO not found in TOC!")
        break

    print(f"  DESENVOLVIMENTO found at TOC index {dev_toc_idx}")

    # Use an existing TOC H1 entry as template (copy DESENVOLVIMENTO entry)
    dev_toc_entry = paras[dev_toc_idx]

    # Create MOTOR H1 TOC entry
    motor_h1_entry = copy.deepcopy(dev_toc_entry)
    # Update text
    for t_el in motor_h1_entry.iter(qn('w:t')):
        if t_el.text and 'DESENVOLVIMENTO' in t_el.text:
            t_el.text = t_el.text.replace('DESENVOLVIMENTO', chapter_title)

    # Find a H2 TOC entry template (first H2 under DESENVOLVIMENTO)
    h2_template = None
    for idx in range(dev_toc_idx + 1, len(paras)):
        text = ''.join(t.text for t in paras[idx].iter(qn('w:t')) if t.text)
        if text.strip() and not any(ch in text for ch in ['INTRODUÇÃO', 'FUNDAMENTAÇÃO', 'DESENVOLVIMENTO', 'RESULTADOS', 'CRONOGRAMA', 'REFERÊNCIAS', 'MOTOR']):
            h2_template = paras[idx]
            break

    # Create TOC entries for MOTOR subcapítulos
    motor_toc_entries = [motor_h1_entry]

    if h2_template is not None:
        for sub_idx, (sub_title, _) in enumerate(subcapitulos):
            h2_entry = copy.deepcopy(h2_template)
            # Find and replace the text
            t_elements = list(h2_entry.iter(qn('w:t')))
            # Clear all text elements and set new text in the first one
            first_text_set = False
            for t_el in t_elements:
                if t_el.text and t_el.text.strip():
                    if not first_text_set:
                        t_el.text = f"3.{sub_idx + 1} {sub_title}"
                        first_text_set = True
                    else:
                        t_el.text = ''
            motor_toc_entries.append(h2_entry)

    # Insert MOTOR TOC entries before DESENVOLVIMENTO
    dev_ref = paras[dev_toc_idx]
    for entry in motor_toc_entries:
        dev_ref.addprevious(entry)

    print(f"  {len(motor_toc_entries)} entradas do MOTOR inseridas no TOC")

    # Update DESENVOLVIMENTO TOC numbering: 3.x → 4.x
    # Find all DESENVOLVIMENTO H2 entries
    # Re-get paras since we modified
    paras = list(sdtContent.findall(qn('w:p')))
    in_dev = False
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'DESENVOLVIMENTO' in text and 'MOTOR' not in text:
            in_dev = True
            continue
        if in_dev and any(ch in text for ch in ['RESULTADOS', 'CRONOGRAMA', 'REFERÊNCIAS']):
            in_dev = False
            # Also update these chapter entries
            if 'RESULTADOS' in text:
                for t_el in p.iter(qn('w:t')):
                    if t_el.text:
                        t_el.text = t_el.text.replace('4.', '5.').replace('4 ', '5 ')
            if 'CRONOGRAMA' in text:
                for t_el in p.iter(qn('w:t')):
                    if t_el.text:
                        t_el.text = t_el.text.replace('5.', '6.').replace('5 ', '6 ')
        if in_dev and text.strip():
            for t_el in p.iter(qn('w:t')):
                if t_el.text:
                    for old, new in [('3.9', '4.9'), ('3.8', '4.8'), ('3.7', '4.7'),
                                     ('3.6', '4.6'), ('3.5', '4.5'), ('3.4', '4.4'),
                                     ('3.3', '4.3'), ('3.2', '4.2'), ('3.1', '4.1')]:
                        t_el.text = t_el.text.replace(old, new)

    # Update RESULTADOS H2 entries: 4.x → 5.x
    in_res = False
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'RESULTADOS' in text:
            in_res = True
            continue
        if in_res and any(ch in text for ch in ['CRONOGRAMA', 'REFERÊNCIAS']):
            in_res = False
        if in_res and text.strip():
            for t_el in p.iter(qn('w:t')):
                if t_el.text:
                    for old, new in [('4.5', '5.5'), ('4.4', '5.4'), ('4.3', '5.3'),
                                     ('4.2', '5.2'), ('4.1', '5.1')]:
                        t_el.text = t_el.text.replace(old, new)

    print("  Numeração do TOC atualizada")
    break

# ============================================================
# STEP 7: Save and verify
# ============================================================
doc.save(DOCX_PATH)
print(f"\n=== Documento salvo: {DOCX_PATH} ===")

# Verify
print("\n=== Verificação ===")
doc2 = Document(DOCX_PATH)

print("\nCapítulos (Heading 1):")
for i, p in enumerate(doc2.paragraphs):
    if p.style and p.style.name == 'Heading 1' and p.text.strip() and i > 180:
        print(f"  {i}: {p.text[:70]}")

print("\nBoxes:")
for i, p in enumerate(doc2.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = [t_el.text for t_el in p._element.iter(qn('w:t')) if t_el.text and t_el.text.strip()]
        combined = ' '.join(texts)
        if 'CAPÍTULO' in combined:
            for j in range(i+1, min(i+15, len(doc2.paragraphs))):
                if doc2.paragraphs[j].style and doc2.paragraphs[j].style.name == 'Heading 1' and doc2.paragraphs[j].text.strip():
                    print(f"  {combined} → {doc2.paragraphs[j].text[:50]}")
                    break

print("\nSubcapítulos MOTOR:")
for i, p in enumerate(doc2.paragraphs):
    if p.style and p.style.name == 'Heading 2' and p.text.strip():
        pPr = p._element.find(qn('w:pPr'))
        numPr = pPr.find(qn('w:numPr')) if pPr is not None else None
        if numPr is not None:
            nid = numPr.find(qn('w:numId'))
            if nid is not None and nid.get(qn('w:val')) == new_num_id:
                print(f"  {i}: {p.text[:60]} (numId={new_num_id})")

print("\nTOC:")
for sdt in doc2.element.body.findall(qn('w:sdt')):
    sdtPr = sdt.find(qn('w:sdtPr'))
    if sdtPr is not None:
        docPartObj = sdtPr.find(qn('w:docPartObj'))
        if docPartObj is not None:
            gallery = docPartObj.find(qn('w:docPartGallery'))
            if gallery is not None and 'Table of Contents' in gallery.get(qn('w:val'), ''):
                for p in sdt.find(qn('w:sdtContent')).findall(qn('w:p')):
                    text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
                    if text.strip():
                        print(f"  {text[:80]}")
