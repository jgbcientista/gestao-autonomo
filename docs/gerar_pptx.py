from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)

# === CORES ===
AZUL_ESCURO = RGBColor(0x0D, 0x1B, 0x2A)
AZUL_MEDIO = RGBColor(0x1B, 0x2A, 0x4A)
AZUL_CLARO = RGBColor(0x41, 0x6D, 0x9F)
AZUL_DESTAQUE = RGBColor(0x00, 0x7A, 0xCC)
BRANCO = RGBColor(0xFF, 0xFF, 0xFF)
CINZA_CLARO = RGBColor(0xF0, 0xF2, 0xF5)
CINZA_TEXTO = RGBColor(0x4A, 0x4A, 0x4A)
VERDE = RGBColor(0x27, 0xAE, 0x60)
LARANJA = RGBColor(0xE6, 0x7E, 0x22)
VERMELHO = RGBColor(0xC0, 0x39, 0x2B)
AMARELO = RGBColor(0xF3, 0x9C, 0x12)

def set_slide_bg(slide, color):
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = color

def add_shape(slide, left, top, width, height, color, corner_radius=None):
    if corner_radius:
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.adjustments[0] = corner_radius
    else:
        shape = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = color
    shape.line.fill.background()
    return shape

def add_text(slide, left, top, width, height, text, font_size=18, color=CINZA_TEXTO, bold=False, alignment=PP_ALIGN.LEFT, font_name="Segoe UI"):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = text
    p.font.size = Pt(font_size)
    p.font.color.rgb = color
    p.font.bold = bold
    p.font.name = font_name
    p.alignment = alignment
    return txBox

def add_multiline(slide, left, top, width, height, lines, font_size=16, color=CINZA_TEXTO, bold=False, line_spacing=1.3):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    for i, line in enumerate(lines):
        if i == 0:
            p = tf.paragraphs[0]
        else:
            p = tf.add_paragraph()
        p.text = line
        p.font.size = Pt(font_size)
        p.font.color.rgb = color
        p.font.bold = bold
        p.font.name = "Segoe UI"
        p.space_after = Pt(font_size * (line_spacing - 1) * 2)
    return txBox

def add_bullet_list(slide, left, top, width, height, items, font_size=15, color=CINZA_TEXTO, bullet_char="\u2022"):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    for i, item in enumerate(items):
        if i == 0:
            p = tf.paragraphs[0]
        else:
            p = tf.add_paragraph()
        p.text = f"{bullet_char} {item}"
        p.font.size = Pt(font_size)
        p.font.color.rgb = color
        p.font.name = "Segoe UI"
        p.space_after = Pt(6)
    return txBox

def slide_header(slide, titulo, subtitulo=None):
    set_slide_bg(slide, BRANCO)
    # Top bar
    add_shape(slide, Inches(0), Inches(0), prs.slide_width, Inches(1.3), AZUL_ESCURO)
    add_text(slide, Inches(0.8), Inches(0.25), Inches(11), Inches(0.6), titulo, font_size=28, color=BRANCO, bold=True)
    if subtitulo:
        add_text(slide, Inches(0.8), Inches(0.75), Inches(11), Inches(0.4), subtitulo, font_size=14, color=RGBColor(0xAA,0xCC,0xEE))
    # Accent line
    add_shape(slide, Inches(0), Inches(1.3), prs.slide_width, Inches(0.06), AZUL_DESTAQUE)

def card(slide, left, top, width, height, title, body_lines, title_color=AZUL_DESTAQUE, bg_color=CINZA_CLARO):
    shape = add_shape(slide, left, top, width, height, bg_color, corner_radius=0.05)
    add_text(slide, left + Inches(0.2), top + Inches(0.1), width - Inches(0.4), Inches(0.4), title, font_size=14, color=title_color, bold=True)
    add_bullet_list(slide, left + Inches(0.2), top + Inches(0.5), width - Inches(0.4), height - Inches(0.6), body_lines, font_size=12, color=CINZA_TEXTO)

# =====================================================
# SLIDE 1 — CAPA
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide, AZUL_ESCURO)
# Geometric accents
add_shape(slide, Inches(0), Inches(0), Inches(0.15), prs.slide_height, AZUL_DESTAQUE)
add_shape(slide, Inches(0), Inches(7.1), prs.slide_width, Inches(0.06), AZUL_CLARO)

add_text(slide, Inches(1.5), Inches(1.5), Inches(10), Inches(1),
         "TRABALHOS RELACIONADOS E ESTADO DA ARTE",
         font_size=36, color=BRANCO, bold=True, alignment=PP_ALIGN.CENTER)
add_text(slide, Inches(1.5), Inches(2.5), Inches(10), Inches(0.8),
         "Autenticacao Adaptativa com Inteligencia Artificial,\nBiometria Comportamental e Blockchain",
         font_size=20, color=RGBColor(0xAA,0xCC,0xEE), alignment=PP_ALIGN.CENTER)
add_shape(slide, Inches(5.5), Inches(3.5), Inches(2.3), Inches(0.04), AZUL_DESTAQUE)
add_text(slide, Inches(1.5), Inches(4.0), Inches(10), Inches(0.5),
         "Sistema Autonomo de Autenticacao Inteligente",
         font_size=18, color=RGBColor(0x88,0xAA,0xCC), alignment=PP_ALIGN.CENTER)
add_text(slide, Inches(1.5), Inches(5.5), Inches(10), Inches(0.5),
         "Abril 2026",
         font_size=14, color=RGBColor(0x66,0x88,0xAA), alignment=PP_ALIGN.CENTER)

# =====================================================
# SLIDE 2 — AGENDA
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Agenda")

items = [
    ("01", "Contexto e Objetivo da Revisao"),
    ("02", "Quadro Comparativo Geral"),
    ("03", "Pesquisa no INPI"),
    ("04", "Trabalhos Academicos (12 estudos)"),
    ("05", "Solucoes da Industria"),
    ("06", "Sintese Comparativa e Diferenciais"),
    ("07", "Lacunas Identificadas na Literatura"),
]
for i, (num, texto) in enumerate(items):
    y = Inches(1.8) + Inches(i * 0.7)
    add_shape(slide, Inches(1.5), y, Inches(0.7), Inches(0.5), AZUL_DESTAQUE, corner_radius=0.1)
    add_text(slide, Inches(1.55), y + Inches(0.05), Inches(0.6), Inches(0.4), num, font_size=18, color=BRANCO, bold=True, alignment=PP_ALIGN.CENTER)
    add_text(slide, Inches(2.5), y + Inches(0.05), Inches(8), Inches(0.4), texto, font_size=18, color=CINZA_TEXTO)

# =====================================================
# SLIDE 3 — CONTEXTO
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Contexto e Objetivo da Revisao")

add_text(slide, Inches(0.8), Inches(1.6), Inches(11.5), Inches(1.2),
         "O sistema proposto integra multiplas tecnologias em uma plataforma unica de autenticacao inteligente. "
         "Esta revisao identifica e compara trabalhos da literatura academica e da industria que abordam, "
         "de forma isolada ou parcial, os mesmos eixos tematicos.",
         font_size=16, color=CINZA_TEXTO)

card(slide, Inches(0.8), Inches(3.0), Inches(3.6), Inches(3.5),
     "Machine Learning",
     ["Isolation Forest (40%)", "Random Forest (30%)", "Deep Learning (30%)", "Score unificado 0.0-1.0"],
     title_color=AZUL_DESTAQUE)

card(slide, Inches(4.8), Inches(3.0), Inches(3.6), Inches(3.5),
     "Biometria e Contexto",
     ["Dinamica de digitacao", "Geolocalizacao por IP", "Analise de sessao", "Deriva comportamental"],
     title_color=VERDE)

card(slide, Inches(8.8), Inches(3.0), Inches(3.6), Inches(3.5),
     "Blockchain e XAI",
     ["Hyperledger Fabric", "Ethereum (Web3j)", "Auditoria imutavel", "Explicabilidade via SHAP"],
     title_color=LARANJA)

# =====================================================
# SLIDE 4 — QUADRO COMPARATIVO (parte 1)
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Quadro Comparativo Geral", "Trabalhos Academicos (1/2)")

# Table
rows = 8
cols = 5
tbl = slide.shapes.add_table(rows, cols, Inches(0.4), Inches(1.6), Inches(12.5), Inches(5.5)).table
tbl.columns[0].width = Inches(3.2)
tbl.columns[1].width = Inches(1.0)
tbl.columns[2].width = Inches(3.0)
tbl.columns[3].width = Inches(2.8)
tbl.columns[4].width = Inches(2.5)

headers = ["Trabalho", "Ano", "Objetivo", "Experimento", "Metricas"]
data = [
    ["Adaptive Biometric Auth. Systems (ScienceDirect)", "2023", "Mapear estado da arte em autenticacao biometrica adaptativa", "Revisao de 80+ trabalhos", "FAR, FRR, EER"],
    ["AI-Driven Adaptive Auth. (ESRG)", "2024", "Reduzir FAR/FRR com ML multimodal", "Testes com biometria multimodal", "FAR -27%, FRR -35%"],
    ["Continuous Smartphone Auth. (MDPI)", "2026", "Autenticacao continua em dispositivos moveis", "Pipeline CNN+LSTM+GRU", "Acuracia, EER, F1"],
    ["Keystroke Dynamics (Springer)", "2025", "Autenticacao por padroes de digitacao", "KNN, RF, LGBM em texto fixo", "EER 2.67%-6.61%"],
    ["Blockchain Audit Trail (MDPI)", "2021", "Auditoria imutavel com blockchain", "Smart contracts", "Throughput, latencia"],
    ["Harpocrates (Virginia Tech)", "2022", "Logs imutaveis com privacidade", "Hyperledger Fabric em EC2", "Latencia, overhead"],
    ["IAM + Hyperledger + OAuth (ScienceDirect)", "2023", "IAM com blockchain para saude", "Prototipo Hyperledger", "Escalabilidade, seguranca"],
]

for j, h in enumerate(headers):
    cell = tbl.cell(0, j)
    cell.text = h
    for p in cell.text_frame.paragraphs:
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = BRANCO
        p.font.name = "Segoe UI"
    cell.fill.solid()
    cell.fill.fore_color.rgb = AZUL_ESCURO

for i, row_data in enumerate(data):
    for j, val in enumerate(row_data):
        cell = tbl.cell(i+1, j)
        cell.text = val
        for p in cell.text_frame.paragraphs:
            p.font.size = Pt(10)
            p.font.color.rgb = CINZA_TEXTO
            p.font.name = "Segoe UI"
        cell.fill.solid()
        cell.fill.fore_color.rgb = BRANCO if i % 2 == 0 else CINZA_CLARO

# =====================================================
# SLIDE 5 — QUADRO COMPARATIVO (parte 2)
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Quadro Comparativo Geral", "Trabalhos Academicos (2/2) + Industria")

rows = 8
cols = 5
tbl = slide.shapes.add_table(rows, cols, Inches(0.4), Inches(1.6), Inches(12.5), Inches(5.5)).table
tbl.columns[0].width = Inches(3.2)
tbl.columns[1].width = Inches(1.0)
tbl.columns[2].width = Inches(3.0)
tbl.columns[3].width = Inches(2.8)
tbl.columns[4].width = Inches(2.5)

headers = ["Trabalho", "Ano", "Objetivo", "Experimento", "Metricas"]
data = [
    ["Risk-Based Auth. com ML (IEEE)", "2018", "Classificar logins por nivel de risco", "Modelos ML para scoring", "Acuracia, precisao, recall"],
    ["Trust Engine Behavioral (IJETCSIT)", "2024", "Score de confianca dinamico passwordless", "Avaliacao em moveis/wearables", "TAR>90%, FAR<5%, EER~6.5%"],
    ["AI Continuous Auth. Zero Trust (ResearchGate)", "2025", "Decisoes adaptativas com RL", "Framework com Reinforcement Learning", "Reducao falsos positivos"],
    ["XAI para IDS (Frontiers in AI)", "2025", "Explicabilidade em sistemas de seguranca", "Analise SHAP/LIME em IDS", "Interpretabilidade, fidelidade"],
    ["Adaptive Security + Drift (Al-Qadisiyah)", "2024", "Deteccao de deriva comportamental", "LSTM+Autoencoder+KL-divergence", "Acuracia de deteccao"],
    ["IBM Trusteer (Industria)", "2016+", "Prevencao de fraude com biometria IA", "Producao em inst. financeiras", "Proprietario (nao publicado)"],
    ["BioCatch (Industria)", "2011+", "Biometria comportamental cognitiva", "Producao em bancos/fintechs", "Proprietario (nao publicado)"],
]

for j, h in enumerate(headers):
    cell = tbl.cell(0, j)
    cell.text = h
    for p in cell.text_frame.paragraphs:
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = BRANCO
        p.font.name = "Segoe UI"
    cell.fill.solid()
    cell.fill.fore_color.rgb = AZUL_ESCURO

for i, row_data in enumerate(data):
    for j, val in enumerate(row_data):
        cell = tbl.cell(i+1, j)
        cell.text = val
        for p in cell.text_frame.paragraphs:
            p.font.size = Pt(10)
            p.font.color.rgb = CINZA_TEXTO
            p.font.name = "Segoe UI"
        cell.fill.solid()
        cell.fill.fore_color.rgb = BRANCO if i % 2 == 0 else CINZA_CLARO

# =====================================================
# SLIDE 6 — PESQUISA INPI
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Pesquisa no INPI", "Instituto Nacional da Propriedade Industrial")

add_shape(slide, Inches(0.8), Inches(1.7), Inches(5.5), Inches(2.5), CINZA_CLARO, corner_radius=0.05)
add_text(slide, Inches(1.0), Inches(1.8), Inches(5.1), Inches(0.4), "Resultados da Busca", font_size=16, color=AZUL_DESTAQUE, bold=True)
add_bullet_list(slide, Inches(1.0), Inches(2.3), Inches(5.1), Inches(1.8), [
    "47 pedidos de patentes relacionados a blockchain",
    "Nenhum combinando todos os elementos da proposta",
    "Sem registros de biometria comportamental + ML + blockchain",
    "Termos: autenticacao adaptativa, login inteligente, biometria comportamental",
], font_size=13, color=CINZA_TEXTO)

add_shape(slide, Inches(6.8), Inches(1.7), Inches(5.5), Inches(2.5), CINZA_CLARO, corner_radius=0.05)
add_text(slide, Inches(7.0), Inches(1.8), Inches(5.1), Inches(0.4), "Diretrizes de IA no INPI (Ago/2025)", font_size=16, color=AZUL_DESTAQUE, bold=True)
add_bullet_list(slide, Inches(7.0), Inches(2.3), Inches(5.1), Inches(1.8), [
    "Consulta publica sobre patentes de IA",
    "Categorias: modelos de IA, invencoes baseadas em IA, invencoes assistidas por IA",
    "Amadurecimento regulatorio para protecao futura",
], font_size=13, color=CINZA_TEXTO)

add_shape(slide, Inches(0.8), Inches(4.6), Inches(11.5), Inches(2.4), RGBColor(0xE8,0xF5,0xE9), corner_radius=0.05)
add_text(slide, Inches(1.0), Inches(4.7), Inches(11.1), Inches(0.4), "Relacao com a Proposta", font_size=16, color=VERDE, bold=True)
add_text(slide, Inches(1.0), Inches(5.2), Inches(11.1), Inches(1.6),
         "A ausencia de patentes no INPI que integrem autenticacao adaptativa com ML, biometria comportamental, "
         "blockchain e explicabilidade de IA evidencia o carater inovador da proposta. O sistema se posiciona "
         "em uma lacuna de propriedade intelectual no cenario brasileiro, combinando elementos que, "
         "individualmente, ja possuem registro, mas que em conjunto representam uma contribuicao original.",
         font_size=14, color=CINZA_TEXTO)

# =====================================================
# SLIDES 7-12 — TRABALHOS ACADEMICOS (detalhados)
# =====================================================

trabalhos = [
    {
        "titulo": "Adaptive Biometric Authentication Systems",
        "fonte": "ScienceDirect, 2023 - Revisao Sistematica",
        "descricao": (
            "Revisao sistematica que analisa mais de 80 trabalhos sobre sistemas de autenticacao "
            "biometrica adaptativos. Mapeia como esses sistemas ajustam dinamicamente seus processos "
            "de amostragem e reconhecimento em resposta a mudancas no ambiente operacional. "
            "Identifica desafios criticos como ameacas a privacidade, spoofing e acessibilidade."
        ),
        "comparacao": (
            "O sistema proposto implementa concretamente uma solucao adaptativa com ensemble de ML "
            "(Isolation Forest + Random Forest + Deep Learning), MFA condicional baseado em risco, "
            "e deteccao de deriva comportamental. Adiciona blockchain e explicabilidade de IA — "
            "elementos nao cobertos na revisao."
        ),
    },
    {
        "titulo": "AI-Driven Adaptive Authentication for Multi-Modal Biometrics",
        "fonte": "ESRG Journal, 2024",
        "descricao": (
            "Framework com ML para ajuste dinamico de parametros de autenticacao baseado em dados "
            "contextuais e comportamentais. Resultados: reducao de 27% na FAR e 35% na FRR "
            "comparado a metodos estaticos tradicionais. Utiliza biometria multimodal para perfis adaptativos."
        ),
        "comparacao": (
            "A proposta compartilha o principio de ajuste dinamico, mas implementa ensemble de 3 algoritmos "
            "com pesos configuraveis (IF 40%, RF 30%, DL 30%) e regras de decisao escalonadas em 4 niveis "
            "(permitir, MFA, MFA+alerta, bloquear). Integra blockchain para registro imutavel de cada decisao."
        ),
    },
    {
        "titulo": "Continuous Smartphone Auth. + Keystroke Dynamics",
        "fonte": "MDPI 2026 / Springer 2025",
        "descricao": (
            "O trabalho da MDPI utiliza pipeline de CNN+LSTM+GRU para autenticacao continua em moveis. "
            "O da Springer aplica KNN, RF e LGBM para keystroke dynamics, alcancando EER de 2.67% a 6.61%. "
            "Ambos demonstram a eficacia de ensembles e biometria comportamental."
        ),
        "comparacao": (
            "O sistema proposto integra dinamica de digitacao como uma das 14 features do ensemble (nao isoladamente), "
            "combinando com geolocalizacao, padrao de sessao e contexto. Adiciona explicabilidade SHAP "
            "e score persistente em blockchain — aspectos nao abordados nos trabalhos comparados."
        ),
    },
    {
        "titulo": "Blockchain Audit Trail + Harpocrates",
        "fonte": "MDPI 2021 / Virginia Tech 2022",
        "descricao": (
            "O MDPI propoe mecanismo generico de audit trail com smart contracts. "
            "Harpocrates implementa logs imutaveis com preservacao de privacidade sobre Hyperledger Fabric "
            "em Amazon EC2, combinando tecnicas criptograficas com imutabilidade blockchain."
        ),
        "comparacao": (
            "O sistema proposto registra especificamente eventos de autenticacao (hash, tipo de evento, "
            "ID hasheado SHA-3, IP, score, decisao, timestamp) em blockchain dual (Hyperledger Fabric + Ethereum). "
            "Opera em modo simulacao para dev e redes reais em producao — flexibilidade nao presente nos comparados."
        ),
    },
    {
        "titulo": "IAM Hyperledger + OAuth 2.0 / Risk-Based Auth. IEEE",
        "fonte": "ScienceDirect 2023 / IEEE 2018",
        "descricao": (
            "O trabalho ScienceDirect combina Hyperledger Fabric com OAuth 2.0 para IAM no setor de saude. "
            "O IEEE (2018) e um trabalho pioneiro de autenticacao baseada em risco com ML, "
            "classificando logins por nivel de risco com features contextuais basicas."
        ),
        "comparacao": (
            "O sistema proposto substitui autenticacao estatica (OAuth) por score dinamico de IA, "
            "com 4 niveis de resposta em vez de decisao binaria. Expande o trabalho de 2018 "
            "com ensemble de 3 algoritmos, 14 features, biometria comportamental, blockchain e explicabilidade."
        ),
    },
    {
        "titulo": "Trust Engine + Zero Trust + XAI + Drift Detection",
        "fonte": "IJETCSIT 2024 / ResearchGate 2025 / Frontiers 2025 / Al-Qadisiyah 2024",
        "descricao": (
            "Trust Engine: score dinamico passwordless com TAR>90% e FAR<5%. Zero Trust: RL para enforcement "
            "adaptativo. XAI: revisao de SHAP/LIME para IDS. Drift Detection: LSTM+Autoencoder "
            "com KL-divergence para detectar mudancas de comportamento."
        ),
        "comparacao": (
            "O sistema proposto integra TODOS esses elementos em uma unica plataforma: "
            "score de confianca (como Trust Engine), decisoes adaptativas (como Zero Trust), "
            "explicabilidade SHAP (como XAI), e deteccao de deriva (como Drift Detection) — "
            "com o diferencial de registro imutavel em blockchain de cada decisao e explicacao."
        ),
    },
]

for t in trabalhos:
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    slide_header(slide, t["titulo"], t["fonte"])

    # Description box
    add_shape(slide, Inches(0.6), Inches(1.6), Inches(12.1), Inches(2.4), CINZA_CLARO, corner_radius=0.05)
    add_text(slide, Inches(0.8), Inches(1.7), Inches(11.7), Inches(0.35), "Descricao do Trabalho", font_size=14, color=AZUL_DESTAQUE, bold=True)
    add_text(slide, Inches(0.8), Inches(2.1), Inches(11.7), Inches(1.7), t["descricao"], font_size=14, color=CINZA_TEXTO)

    # Comparison box
    add_shape(slide, Inches(0.6), Inches(4.3), Inches(12.1), Inches(2.7), RGBColor(0xE8,0xF5,0xE9), corner_radius=0.05)
    add_text(slide, Inches(0.8), Inches(4.4), Inches(11.7), Inches(0.35), "Comparacao com a Proposta", font_size=14, color=VERDE, bold=True)
    add_text(slide, Inches(0.8), Inches(4.85), Inches(11.7), Inches(2.0), t["comparacao"], font_size=14, color=CINZA_TEXTO)

# =====================================================
# SLIDE 13 — SOLUCOES DA INDUSTRIA
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Solucoes da Industria", "IBM Trusteer e BioCatch")

# IBM Card
add_shape(slide, Inches(0.6), Inches(1.6), Inches(5.8), Inches(5.2), CINZA_CLARO, corner_radius=0.05)
add_text(slide, Inches(0.8), Inches(1.7), Inches(5.4), Inches(0.4), "IBM Security Trusteer (2016+)", font_size=16, color=AZUL_DESTAQUE, bold=True)
add_text(slide, Inches(0.8), Inches(2.2), Inches(5.4), Inches(1.5),
         "Suite de prevencao de fraude com IA, biometria comportamental e "
         "inteligencia global. Analisa sinais de identidade digital e "
         "informacoes de dispositivo. Utilizado por centenas de instituicoes financeiras.",
         font_size=13, color=CINZA_TEXTO)
add_text(slide, Inches(0.8), Inches(3.8), Inches(5.4), Inches(0.3), "vs. Proposta:", font_size=13, color=VERDE, bold=True)
add_text(slide, Inches(0.8), Inches(4.2), Inches(5.4), Inches(2.3),
         "Trusteer e proprietario/black-box. A proposta oferece: explicabilidade SHAP, "
         "blockchain para auditoria, arquitetura aberta e reprodutivel, "
         "metricas e decisoes transparentes.",
         font_size=13, color=CINZA_TEXTO)

# BioCatch Card
add_shape(slide, Inches(6.9), Inches(1.6), Inches(5.8), Inches(5.2), CINZA_CLARO, corner_radius=0.05)
add_text(slide, Inches(7.1), Inches(1.7), Inches(5.4), Inches(0.4), "BioCatch (2011+)", font_size=16, color=AZUL_DESTAQUE, bold=True)
add_text(slide, Inches(7.1), Inches(2.2), Inches(5.4), Inches(1.5),
         "Biometria comportamental cognitiva e fisica para prevencao de fraude. "
         "Analisa interacoes humano-dispositivo com IA. "
         "Diferencia usuarios legitimos de fraudadores. Adotado por bancos globais.",
         font_size=13, color=CINZA_TEXTO)
add_text(slide, Inches(7.1), Inches(3.8), Inches(5.4), Inches(0.3), "vs. Proposta:", font_size=13, color=VERDE, bold=True)
add_text(slide, Inches(7.1), Inches(4.2), Inches(5.4), Inches(2.3),
         "BioCatch e SaaS proprietario para fraude financeira. A proposta e "
         "voltada para autenticacao web geral, com ensemble explicito de 3 algoritmos, "
         "blockchain dual, explicabilidade SHAP e deteccao de deriva.",
         font_size=13, color=CINZA_TEXTO)

# =====================================================
# SLIDE 14 — SINTESE COMPARATIVA (tabela)
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Sintese Comparativa", "Diferenciais exclusivos da proposta")

rows = 10
cols = 4
tbl = slide.shapes.add_table(rows, cols, Inches(0.6), Inches(1.6), Inches(12.1), Inches(5.4)).table
tbl.columns[0].width = Inches(5.0)
tbl.columns[1].width = Inches(2.4)
tbl.columns[2].width = Inches(2.4)
tbl.columns[3].width = Inches(2.3)

headers = ["Caracteristica", "Proposta", "Literatura", "Industria"]
data = [
    ["Ensemble de 3 algoritmos (IF+RF+DL) com pesos configuraveis", "SIM", "Parcial (1-2 modelos)", "Nao divulgado"],
    ["Score de confianca com 4 niveis de decisao", "SIM", "Binario ou ternario", "Nao divulgado"],
    ["Blockchain dual (Hyperledger Fabric + Ethereum)", "SIM", "Uma rede ou nenhuma", "Nao utilizado"],
    ["Explicabilidade via SHAP para autenticacao", "SIM", "Aplicado em IDS", "Nao disponivel"],
    ["Deteccao de deriva comportamental integrada", "SIM", "Trabalhos isolados", "Parcial"],
    ["Dinamica de digitacao + contexto + geolocalizacao", "SIM", "Geralmente unimodal", "Multimodal (proprietario)"],
    ["Trilha de auditoria imutavel das decisoes de IA", "SIM", "Blockchain generico", "Nao disponivel"],
    ["Codigo aberto e reprodutivel", "SIM", "Parcial", "Nao"],
    ["MFA adaptativo condicionado ao score", "SIM", "Raro", "Parcial"],
]

for j, h in enumerate(headers):
    cell = tbl.cell(0, j)
    cell.text = h
    for p in cell.text_frame.paragraphs:
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = BRANCO
        p.font.name = "Segoe UI"
    cell.fill.solid()
    cell.fill.fore_color.rgb = AZUL_ESCURO

for i, row_data in enumerate(data):
    for j, val in enumerate(row_data):
        cell = tbl.cell(i+1, j)
        cell.text = val
        for p in cell.text_frame.paragraphs:
            p.font.size = Pt(11)
            p.font.name = "Segoe UI"
            if j == 1 and val == "SIM":
                p.font.color.rgb = VERDE
                p.font.bold = True
            else:
                p.font.color.rgb = CINZA_TEXTO
        cell.fill.solid()
        cell.fill.fore_color.rgb = BRANCO if i % 2 == 0 else CINZA_CLARO

# =====================================================
# SLIDE 15 — LACUNAS NA LITERATURA
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Lacunas Identificadas na Literatura", "Contribuicoes originais da proposta")

lacunas = [
    ("01", "Explicabilidade + Autenticacao + Blockchain",
     "Nenhum trabalho revisado implementa explicabilidade de IA especificamente para decisoes de autenticacao com registro em blockchain."),
    ("02", "Blockchain Dual para Auditoria",
     "A combinacao de Hyperledger Fabric (permissionada) e Ethereum (interoperavel) para auditoria de eventos de autenticacao nao foi encontrada."),
    ("03", "Ensemble de 3 Paradigmas de ML",
     "A combinacao de deteccao de anomalias (IF), classificacao (RF) e aprendizado profundo (DL) com pesos configuraveis para autenticacao e contribuicao original."),
    ("04", "Resposta Escalonada em 4 Niveis",
     "A granularidade de resposta (permitir, MFA, MFA+alerta, bloquear) baseada em score continuo supera as abordagens binarias predominantes."),
]

colors_lacuna = [AZUL_DESTAQUE, VERDE, LARANJA, VERMELHO]

for i, (num, titulo, desc) in enumerate(lacunas):
    y = Inches(1.7) + Inches(i * 1.35)
    add_shape(slide, Inches(0.8), y, Inches(0.7), Inches(0.5), colors_lacuna[i], corner_radius=0.1)
    add_text(slide, Inches(0.85), y + Inches(0.05), Inches(0.6), Inches(0.4), num, font_size=20, color=BRANCO, bold=True, alignment=PP_ALIGN.CENTER)
    add_text(slide, Inches(1.8), y, Inches(10.5), Inches(0.4), titulo, font_size=16, color=AZUL_ESCURO, bold=True)
    add_text(slide, Inches(1.8), y + Inches(0.45), Inches(10.5), Inches(0.8), desc, font_size=13, color=CINZA_TEXTO)

# =====================================================
# SLIDE 16 — REFERENCIAS
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
slide_header(slide, "Referencias Bibliograficas")

refs = [
    "[1] Adaptive Biometric Auth. Systems — ScienceDirect, 2023",
    "[2] AI-Driven Adaptive Auth. Multi-Modal — ESRG Journal, 2024",
    "[3] Continuous Smartphone Auth. — MDPI Mathematics, 2026",
    "[4] Keystroke Dynamics — Springer Discover Applied Sciences, 2025",
    "[5] Blockchain Audit Trail — MDPI Algorithms, 2021",
    "[6] Harpocrates — arXiv / Virginia Tech, 2022",
    "[7] IAM + Hyperledger + OAuth 2.0 — ScienceDirect, 2023",
    "[8] Risk-Based Auth. com ML — IEEE, 2018",
    "[9] Trust Engine Behavioral — IJETCSIT, 2024",
    "[10] AI Continuous Auth. Zero Trust — ResearchGate, 2025",
    "[11] XAI para IDS — Frontiers in AI, 2025",
    "[12] Adaptive Security + Drift — Al-Qadisiyah Journal, 2024",
    "[13] IBM Security Trusteer — ibm.com",
    "[14] BioCatch Behavioral Biometrics — biocatch.com",
    "[15] INPI Diretrizes de IA — gov.br/inpi, 2025",
]

add_bullet_list(slide, Inches(0.8), Inches(1.6), Inches(11.5), Inches(5.5), refs, font_size=12, color=CINZA_TEXTO, bullet_char="")

# =====================================================
# SLIDE 17 — ENCERRAMENTO
# =====================================================
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide, AZUL_ESCURO)
add_shape(slide, Inches(0), Inches(0), Inches(0.15), prs.slide_height, AZUL_DESTAQUE)

add_text(slide, Inches(1.5), Inches(2.5), Inches(10), Inches(1),
         "Obrigado!",
         font_size=44, color=BRANCO, bold=True, alignment=PP_ALIGN.CENTER)
add_shape(slide, Inches(5.5), Inches(3.6), Inches(2.3), Inches(0.04), AZUL_DESTAQUE)
add_text(slide, Inches(1.5), Inches(4.0), Inches(10), Inches(0.6),
         "Sistema Autonomo de Autenticacao Inteligente",
         font_size=18, color=RGBColor(0xAA,0xCC,0xEE), alignment=PP_ALIGN.CENTER)
add_text(slide, Inches(1.5), Inches(4.6), Inches(10), Inches(0.5),
         "Trabalhos Relacionados e Estado da Arte",
         font_size=14, color=RGBColor(0x88,0xAA,0xCC), alignment=PP_ALIGN.CENTER)

# =====================================================
# SALVAR
# =====================================================
output_path = r"C:\dev\projetos\sistema-autonomo\docs\trabalhos-relacionados.pptx"
prs.save(output_path)
print(f"Apresentacao salva em: {output_path}")
print(f"Total de slides: {len(prs.slides)}")
