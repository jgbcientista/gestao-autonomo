import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from lxml import etree

DOCX_PATH = 'doc-mestrado/doc-qualificacao-ifba.docx'
doc = Document(DOCX_PATH)
body = doc.element.body

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

    # Remove the old broken H1 entry "Motor De Inteligência..." (mixed case, with page 16)
    print("=== Removendo entrada H1 duplicada ===")
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        # The old one has "Motor De" (title case) vs the new "MOTOR DE" (upper case)
        if 'Motor De Intelig' in text:
            sdtContent.remove(p)
            print(f"  Removida: {text[:60]}")
            break

    # Fix CRONOGRAMA: 5.1 should be 6.1
    paras = list(sdtContent.findall(qn('w:p')))
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if '5.1' in text and 'Cronograma' in text:
            for t_el in p.iter(qn('w:t')):
                if t_el.text and '5.1' in t_el.text:
                    t_el.text = t_el.text.replace('5.1', '6.1')
                    print(f"  CRONOGRAMA: 5.1 → 6.1")

    break

doc.save(DOCX_PATH)

# Verify
print("\n=== TOC Final ===")
doc2 = Document(DOCX_PATH)
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

print(f"\nSalvo: {DOCX_PATH}")
