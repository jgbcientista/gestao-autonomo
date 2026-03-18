# -*- coding: utf-8 -*-
import sys
sys.stdout.reconfigure(encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn

doc = Document(r'C:\dev\mestrado\qualificacao-ifba-2025-FINAL-BACKUP.docx')

print(f"Tabelas no BACKUP: {len(doc.tables)}")

body = doc.element.body
tables = body.findall(qn('w:tbl'))
print(f"Tabelas via XML no BACKUP: {len(tables)}")

for idx, tbl in enumerate(tables):
    rows = tbl.findall(qn('w:tr'))
    print(f"\nTabela {idx+1}: {len(rows)} linhas")
    for j, row in enumerate(rows):
        cells = row.findall(qn('w:tc'))
        cell_texts = []
        for cell in cells:
            text = ''
            for p in cell.findall(qn('w:p')):
                for r in p.findall(qn('w:r')):
                    for t in r.findall(qn('w:t')):
                        text += t.text or ''
            cell_texts.append(text.strip()[:20])
        print(f"  Linha {j}: {cell_texts}")
