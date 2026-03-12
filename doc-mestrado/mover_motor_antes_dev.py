import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')
body = doc.element.body
numbering = doc.part.numbering_part.numbering_definitions._numbering

# === Current order ===
# CAP 1 (190) → INTRODUÇÃO (196)
# CAP 2 (234) → FUNDAMENTAÇÃO TEÓRICA (240)
# CAP 3 (288) → DESENVOLVIMENTO (292)
# CAP 4 (441) → MOTOR DE IA (447)
# CAP 5 (559) → RESULTADOS PARCIAIS (560)
# CAP 6 (606) → CRONOGRAMA (610)
# CAP 7 (622) → REFERÊNCIAS (628)

# === Target order ===
# CAP 1 → INTRODUÇÃO
# CAP 2 → FUNDAMENTAÇÃO TEÓRICA
# CAP 3 → MOTOR DE IA
# CAP 4 → DESENVOLVIMENTO
# CAP 5 → RESULTADOS PARCIAIS
# CAP 6 → CRONOGRAMA
# CAP 7 → REFERÊNCIAS

# === STEP 1: Identify MOTOR chapter full range (box + desc + content) ===
# MOTOR: box at 441, desc at 443, heading at 447, content until before CAPÍTULO 5 box

# Find the last paragraph of MOTOR chapter (before CAPÍTULO 5 box at 559)
# Everything from 439 (2 before box) to just before the CAPÍTULO 5 stuff
# Let's find the first empty paragraph before CAPÍTULO 5 box that belongs to spacing

# MOTOR content ends at the last non-empty paragraph before RESULTADOS section
motor_range_start = 439  # 2 empties before the box
motor_range_end = None

# Find where CAPÍTULO 5 box area starts
for i in range(519, 560):
    p = doc.paragraphs[i]
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = [t.text for t in p._element.iter(qn('w:t')) if t.text and 'CAPÍTULO' in t.text]
        if texts:
            # Find start of this box section (go back to find empties)
            motor_range_end = i
            # Go back to find the start of spacing before CAPÍTULO 5
            for j in range(i-1, 518, -1):
                pj = doc.paragraphs[j]
                if pj.text and pj.text.strip():
                    motor_range_end = j + 1
                    break
            break

print(f"MOTOR chapter range: {motor_range_start} to {motor_range_end - 1}")

# Also find CAPÍTULO 3 box range (before DESENVOLVIMENTO)
# CAP 3 box at 288, DESENVOLVIMENTO heading at 292
# We need to insert MOTOR chapter elements before the CAPÍTULO 3 box area
# That means before paragraph 288 (or the empties before it)

# Find insertion point - before the empties that precede CAPÍTULO 3 box
insert_before_idx = None
for i in range(287, 280, -1):
    p = doc.paragraphs[i]
    if p.text and p.text.strip():
        insert_before_idx = i + 1
        break

if insert_before_idx is None:
    insert_before_idx = 286

print(f"Inserting before paragraph: {insert_before_idx}")

# === STEP 2: Collect MOTOR chapter elements ===
motor_elements = []
for i in range(motor_range_start, motor_range_end):
    motor_elements.append(doc.paragraphs[i]._element)

print(f"Moving {len(motor_elements)} paragraphs")

# Get insertion reference element
# We want to insert before the CAPÍTULO 3 (DESENVOLVIMENTO) box area
# The CAP 3 box is at 288, with empties at 286, 287 before it
ref_element = doc.paragraphs[286]._element

# === STEP 3: Move elements ===
for elem in motor_elements:
    ref_element.addprevious(elem)

print("Paragraphs moved!")

# === STEP 4: Update box numbers ===
print("\n=== Atualizando numeração dos boxes ===")

# Reload to get new indices
# Now: CAP 1, CAP 2, CAP 4(MOTOR), CAP 3(DEV), CAP 5(RES), CAP 6(CRONO), CAP 7(REF)
# Need: CAP 3(MOTOR), CAP 4(DEV), rest stays

for i, p in enumerate(doc.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = []
        for t in p._element.iter(qn('w:t')):
            if t.text and t.text.strip():
                texts.append(t.text.strip())
        combined = ' '.join(texts)
        if 'CAPÍTULO' not in combined:
            continue

        # Find which heading this box belongs to
        next_h1 = ''
        for j in range(i+1, min(i+15, len(doc.paragraphs))):
            if doc.paragraphs[j].style and doc.paragraphs[j].style.name == 'Heading 1' and doc.paragraphs[j].text.strip():
                next_h1 = doc.paragraphs[j].text.strip()
                break

        # Determine correct number
        if 'MOTOR' in next_h1:
            target_num = '3'
        elif 'DESENVOLVIMENTO' in next_h1:
            target_num = '4'
        elif 'RESULTADOS' in next_h1:
            target_num = '5'
        elif 'CRONOGRAMA' in next_h1:
            target_num = '6'
        elif 'REFER' in next_h1:
            target_num = '7'
        else:
            continue

        # Find current number and update
        current_num = None
        for t in p._element.iter(qn('w:t')):
            if t.text and t.text.strip().isdigit():
                current_num = t.text.strip()
                if current_num != target_num:
                    t.text = target_num

        if current_num and current_num != target_num:
            print(f"  CAPÍTULO {current_num} → {target_num} ({next_h1[:40]})")
        elif current_num:
            print(f"  CAPÍTULO {current_num} OK ({next_h1[:40]})")

# === STEP 5: Update numbering definitions ===
print("\n=== Atualizando numeração dos subcapítulos ===")

# Find which numId each chapter's H2s use
chapter_numids = {}
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Heading 2' and p.text.strip():
        pPr = p._element.find(qn('w:pPr'))
        if pPr is not None:
            numPr = pPr.find(qn('w:numPr'))
            if numPr is not None:
                numId = numPr.find(qn('w:numId'))
                if numId is not None:
                    nid = numId.get(qn('w:val'))
                    # Find parent chapter
                    for j in range(i-1, -1, -1):
                        pj = doc.paragraphs[j]
                        if pj.style and pj.style.name == 'Heading 1' and pj.text.strip():
                            chapter_numids[pj.text.strip()[:20]] = nid
                            break
                    # Only need first H2 per chapter
                    if len(chapter_numids) >= 5:
                        break

print(f"  Chapter numIds: {chapter_numids}")

# Map numId -> abstractNumId
numid_to_abstract = {}
for num in numbering.findall(qn('w:num')):
    nid = num.get(qn('w:numId'))
    abstract_ref = num.find(qn('w:abstractNumId'))
    if abstract_ref is not None:
        numid_to_abstract[nid] = abstract_ref.get(qn('w:val'))

# Update abstractNum start values
# MOTOR (was start=4, needs start=3)
# DESENVOLVIMENTO (was start=3, needs start=4)
# RESULTADOS (was start=5, stays 5)

for chapter_name, nid in chapter_numids.items():
    abstract_id = numid_to_abstract.get(nid)
    if abstract_id is None:
        continue

    if 'MOTOR' in chapter_name:
        target_start = '3'
    elif 'DESENVOLVIMENTO' in chapter_name:
        target_start = '4'
    elif 'RESULTADOS' in chapter_name:
        target_start = '5'
    elif 'CRONOGRAMA' in chapter_name:
        target_start = '6'
    else:
        continue

    for abstract in numbering.findall(qn('w:abstractNum')):
        if abstract.get(qn('w:abstractNumId')) == abstract_id:
            for lvl in abstract.findall(qn('w:lvl')):
                if lvl.get(qn('w:ilvl')) == '0':
                    start = lvl.find(qn('w:start'))
                    if start is not None:
                        old_val = start.get(qn('w:val'))
                        start.set(qn('w:val'), target_start)
                        print(f"  {chapter_name}: abstractNum {abstract_id} start {old_val} → {target_start}")

# === STEP 6: Update TOC entries ===
print("\n=== Atualizando sumário ===")

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

    # Find MOTOR and DESENVOLVIMENTO TOC entry groups
    motor_toc_start = None
    motor_toc_end = None
    dev_toc_start = None
    dev_toc_end = None

    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'MOTOR DE INTELIG' in text:
            motor_toc_start = idx
        elif 'DESENVOLVIMENTO' in text and motor_toc_start is not None and motor_toc_end is None:
            # This shouldn't happen since DESENVOLVIMENTO was before MOTOR in TOC
            pass
        elif 'DESENVOLVIMENTO' in text and motor_toc_start is None:
            dev_toc_start = idx
        elif 'MOTOR DE INTELIG' in text:
            motor_toc_start = idx

    # Re-scan properly
    motor_toc_start = None
    dev_toc_start = None
    resultados_toc_start = None

    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'DESENVOLVIMENTO' in text:
            dev_toc_start = idx
        elif 'MOTOR DE INTELIG' in text:
            motor_toc_start = idx
        elif 'RESULTADOS' in text:
            resultados_toc_start = idx

    print(f"  TOC: DEV={dev_toc_start}, MOTOR={motor_toc_start}, RES={resultados_toc_start}")

    if dev_toc_start is not None and motor_toc_start is not None:
        # MOTOR entries go from motor_toc_start to resultados_toc_start-1
        # DEV entries go from dev_toc_start to motor_toc_start-1
        motor_toc_end = resultados_toc_start
        dev_toc_end = motor_toc_start

        # Collect elements
        motor_toc_elems = [paras[i] for i in range(motor_toc_start, motor_toc_end)]
        dev_toc_elems = [paras[i] for i in range(dev_toc_start, dev_toc_end)]

        # Move MOTOR entries before DEV entries
        first_dev = dev_toc_elems[0]
        for elem in motor_toc_elems:
            first_dev.addprevious(elem)

        print(f"  Moved {len(motor_toc_elems)} MOTOR entries before {len(dev_toc_elems)} DEV entries")

        # Update numbering in TOC
        # MOTOR: was 4.x → 3.x
        for elem in motor_toc_elems:
            for t_elem in elem.iter(qn('w:t')):
                if t_elem.text:
                    t_elem.text = (t_elem.text
                        .replace('4.1', '3.1').replace('4.2', '3.2').replace('4.3', '3.3')
                        .replace('4.4', '3.4').replace('4.5', '3.5').replace('4.6', '3.6'))

        # DEV: was 3.x → 4.x
        for elem in dev_toc_elems:
            for t_elem in elem.iter(qn('w:t')):
                if t_elem.text:
                    t_elem.text = (t_elem.text
                        .replace('3.1', '4.1').replace('3.2', '4.2').replace('3.3', '4.3')
                        .replace('3.4', '4.4').replace('3.5', '4.5').replace('3.6', '4.6')
                        .replace('3.7', '4.7').replace('3.8', '4.8').replace('3.9', '4.9'))

        print("  TOC numbering updated")

    break

# === Save ===
doc.save('doc-qualificacao-ifba-12-03-2026.docx')

# === Verify ===
print("\n=== Verificação final ===")
doc2 = Document('doc-qualificacao-ifba-12-03-2026.docx')

print("Chapters:")
for i, p in enumerate(doc2.paragraphs):
    if p.style and p.style.name == 'Heading 1' and p.text.strip() and i > 190:
        print(f"  {i}: {p.text[:60]}")

print("\nBoxes:")
for i, p in enumerate(doc2.paragraphs):
    xml_str = etree.tostring(p._element, pretty_print=True).decode()
    if 'w:drawing' in xml_str:
        texts = [t.text for t in p._element.iter(qn('w:t')) if t.text and t.text.strip()]
        combined = ' '.join(texts)
        if 'CAPÍTULO' in combined:
            for j in range(i+1, min(i+15, len(doc2.paragraphs))):
                if doc2.paragraphs[j].style and doc2.paragraphs[j].style.name == 'Heading 1' and doc2.paragraphs[j].text.strip():
                    print(f"  {combined} → {doc2.paragraphs[j].text[:50]}")
                    break

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

print("\nDocumento salvo!")
