import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.shared import Pt, Cm, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')

# =============================================
# ABNT NBR 14724 / NBR 6024 compliance fixes
# =============================================

# === 1. FIX MARGINS: top=3cm, bottom=2cm, left=3cm, right=2cm ===
print("=== 1. Corrigindo margens ===")
for si, section in enumerate(doc.sections):
    old = (
        round(section.top_margin / 914400 * 2.54, 1),
        round(section.bottom_margin / 914400 * 2.54, 1),
        round(section.left_margin / 914400 * 2.54, 1),
        round(section.right_margin / 914400 * 2.54, 1),
    )

    section.top_margin = Cm(3)
    section.bottom_margin = Cm(2)
    section.left_margin = Cm(3)
    section.right_margin = Cm(2)

    new = (3.0, 2.0, 3.0, 2.0)
    if old != new:
        print(f"  Seção {si}: ({old[0]}, {old[1]}, {old[2]}, {old[3]}) → (3.0, 2.0, 3.0, 2.0) cm")

# === 2. FIX LINE SPACING: all body text should be 1.5 ===
print("\n=== 2. Corrigindo espaçamento entre linhas ===")
fixed_spacing = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Body Text' and p.text.strip():
        ls = p.paragraph_format.line_spacing
        if ls is None or abs(ls - 1.5) > 0.01:
            p.paragraph_format.line_spacing = 1.5
            fixed_spacing += 1
print(f"  {fixed_spacing} parágrafos corrigidos para espaçamento 1.5")

# === 3. FIX FIRST LINE INDENT: 1.25cm (ABNT) for body text ===
print("\n=== 3. Corrigindo recuo de primeira linha ===")
INDENT_ABNT = Cm(1.25)
fixed_indent = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 30:
        fi = p.paragraph_format.first_line_indent
        if fi is not None:
            cm_val = round(fi / 914400 * 2.54, 2)
            if abs(cm_val - 1.25) > 0.1 and abs(cm_val - 1.27) > 0.1:
                p.paragraph_format.first_line_indent = INDENT_ABNT
                fixed_indent += 1
        else:
            # No indent set - add it
            p.paragraph_format.first_line_indent = INDENT_ABNT
            fixed_indent += 1
print(f"  {fixed_indent} parágrafos corrigidos para recuo 1.25 cm")

# === 4. FIX ALIGNMENT: body text should be JUSTIFY ===
print("\n=== 4. Corrigindo alinhamento ===")
fixed_align = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 30:
        if p.paragraph_format.alignment != WD_ALIGN_PARAGRAPH.JUSTIFY:
            p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
            fixed_align += 1
print(f"  {fixed_align} parágrafos corrigidos para justificado")

# === 5. FIX MOTOR H1 to match other H1 pattern ===
print("\n=== 5. Padronizando Heading 1 do MOTOR DE IA ===")
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Heading 1' and 'MOTOR DE INTELIG' in p.text:
        # Remove extra spacing that was added programmatically
        p.paragraph_format.space_before = None
        p.paragraph_format.space_after = None
        p.paragraph_format.line_spacing = None
        # Match alignment with other H1s (RIGHT in this document)
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        print(f"  Para {i}: removido espaçamento extra, alinhamento → RIGHT")
        break

# Also fix DESENVOLVIMENTO and CRONOGRAMA H1 alignment
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Heading 1' and p.text.strip() and i > 190:
        al = p.paragraph_format.alignment
        if al != WD_ALIGN_PARAGRAPH.RIGHT and al is not None:
            # Some H1s have different alignment
            pass  # Don't force - check individually

# === 6. FIX HEADING 2 spacing (ABNT: space before and after sections) ===
print("\n=== 6. Verificando Heading 2 ===")
# H2 in MOTOR chapter had extra formatting from programmatic insertion
fixed_h2 = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Heading 2' and p.text.strip():
        fmt = p.paragraph_format
        # Remove any programmatically added spacing that doesn't match the pattern
        if fmt.space_before is not None or fmt.space_after is not None:
            # Check if this is in the MOTOR chapter (roughly 294-393)
            if 294 <= i <= 393:
                fmt.space_before = None
                fmt.space_after = None
                fmt.line_spacing = None
                fmt.first_line_indent = None
                fixed_h2 += 1
print(f"  {fixed_h2} Heading 2 do MOTOR corrigidos")

# === 7. FIX FONT: ensure Arial 12pt on all body runs ===
print("\n=== 7. Verificando fonte ===")
fixed_font = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Body Text' and p.text.strip():
        for run in p.runs:
            if run.font.name and run.font.name != 'Arial':
                run.font.name = 'Arial'
                fixed_font += 1
            # Size is defined at style level (12pt = 152400 EMU), don't override
print(f"  {fixed_font} runs com fonte incorreta corrigidos")

# === 8. SET paragraph spacing (ABNT: 0pt before/after for body text) ===
print("\n=== 8. Corrigindo espaçamento entre parágrafos ===")
fixed_para_spacing = 0
for i, p in enumerate(doc.paragraphs):
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 30:
        fmt = p.paragraph_format
        # ABNT: no extra spacing between paragraphs (spacing is via line spacing 1.5)
        if fmt.space_after is not None and fmt.space_after != Pt(0):
            fmt.space_after = Pt(0)
            fixed_para_spacing += 1
        if fmt.space_before is not None and fmt.space_before != Pt(0):
            # Don't zero out space_before for first paragraph after heading
            fmt.space_before = Pt(0)
            fixed_para_spacing += 1
print(f"  {fixed_para_spacing} parágrafos com espaçamento extra corrigidos")

# === Save ===
doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print("\n=== DOCUMENTO SALVO ===")

# === Final verification ===
print("\n=== VERIFICAÇÃO FINAL ===")
doc2 = Document('doc-qualificacao-ifba-12-03-2026.docx')

# Margins
s = doc2.sections[0]
print(f"Margens: sup={s.top_margin/914400*2.54:.1f} inf={s.bottom_margin/914400*2.54:.1f} "
      f"esq={s.left_margin/914400*2.54:.1f} dir={s.right_margin/914400*2.54:.1f} cm")

# Line spacing
wrong_ls = 0
for p in doc2.paragraphs:
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 50:
        ls = p.paragraph_format.line_spacing
        if ls and abs(ls - 1.5) > 0.01:
            wrong_ls += 1
print(f"Parágrafos com espaçamento != 1.5: {wrong_ls}")

# Indent
wrong_indent = 0
for p in doc2.paragraphs:
    if p.style and p.style.name == 'Body Text' and p.text.strip() and len(p.text) > 50:
        fi = p.paragraph_format.first_line_indent
        if fi:
            cm = fi / 914400 * 2.54
            if abs(cm - 1.25) > 0.1 and abs(cm - 1.27) > 0.1:
                wrong_indent += 1
print(f"Parágrafos com recuo fora do padrão: {wrong_indent}")
