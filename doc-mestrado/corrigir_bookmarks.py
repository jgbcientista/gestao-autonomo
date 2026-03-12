import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

DOCX_PATH = 'doc-mestrado/doc-qualificacao-ifba.docx'
doc = Document(DOCX_PATH)

# Find max bookmark ID in document
max_bm_id = 0
for bm in doc.element.body.iter(qn('w:bookmarkStart')):
    bm_id = int(bm.get(qn('w:id'), '0'))
    if bm_id > max_bm_id:
        max_bm_id = bm_id
print(f"Max bookmark ID: {max_bm_id}")

# Remove old empty bookmark _Toc223603190 if it exists on wrong paragraph
for bm in doc.element.body.iter(qn('w:bookmarkStart')):
    name = bm.get(qn('w:name'))
    if name == '_Toc223603190':
        parent = bm.getparent()
        text = ''.join(t.text for t in parent.iter(qn('w:t')) if t.text)
        if 'MOTOR' not in text.upper():
            # Remove this misplaced bookmark
            bm_id = bm.get(qn('w:id'))
            parent.remove(bm)
            # Also remove matching bookmarkEnd
            for bme in doc.element.body.iter(qn('w:bookmarkEnd')):
                if bme.get(qn('w:id')) == bm_id:
                    bme.getparent().remove(bme)
            print(f"  Removed misplaced _Toc223603190 from: '{text[:40]}'")

# Bookmark mapping: heading text -> bookmark name
bookmark_map = [
    (289, '_Toc223603190'),  # MOTOR H1
    (292, '_Toc223603191'),  # 3.1 Arquitetura
    (305, '_Toc223603192'),  # 3.2 Critérios
    (318, '_Toc223603193'),  # 3.3 Processo
    (329, '_Toc223603194'),  # 3.4 Limiares
    (340, '_Toc223603195'),  # 3.5 Pipeline
    (351, '_Toc223603196'),  # 3.6 Integração
]

print("\n=== Adicionando bookmarks ===")
next_id = max_bm_id + 1

for para_idx, bm_name in bookmark_map:
    p = doc.paragraphs[para_idx]

    # Check if bookmark already exists
    existing = [bm.get(qn('w:name')) for bm in p._element.iter(qn('w:bookmarkStart'))]
    if bm_name in existing:
        print(f"  {para_idx}: {bm_name} já existe")
        continue

    # Add bookmarkStart at the beginning of the paragraph (after pPr)
    bm_start = OxmlElement('w:bookmarkStart')
    bm_start.set(qn('w:id'), str(next_id))
    bm_start.set(qn('w:name'), bm_name)

    bm_end = OxmlElement('w:bookmarkEnd')
    bm_end.set(qn('w:id'), str(next_id))

    # Insert after pPr
    pPr = p._element.find(qn('w:pPr'))
    if pPr is not None:
        pPr.addnext(bm_start)
    else:
        p._element.insert(0, bm_start)

    # Add bookmarkEnd at end of paragraph
    p._element.append(bm_end)

    print(f"  {para_idx}: {bm_name} adicionado (id={next_id}) -> {p.text[:50]}")
    next_id += 1

# Also fix the H1 text to be all uppercase (matching the TOC)
p289 = doc.paragraphs[289]
for r in p289._element.findall(qn('w:r')):
    for t in r.findall(qn('w:t')):
        if t.text and 'motor' in t.text.lower():
            old = t.text
            t.text = t.text.upper()
            print(f"\n  H1 text: '{old[:50]}' -> '{t.text[:50]}'")

doc.save(DOCX_PATH)

# Verify
print("\n=== Verificação ===")
doc2 = Document(DOCX_PATH)
for i, p in enumerate(doc2.paragraphs):
    if p.style and p.style.name in ('Heading 1', 'Heading 2') and i >= 289 and i <= 360:
        bm_names = [bm.get(qn('w:name')) for bm in p._element.iter(qn('w:bookmarkStart'))]
        if bm_names:
            print(f"  {i}: {p.text[:50]} -> {bm_names}")

print(f"\nSalvo: {DOCX_PATH}")
