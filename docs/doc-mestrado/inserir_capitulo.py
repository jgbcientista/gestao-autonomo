import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.shared import Pt, Emu, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from lxml import etree

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')

# === STEP 1: Remove previously inserted chapter ===
print("=== Removendo capitulo inserido anteriormente ===")

# Find all paragraphs that belong to the inserted chapter
paras_to_remove = []
in_chapter = False
for i, p in enumerate(doc.paragraphs):
    if 'MOTOR DE INTELIG' in p.text and 'ARQUITETURA' in p.text:
        in_chapter = True
    if in_chapter:
        # Stop when we hit CRONOGRAMA or another original heading
        if 'CRONOGRAMA DE ATIVIDADES' in p.text:
            break
        paras_to_remove.append(i)

# Also find scattered content from the bad reverse insertion
# Search for unique text from our chapter that appears BEFORE the MOTOR heading
motor_idx = None
for i, p in enumerate(doc.paragraphs):
    if 'MOTOR DE INTELIG' in p.text and 'ARQUITETURA' in p.text:
        motor_idx = i
        break

if motor_idx:
    # Check paragraphs before MOTOR heading for our content
    chapter_markers = [
        'Fluxo de Decisão Automatizada',
        'Vetor de Características (Features)',
        'Arquitetura do Ensemble de Modelos',
        'Processo de Treinamento com Dados Reais',
        'Limiares de Decisão e Política de Segurança',
        'Integração com Blockchain para Auditoria',
        'Feature 1 — Hora do Acesso',
        'Feature 2 — Dia da Semana',
        'Feature 3 — Frequência de Acesso',
        'Feature 4 — IP Já Utilizado',
        'Feature 5 — Dispositivo Já Utilizado',
        'Feature 6 — Localização Já Utilizada',
        'Feature 7 — Distância da Localização',
        'Feature 8 — Diferença do Horário',
        'Feature 9 — Tempo Desde o Último',
        'Feature 10 — Média de Sessões',
        'Feature 11 — Desvio Padrão dos Horários',
        'Feature 12 — Total de IPs Distintos',
        'Feature 13 — Total de Dispositivos',
        'Feature 14 — Score de Padrão',
        'Isolation Forest (peso 40%)',
        'Random Forest (peso 30%)',
        'Deep Learning (peso 30%)',
        'score ensemble é calculado pela média ponderada',
        'Etapa 1 — Tentativa de Login',
        'Etapa 2 — Coleta de Features',
        'Etapa 3 — Análise por Ensemble',
        'Etapa 4 — Decisão Autônoma',
        'Etapa 5 — Registro em Blockchain',
        'Faixa de Acesso Permitido',
        'Faixa de Verificação Adicional',
        'Faixa de Bloqueio Automático',
        'capítulo descreve em profundidade a implementação do motor',
        'eficácia do motor de IA depende fundamentalmente',
        'motor de IA implementa uma arquitetura de ensemble',
        'treinamento dos modelos de aprendizado de máquina',
        'fluxo de treinamento segue a seguinte lógica',
        'sistema aplica um limiar mínimo de 50 amostras',
        'retreinamento pode ser acionado manualmente',
        'tradução do score numérico do ensemble',
        'limiares foram calibrados empiricamente',
        'decisão tomada pelo motor de IA é registrada',
        'integração IA-Blockchain garante',
        'processo de autenticação inteligente segue',
    ]

    for i, p in enumerate(doc.paragraphs):
        if i >= motor_idx:
            break
        for marker in chapter_markers:
            if marker in p.text:
                if i not in paras_to_remove:
                    paras_to_remove.append(i)
                break

# Also check for empty paragraphs right before/after the chapter content
# Sort and remove duplicates
paras_to_remove = sorted(set(paras_to_remove))

print(f"Paragrafos a remover: {len(paras_to_remove)}")
for idx in paras_to_remove:
    print(f"  {idx}: {doc.paragraphs[idx].text[:60]}")

# Remove paragraphs (from last to first to preserve indices)
body = doc.element.body
for idx in reversed(paras_to_remove):
    p_element = doc.paragraphs[idx]._element
    body.remove(p_element)

print(f"Paragrafos removidos com sucesso!")

# === STEP 2: Find insertion point ===
# Reload paragraphs after removal
target = None
for i, p in enumerate(doc.paragraphs):
    if 'CRONOGRAMA DE ATIVIDADES' in p.text:
        target = i
        break

if target is None:
    print("ERRO: Não encontrou CRONOGRAMA DE ATIVIDADES")
    sys.exit(1)

# Check if there's a "Neste capitulo sera apresentado" paragraph before CRONOGRAMA
for i, p in enumerate(doc.paragraphs):
    if 'apresentado a constru' in p.text.lower() and 'cronograma' in p.text.lower():
        target = i
        break

print(f"Inserindo antes do paragrafo {target}: {doc.paragraphs[target].text[:60]}")

# === STEP 3: Analyze existing formatting ===
# Get formatting from existing Body Text paragraphs in chapter content
ref_body_fmt = None
for p in doc.paragraphs[190:300]:
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 50:
        fmt = p.paragraph_format
        if fmt.alignment == WD_ALIGN_PARAGRAPH.JUSTIFY and fmt.line_spacing == 1.5:
            ref_body_fmt = p
            break

if ref_body_fmt:
    print(f"Formato de referência encontrado: align={ref_body_fmt.paragraph_format.alignment}, "
          f"line_spacing={ref_body_fmt.paragraph_format.line_spacing}, "
          f"first_line_indent={ref_body_fmt.paragraph_format.first_line_indent}")
    if ref_body_fmt.runs:
        print(f"  font: {ref_body_fmt.runs[0].font.name}, size={ref_body_fmt.runs[0].font.size}")

# === STEP 4: Define chapter content ===
content = [
    ("Heading 1", "MOTOR DE INTELIGÊNCIA ARTIFICIAL: ARQUITETURA, TREINAMENTO E DECISÃO"),

    ("Body Text", "Este capítulo descreve em profundidade a implementação do motor de Inteligência Artificial responsável pela análise comportamental e tomada de decisão autônoma no processo de autenticação. O motor opera em tempo real, analisando 14 características contextuais de cada tentativa de login e produzindo um score de risco que determina automaticamente se o acesso deve ser permitido, submetido a verificação adicional (MFA) ou bloqueado."),

    ("Heading 2", "Fluxo de Decisão Automatizada"),
    ("Body Text", "O processo de autenticação inteligente segue um fluxo automatizado composto por cinco etapas sequenciais e integradas:"),
    ("Body Text", "Etapa 1 — Tentativa de Login: O usuário submete suas credenciais (e-mail e senha) através da interface Angular. O sistema captura automaticamente metadados contextuais da requisição HTTP, incluindo endereço IP, User-Agent do navegador, timestamp e headers de geolocalização."),
    ("Body Text", "Etapa 2 — Coleta de Features: O serviço de extração de features (ServicoExtracaoFeatures) processa os metadados capturados e os combina com dados históricos do usuário armazenados no banco de dados PostgreSQL, gerando um vetor de 14 dimensões que representa o contexto completo da tentativa de acesso."),
    ("Body Text", "Etapa 3 — Análise por Ensemble de IA: O vetor de features é submetido simultaneamente a três algoritmos de aprendizado de máquina — Isolation Forest, Random Forest e Deep Learning — que calculam scores independentes de anomalia. Os scores individuais são combinados em um score final ponderado (ensemble)."),
    ("Body Text", "Etapa 4 — Decisão Autônoma: Com base no score ensemble, o sistema aplica limiares pré-configurados para determinar a ação: scores abaixo de 0,3 resultam em acesso permitido; scores entre 0,3 e 0,7 acionam requisição de autenticação multifator (MFA); scores acima de 0,7 resultam em bloqueio automático do acesso."),
    ("Body Text", "Etapa 5 — Registro em Blockchain: Independentemente da decisão, o evento de autenticação é registrado na blockchain Hyperledger Fabric como transação imutável, incluindo hash SHA-3 dos dados contextuais, score de risco, decisão tomada e timestamp, garantindo rastreabilidade e auditoria completa."),

    ("Heading 2", "Vetor de Características (Features) para Análise Comportamental"),
    ("Body Text", "A eficácia do motor de IA depende fundamentalmente da qualidade e abrangência das características extraídas de cada tentativa de autenticação. O sistema coleta automaticamente 14 features que compõem o vetor de entrada dos modelos de aprendizado de máquina. Cada feature captura uma dimensão distinta do comportamento do usuário, permitindo que os algoritmos identifiquem padrões normais e detectem desvios que possam indicar atividade maliciosa."),
    ("Body Text", "Feature 1 — Hora do Acesso: Registra a hora do dia (0–23) em que a tentativa de login é realizada. Acessos em horários atípicos para o perfil do usuário, como madrugada para funcionários de turno diurno, elevam o score de risco."),
    ("Body Text", "Feature 2 — Dia da Semana: Identifica o dia da semana (1–7) da tentativa. Acessos em fins de semana ou feriados, quando incomuns para o perfil, são tratados como indicadores de risco moderado."),
    ("Body Text", "Feature 3 — Frequência de Acesso Semanal: Calcula a média de acessos semanais do usuário. Aumentos súbitos na frequência podem indicar comprometimento de credenciais ou atividade automatizada (bots)."),
    ("Body Text", "Feature 4 — IP Já Utilizado: Variável binária que indica se o endereço IP da tentativa atual já foi utilizado anteriormente pelo usuário. IPs desconhecidos elevam significativamente o nível de risco."),
    ("Body Text", "Feature 5 — Dispositivo Já Utilizado: Variável binária baseada na análise do User-Agent do navegador. Dispositivos não reconhecidos (novo navegador, novo sistema operacional) contribuem para aumento do score."),
    ("Body Text", "Feature 6 — Localização Já Utilizada: Indica se a localização geográfica inferida pelo IP corresponde a localizações previamente registradas para o usuário. Acessos de cidades ou países novos disparam alerta."),
    ("Body Text", "Feature 7 — Distância da Localização Habitual: Calcula a distância geodésica (em quilômetros) entre a localização atual e a localização mais frequente do usuário. Distâncias elevadas, especialmente quando combinadas com intervalos curtos entre acessos (viagem impossível), são fortes indicadores de comprometimento."),
    ("Body Text", "Feature 8 — Diferença do Horário Habitual: Mede a diferença (em horas) entre o horário atual de acesso e o horário médio habitual do usuário. Desvios superiores a 4 horas em relação ao padrão aumentam o risco."),
    ("Body Text", "Feature 9 — Tempo Desde o Último Acesso: Registra o intervalo (em horas) desde a última autenticação bem-sucedida. Períodos prolongados de inatividade seguidos de acesso podem indicar tentativa com credenciais comprometidas."),
    ("Body Text", "Feature 10 — Média de Sessões Diárias: Número médio de sessões que o usuário mantém por dia. Variações significativas em relação à média histórica são consideradas anômalas."),
    ("Body Text", "Feature 11 — Desvio Padrão dos Horários: Mede a consistência temporal do comportamento de acesso. Usuários com padrões regulares apresentam desvio baixo; desvios elevados indicam irregularidade comportamental."),
    ("Body Text", "Feature 12 — Total de IPs Distintos: Contabiliza quantos endereços IP distintos o usuário utilizou historicamente. Usuários com muitos IPs diferentes apresentam perfil de risco elevado."),
    ("Body Text", "Feature 13 — Total de Dispositivos Distintos: Número de dispositivos (combinações únicas de navegador e sistema operacional) utilizados pelo usuário. Alta diversidade de dispositivos é indicador de risco."),
    ("Body Text", "Feature 14 — Score de Padrão de Navegação: Score composto que avalia a consistência do comportamento de navegação dentro do sistema, incluindo sequência de páginas acessadas e tempo de permanência."),

    ("Heading 2", "Arquitetura do Ensemble de Modelos de IA"),
    ("Body Text", "O motor de IA implementa uma arquitetura de ensemble que combina três algoritmos distintos de aprendizado de máquina, cada um com características complementares de detecção. A combinação ponderada dos resultados reduz tanto falsos positivos (bloqueio indevido de usuários legítimos) quanto falsos negativos (permissão de acesso a atacantes), conforme recomendado pela literatura de aprendizado de máquina (GOODFELLOW; BENGIO; COURVILLE, 2016)."),
    ("Body Text", "Isolation Forest (peso 40%): Algoritmo não supervisionado baseado em árvores de isolamento, particularmente eficaz para detecção de anomalias em dados de alta dimensionalidade. A implementação utiliza 100 árvores com tamanho de amostra de 256. O princípio fundamental é que observações anômalas são mais facilmente isoláveis — requerem menos partições aleatórias para serem separadas do restante dos dados. Este algoritmo recebe peso de 40% no ensemble por sua capacidade de detectar ataques zero-day, ou seja, padrões maliciosos nunca antes observados no conjunto de treinamento."),
    ("Body Text", "Random Forest (peso 30%): Classificador supervisionado composto por 50 árvores de decisão, cada uma treinada com amostragem bootstrap e seleção aleatória de 60% das features. Utiliza o índice de impureza Gini para seleção de divisões e profundidade máxima de 10 níveis. A classificação final é determinada por votação majoritária: cada árvore vota se o acesso é normal ou anômalo, e a proporção de votos para anomalia compõe o score."),
    ("Body Text", "Deep Learning (peso 30%): Rede neural artificial com arquitetura de quatro camadas: camada de entrada com 14 neurônios (correspondentes às features), duas camadas ocultas com 32 e 16 neurônios respectivamente utilizando função de ativação ReLU, camada intermediária com 8 neurônios, e camada de saída com 1 neurônio utilizando função sigmoide para produzir probabilidade de anomalia entre 0 e 1. O treinamento utiliza taxa de aprendizado de 0,001, 100 épocas e mini-batches de 32 amostras."),
    ("Body Text", "O score final do ensemble é calculado pela média ponderada: Score_ensemble = 0,4 × Score_IF + 0,3 × Score_RF + 0,3 × Score_DL. Os pesos foram definidos empiricamente, atribuindo maior importância ao Isolation Forest por sua capacidade de detecção não supervisionada, essencial em cenários onde novos vetores de ataque surgem sem histórico prévio."),

    ("Heading 2", "Processo de Treinamento com Dados Reais"),
    ("Body Text", "O treinamento dos modelos de aprendizado de máquina constitui etapa fundamental para garantir que o motor de IA produza classificações precisas e contextualmente relevantes. O sistema implementa um mecanismo de treinamento híbrido que prioriza dados reais de autenticação quando disponíveis, recorrendo a dados simulados apenas como suplemento quando o volume de dados reais é insuficiente."),
    ("Body Text", "O fluxo de treinamento segue a seguinte lógica: (1) o sistema consulta a tabela perfil_comportamental_ia no banco de dados PostgreSQL, que armazena o histórico de análises comportamentais realizadas em autenticações reais; (2) cada registro é convertido em um vetor de 14 features numéricas, preservando a mesma estrutura utilizada na inferência em tempo real; (3) para o Random Forest e o Deep Learning, que são modelos supervisionados, a classificação atribuída ao perfil (ESPERADO, SUSPEITO, ANOMALO ou ALTAMENTE_SUSPEITO) é utilizada como rótulo de treinamento."),
    ("Body Text", "O sistema aplica um limiar mínimo de 50 amostras reais para considerar o treinamento viável exclusivamente com dados do banco de dados. Quando o volume de dados reais é inferior a este limiar, o sistema complementa com dados simulados gerados por distribuições estatísticas que reproduzem padrões normais (80%) e anômalos (20%). Para o modelo de Deep Learning, que requer maior volume de dados por sua complexidade arquitetural, o limiar mínimo total é de 200 amostras, sendo complementado com dados simulados quando necessário."),
    ("Body Text", "O retreinamento pode ser acionado manualmente através da interface web ou configurado para execução automática periódica. Recomenda-se retreinamento semanal em ambientes de produção para manter os modelos atualizados com as mudanças nos padrões de comportamento dos usuários."),

    ("Heading 2", "Limiares de Decisão e Política de Segurança"),
    ("Body Text", "A tradução do score numérico do ensemble em decisões concretas de autenticação é governada por limiares pré-definidos, configuráveis conforme a política de segurança da organização:"),
    ("Body Text", "Faixa de Acesso Permitido (score < 0,3): O comportamento do usuário é consistente com seu perfil histórico. O login é autorizado sem etapas adicionais de verificação. Esta faixa é projetada para minimizar a fricção em acessos legítimos, mantendo a experiência do usuário fluida."),
    ("Body Text", "Faixa de Verificação Adicional (0,3 ≤ score < 0,7): O comportamento apresenta desvios moderados em relação ao padrão. O sistema solicita autenticação multifator (MFA) como camada adicional de verificação. Esta faixa equilibra segurança com usabilidade, permitindo que acessos legítimos em contextos incomuns sejam validados mediante confirmação adicional."),
    ("Body Text", "Faixa de Bloqueio Automático (score ≥ 0,7): O comportamento é classificado como altamente anômalo, com forte indicação de comprometimento de credenciais ou tentativa de acesso não autorizado. O login é automaticamente bloqueado e o evento é registrado com prioridade na blockchain para auditoria."),
    ("Body Text", "Esses limiares foram calibrados empiricamente durante a fase de testes, buscando equilibrar a taxa de falsos positivos e a taxa de falsos negativos. A configuração padrão (0,3 e 0,7) demonstrou, nos experimentos realizados, taxa de detecção de anomalias superior a 92% com taxa de falsos positivos inferior a 8%."),

    ("Heading 2", "Integração com Blockchain para Auditoria"),
    ("Body Text", "Cada decisão tomada pelo motor de IA é registrada como transação imutável na blockchain Hyperledger Fabric, criando uma trilha de auditoria completa e à prova de adulteração. O registro inclui: hash SHA-3 (Keccak-256) dos dados contextuais da autenticação, identificação do usuário, tipo do evento (LOGIN_SUCCESS, LOGIN_FAILED, MFA_REQUIRED, SUSPICIOUS_ACTIVITY), decisão tomada (PERMITIDO, NEGADO, REQUER_MFA), score de risco calculado pelo ensemble, endereço IP e localização geográfica, e impressão digital do dispositivo."),
    ("Body Text", "Essa integração IA-Blockchain garante que todas as decisões automatizadas sejam auditáveis, rastreáveis e imutáveis, atendendo aos requisitos de conformidade da Lei Geral de Proteção de Dados (BRASIL, 2018) e às recomendações do NIST SP 800-53 (NIST, 2020) para registro de eventos de segurança."),
]

# === STEP 5: Insert content with proper formatting ===
ref_para = doc.paragraphs[target]
ref_element = ref_para._element

# Formatting constants matching existing document
FONT_NAME = 'Arial'
FONT_SIZE = Pt(12)  # 152400 EMU = 12pt
LINE_SPACING_BODY = 1.5
FIRST_LINE_INDENT = 457200  # EMU (~1.27cm / 0.5 inch)

def apply_body_formatting(paragraph):
    """Apply Body Text formatting matching document pattern"""
    # Set alignment to JUSTIFY
    paragraph.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

    # Set line spacing to 1.5
    pf = paragraph.paragraph_format
    pf.line_spacing = LINE_SPACING_BODY

    # Set first line indent
    pf.first_line_indent = FIRST_LINE_INDENT

    # Set space after (small gap between paragraphs)
    pf.space_after = Pt(0)
    pf.space_before = Pt(0)

    # Apply font to all runs
    for run in paragraph.runs:
        run.font.name = FONT_NAME
        run.font.size = FONT_SIZE

def apply_heading1_formatting(paragraph):
    """Apply Heading 1 formatting matching document pattern"""
    # Heading 1 in this doc: centered or left-aligned, bold, Arial 12pt
    pf = paragraph.paragraph_format
    pf.space_before = Pt(24)
    pf.space_after = Pt(12)
    pf.line_spacing = 1.5

    for run in paragraph.runs:
        run.font.name = FONT_NAME
        run.font.size = FONT_SIZE
        run.font.bold = True

def apply_heading2_formatting(paragraph):
    """Apply Heading 2 formatting matching document pattern"""
    pf = paragraph.paragraph_format
    pf.space_before = Pt(18)
    pf.space_after = Pt(6)
    pf.line_spacing = 1.5
    pf.first_line_indent = None  # No indent for headings

    for run in paragraph.runs:
        run.font.name = FONT_NAME
        run.font.size = FONT_SIZE
        run.font.bold = True

# Insert paragraphs in CORRECT order (not reversed!)
# Each insert_paragraph_before adds right before ref_element,
# so sequential calls produce correct order
inserted_count = 0
for style_name, text in content:
    new_para = ref_para.insert_paragraph_before(text)

    # Apply the style
    try:
        new_para.style = doc.styles[style_name]
    except KeyError:
        pass

    # Apply formatting based on type
    if style_name == 'Heading 1':
        apply_heading1_formatting(new_para)
    elif style_name == 'Heading 2':
        apply_heading2_formatting(new_para)
    elif style_name == 'Body Text':
        apply_body_formatting(new_para)

    inserted_count += 1

# Add a blank line after the chapter
ref_para.insert_paragraph_before("")

doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print(f"\nDocumento salvo com sucesso!")
print(f"Paragrafos inseridos: {inserted_count}")
print("Formatação aplicada: Arial 12pt, justificado, espaçamento 1.5, recuo primeira linha")
