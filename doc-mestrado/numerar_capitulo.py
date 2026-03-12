import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')
numbering = doc.part.numbering_part.numbering_definitions._numbering

# === STEP 1: Create new abstractNum for chapter 5 ===
# Copy structure from abstractNum 36 (chapter 4, start=4) but change start to 5
print("=== Criando definição de numeração para capítulo 5 ===")

NEW_ABSTRACT_ID = '37'
NEW_NUM_ID = '38'

# Find abstractNum 36 as reference
ref_abstract = None
for abstract in numbering.findall(qn('w:abstractNum')):
    if abstract.get(qn('w:abstractNumId')) == '36':
        ref_abstract = abstract
        break

if ref_abstract is None:
    print("ERRO: abstractNum 36 não encontrado!")
    sys.exit(1)

# Deep copy and modify
new_abstract = copy.deepcopy(ref_abstract)
new_abstract.set(qn('w:abstractNumId'), NEW_ABSTRACT_ID)

# Change start value for ilvl=0 from 4 to 5
for lvl in new_abstract.findall(qn('w:lvl')):
    ilvl = lvl.get(qn('w:ilvl'))
    if ilvl == '0':
        start = lvl.find(qn('w:start'))
        if start is not None:
            start.set(qn('w:val'), '5')
            print(f"  abstractNum {NEW_ABSTRACT_ID}: ilvl=0 start=5")

# Remove nsid and multiLevelType duplicates that might cause issues
# Set a unique nsid
nsid = new_abstract.find(qn('w:nsid'))
if nsid is not None:
    nsid.set(qn('w:val'), '5A1B2C3D')

# Add to numbering
numbering.append(new_abstract)
print(f"  abstractNum {NEW_ABSTRACT_ID} criado")

# Create new num element
new_num = OxmlElement('w:num')
new_num.set(qn('w:numId'), NEW_NUM_ID)
abstract_ref = OxmlElement('w:abstractNumId')
abstract_ref.set(qn('w:val'), NEW_ABSTRACT_ID)
new_num.append(abstract_ref)
numbering.append(new_num)
print(f"  num {NEW_NUM_ID} -> abstractNum {NEW_ABSTRACT_ID}")

# === STEP 2: Add numPr to new chapter H2 headings ===
print("\n=== Adicionando numeração aos headings do capítulo 5 ===")

# Find the new chapter H2 headings (between para 498 and 546)
new_h2_indices = []
for i, p in enumerate(doc.paragraphs):
    if i > 498 and i < 546 and p.style and p.style.name == 'Heading 2' and p.text.strip():
        new_h2_indices.append(i)

for idx in new_h2_indices:
    p = doc.paragraphs[idx]
    pPr = p._element.find(qn('w:pPr'))
    if pPr is None:
        pPr = OxmlElement('w:pPr')
        p._element.insert(0, pPr)

    # Check if numPr already exists
    existing_numPr = pPr.find(qn('w:numPr'))
    if existing_numPr is not None:
        print(f"  {idx}: já tem numPr, pulando ({p.text[:40]})")
        continue

    # Create numPr
    numPr = OxmlElement('w:numPr')
    ilvl = OxmlElement('w:ilvl')
    ilvl.set(qn('w:val'), '1')
    numId = OxmlElement('w:numId')
    numId.set(qn('w:val'), NEW_NUM_ID)
    numPr.append(ilvl)
    numPr.append(numId)

    # Insert numPr right after pStyle (if exists)
    pStyle = pPr.find(qn('w:pStyle'))
    if pStyle is not None:
        pStyle.addnext(numPr)
    else:
        pPr.insert(0, numPr)

    print(f"  {idx}: numPr adicionado (numId={NEW_NUM_ID}, ilvl=1) -> {p.text[:40]}")

# === STEP 3: Update CRONOGRAMA numbering from start=5 to start=6 ===
print("\n=== Atualizando numeração do CRONOGRAMA (5 -> 6) ===")

# CRONOGRAMA uses numId=3, abstractNumId=15
for abstract in numbering.findall(qn('w:abstractNum')):
    if abstract.get(qn('w:abstractNumId')) == '15':
        for lvl in abstract.findall(qn('w:lvl')):
            ilvl = lvl.get(qn('w:ilvl'))
            if ilvl == '0':
                start = lvl.find(qn('w:start'))
                if start is not None:
                    old_val = start.get(qn('w:val'))
                    start.set(qn('w:val'), '6')
                    print(f"  abstractNum 15: ilvl=0 start={old_val} -> 6")

# === STEP 4: Save ===
doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print("\nDocumento salvo!")

# === Verify ===
print("\n=== Verificação ===")
doc2 = Document('doc-qualificacao-ifba-12-03-2026.docx')
for i, p in enumerate(doc2.paragraphs):
    if p.style and p.style.name == 'Heading 2' and p.text.strip() and i > 490 and i < 560:
        pPr = p._element.find(qn('w:pPr'))
        if pPr is not None:
            numPr_elem = pPr.find(qn('w:numPr'))
            if numPr_elem is not None:
                nid = numPr_elem.find(qn('w:numId'))
                ilvl_el = numPr_elem.find(qn('w:ilvl'))
                print(f"  {i}: numId={nid.get(qn('w:val')) if nid is not None else '?'} ilvl={ilvl_el.get(qn('w:val')) if ilvl_el is not None else '?'} -> {p.text[:50]}")
            else:
                print(f"  {i}: SEM numPr -> {p.text[:50]}")
