import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')
body = doc.element.body

# === Current state ===
# Box 441 (CAPÍTULO 4) + desc 443 = "resultados parciais..." → now before MOTOR → needs to say MOTOR stuff
# No box before RESULTADOS PARCIAIS (495) → needs CAPÍTULO 5 box + desc
# Box 541 (CAPÍTULO 5) before CRONOGRAMA → change to CAPÍTULO 6
# Box 558 (CAPÍTULO 6) before REFERÊNCIAS → change to CAPÍTULO 7

# === STEP 1: Fix CAPÍTULO 4 description (para 443) to match MOTOR chapter ===
print("=== Atualizando descrição do CAPÍTULO 4 (MOTOR DE IA) ===")
p443 = doc.paragraphs[443]
print(f"  Texto antigo: {p443.text[:80]}")

# Clear existing runs and set new text
for run in p443.runs:
    run._element.getparent().remove(run._element)

# Also clear any direct text
for t in p443._element.findall(qn('w:r')):
    p443._element.remove(t)

# Add new run with text
new_run = OxmlElement('w:r')
rPr = OxmlElement('w:rPr')
rFonts = OxmlElement('w:rFonts')
rFonts.set(qn('w:ascii'), 'Arial')
rFonts.set(qn('w:hAnsi'), 'Arial')
rPr.append(rFonts)
italic = OxmlElement('w:i')
rPr.append(italic)
sz = OxmlElement('w:sz')
sz.set(qn('w:val'), '24')  # 12pt = 24 half-points
rPr.append(sz)
new_run.append(rPr)
t = OxmlElement('w:t')
t.set(qn('xml:space'), 'preserve')
t.text = "Neste capítulo será apresentado em profundidade o motor de Inteligência Artificial, incluindo a arquitetura do ensemble de modelos, as 14 features comportamentais, o processo de treinamento com dados reais, os limiares de decisão e a integração com blockchain para auditoria."
new_run.append(t)
p443._element.append(new_run)
print(f"  Texto novo: {t.text[:80]}...")

# === STEP 2: Create CAPÍTULO 5 box + description before RESULTADOS PARCIAIS (495) ===
print("\n=== Criando CAPÍTULO 5 box antes de RESULTADOS PARCIAIS ===")

# Use box from para 441 as template
box_template = doc.paragraphs[441]._element
desc_template = doc.paragraphs[443]._element

# Create a deep copy of the box
new_box = copy.deepcopy(box_template)

# Change the text "4" to "5" inside the shape
for t_elem in new_box.iter(qn('w:t')):
    if t_elem.text and t_elem.text.strip() == '4':
        t_elem.text = '5'

# Create description paragraph
new_desc = OxmlElement('w:p')
# Copy pPr from description template
desc_pPr = desc_template.find(qn('w:pPr'))
if desc_pPr is not None:
    new_desc.append(copy.deepcopy(desc_pPr))

# Add text run
desc_run = OxmlElement('w:r')
desc_rPr = OxmlElement('w:rPr')
rFonts2 = OxmlElement('w:rFonts')
rFonts2.set(qn('w:ascii'), 'Arial')
rFonts2.set(qn('w:hAnsi'), 'Arial')
desc_rPr.append(rFonts2)
italic2 = OxmlElement('w:i')
desc_rPr.append(italic2)
sz2 = OxmlElement('w:sz')
sz2.set(qn('w:val'), '24')
desc_rPr.append(sz2)
desc_run.append(desc_rPr)
desc_t = OxmlElement('w:t')
desc_t.set(qn('xml:space'), 'preserve')
desc_t.text = "Neste capítulo será apresentado os resultados parciais obtidos durante a construção e as dificuldades encontradas."
desc_run.append(desc_t)
new_desc.append(desc_run)

# Create empty paragraphs for spacing (matching the pattern)
def make_empty_para(style_name='Body Text'):
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    pStyle = OxmlElement('w:pStyle')
    pStyle.set(qn('w:val'), style_name)
    pPr.append(pStyle)
    p.append(pPr)
    return p

# Find RESULTADOS PARCIAIS heading element
resultados_elem = doc.paragraphs[495]._element

# Insert in order before RESULTADOS: empty, empty, box, empty, desc, empty, empty, empty, heading
# Pattern from CAP 3: box(288), empty(289), desc(290), empty(291), HEADING(292)

# Insert before RESULTADOS heading: empty, empty, empty, desc, empty, box, empty, empty
# Actually looking at the pattern more carefully:
# box → empty → desc → empty → (maybe more empties) → HEADING

# Let's insert: empty, box, empty, desc, empty, empty, empty before RESULTADOS
empty1 = make_empty_para('Body Text')
empty2 = make_empty_para('Body Text')
empty3 = make_empty_para('Body Text')
empty4 = make_empty_para('Body Text')
empty5 = make_empty_para('Body Text')

# Insert in reverse order since addprevious adds right before the element
resultados_elem.addprevious(empty5)
resultados_elem.addprevious(empty4)
resultados_elem.addprevious(empty3)
resultados_elem.addprevious(new_desc)
resultados_elem.addprevious(empty2)
resultados_elem.addprevious(new_box)
resultados_elem.addprevious(empty1)

print("  Box CAPÍTULO 5 e descrição inseridos")

# === STEP 3: Update CAPÍTULO 5 → 6 (before CRONOGRAMA) ===
print("\n=== Atualizando CAPÍTULO 5 → 6 (CRONOGRAMA) ===")

# Need to re-find since indices shifted
for i, p in enumerate(doc.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = []
        for t_elem in p._element.iter(qn('w:t')):
            if t_elem.text and t_elem.text.strip():
                texts.append(t_elem.text.strip())
        combined = ' '.join(texts)
        if 'CAPÍTULO' in combined:
            # Find the next H1 heading
            next_h1 = ''
            for j in range(i+1, min(i+15, len(doc.paragraphs))):
                if doc.paragraphs[j].style and doc.paragraphs[j].style.name == 'Heading 1' and doc.paragraphs[j].text.strip():
                    next_h1 = doc.paragraphs[j].text.strip()
                    break

            if 'CRONOGRAMA' in next_h1:
                # Change 5 to 6
                for t_elem in p._element.iter(qn('w:t')):
                    if t_elem.text and t_elem.text.strip() == '5':
                        t_elem.text = '6'
                print(f"  CAPÍTULO 5 → 6 (antes de CRONOGRAMA)")

                # Also update description if exists
                for j in range(i+1, min(i+6, len(doc.paragraphs))):
                    pj = doc.paragraphs[j]
                    if pj.style and pj.style.name == 'Normal' and pj.text.strip():
                        # This might have the wrong text - but let's keep it
                        print(f"  Descrição CRONOGRAMA: {pj.text[:60]}")
                        break

            elif 'REFER' in next_h1:
                # Change 6 to 7
                for t_elem in p._element.iter(qn('w:t')):
                    if t_elem.text and t_elem.text.strip() == '6':
                        t_elem.text = '7'
                print(f"  CAPÍTULO 6 → 7 (antes de REFERÊNCIAS)")

# === STEP 4: Fix the misplaced description near CRONOGRAMA ===
# Para 545 has "O score final do ensemble..." which is leftover content
# Let's check and clean up
print("\n=== Verificando área do CRONOGRAMA ===")
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Heading 1' and 'CRONOGRAMA' in p.text:
        print(f"  CRONOGRAMA encontrado em {i}")
        for j in range(max(0, i-8), i+3):
            pj = doc.paragraphs[j]
            text = pj.text[:70] if pj.text else '(empty)'
            xml_str = etree.tostring(pj._element, pretty_print=True).decode()
            has_drawing = 'w:drawing' in xml_str
            print(f"    {j}: [{pj.style.name}] {text} {'[BOX]' if has_drawing else ''}")
        break

# === Save ===
doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print("\n=== Verificação final ===")

doc2 = Document('doc-qualificacao-ifba-12-03-2026.docx')
print("Boxes encontrados:")
for i, p in enumerate(doc2.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = []
        for t_elem in p._element.iter(qn('w:t')):
            if t_elem.text and t_elem.text.strip():
                texts.append(t_elem.text.strip())
        combined = ' '.join(texts)
        if 'CAPÍTULO' in combined:
            # Find next H1
            for j in range(i+1, min(i+15, len(doc2.paragraphs))):
                if doc2.paragraphs[j].style and doc2.paragraphs[j].style.name == 'Heading 1' and doc2.paragraphs[j].text.strip():
                    print(f"  {combined} → {doc2.paragraphs[j].text[:50]}")
                    break

print("\nDocumento salvo com sucesso!")
