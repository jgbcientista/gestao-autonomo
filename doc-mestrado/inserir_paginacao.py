import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.shared import Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')

# =============================================
# ABNT NBR 14724: Numeração de páginas
# - Contagem começa na folha de rosto (seção 1)
# - Numeração visível a partir da primeira página textual (INTRODUÇÃO)
# - Posição: canto superior direito
# - Fonte: Arial 10pt
# =============================================

# Sections:
# 0: Capa (não conta)
# 1: Folha de rosto (conta, não mostra)
# 2: Ficha catalográfica (conta, não mostra)
# 3: Resumo (conta, não mostra)
# 4: Abstract (conta, não mostra)
# 5: Listas + INTRODUÇÃO (a partir daqui mostra)
# 6: FUNDAMENTAÇÃO TEÓRICA
# 7: MOTOR DE IA
# 8: DESENVOLVIMENTO + RESULTADOS
# 9: CRONOGRAMA
# 10: REFERÊNCIAS

def create_page_number_paragraph(font_name='Arial', font_size=10):
    """Create a paragraph with PAGE field, right-aligned"""
    p = OxmlElement('w:p')

    # Paragraph properties - right aligned
    pPr = OxmlElement('w:pPr')
    jc = OxmlElement('w:jc')
    jc.set(qn('w:val'), 'right')
    pPr.append(jc)
    p.append(pPr)

    # Run properties template
    def make_rPr():
        rPr = OxmlElement('w:rPr')
        rFonts = OxmlElement('w:rFonts')
        rFonts.set(qn('w:ascii'), font_name)
        rFonts.set(qn('w:hAnsi'), font_name)
        rPr.append(rFonts)
        sz = OxmlElement('w:sz')
        sz.set(qn('w:val'), str(font_size * 2))  # half-points
        rPr.append(sz)
        szCs = OxmlElement('w:szCs')
        szCs.set(qn('w:val'), str(font_size * 2))
        rPr.append(szCs)
        return rPr

    # fldChar begin
    r1 = OxmlElement('w:r')
    r1.append(make_rPr())
    fldBegin = OxmlElement('w:fldChar')
    fldBegin.set(qn('w:fldCharType'), 'begin')
    r1.append(fldBegin)
    p.append(r1)

    # instrText PAGE
    r2 = OxmlElement('w:r')
    r2.append(make_rPr())
    instrText = OxmlElement('w:instrText')
    instrText.set(qn('xml:space'), 'preserve')
    instrText.text = ' PAGE '
    r2.append(instrText)
    p.append(r2)

    # fldChar separate
    r3 = OxmlElement('w:r')
    r3.append(make_rPr())
    fldSep = OxmlElement('w:fldChar')
    fldSep.set(qn('w:fldCharType'), 'separate')
    r3.append(fldSep)
    p.append(r3)

    # Page number text (placeholder)
    r4 = OxmlElement('w:r')
    r4.append(make_rPr())
    t = OxmlElement('w:t')
    t.text = '1'
    r4.append(t)
    p.append(r4)

    # fldChar end
    r5 = OxmlElement('w:r')
    r5.append(make_rPr())
    fldEnd = OxmlElement('w:fldChar')
    fldEnd.set(qn('w:fldCharType'), 'end')
    r5.append(fldEnd)
    p.append(r5)

    return p

# === Process each section ===
for si, section in enumerate(doc.sections):
    header = section.header

    if si <= 4:
        # Pre-text sections: no visible page number
        # Unlink from previous to control independently
        header.is_linked_to_previous = False
        # Clear any existing content
        for p in header.paragraphs:
            for elem in list(p._element):
                if elem.tag != qn('w:pPr'):
                    p._element.remove(elem)
        print(f"Seção {si}: sem numeração (pré-textual)")

    elif si >= 5:
        # Text sections: show page number at top-right
        header.is_linked_to_previous = False

        # Clear existing header content
        header_elem = header._element
        # Remove all existing paragraphs
        for p_elem in header_elem.findall(qn('w:p')):
            header_elem.remove(p_elem)

        # Add page number paragraph
        page_num_p = create_page_number_paragraph()
        header_elem.append(page_num_p)

        print(f"Seção {si}: numeração inserida (canto superior direito, Arial 10pt)")

# === Also clean up any existing footer page numbers to avoid duplication ===
print("\n=== Limpando rodapés com numeração duplicada ===")
for si, section in enumerate(doc.sections):
    footer = section.footer
    footer.is_linked_to_previous = False

    # Check if footer has PAGE field
    has_page = False
    for p in footer.paragraphs:
        xml = p._element.xml if hasattr(p._element, 'xml') else ''
        for elem in p._element.iter(qn('w:instrText')):
            if elem.text and 'PAGE' in elem.text:
                has_page = True
                break

    if has_page:
        # Clear footer
        footer_elem = footer._element
        for p_elem in footer_elem.findall(qn('w:p')):
            for elem in list(p_elem):
                if elem.tag != qn('w:pPr'):
                    p_elem.remove(elem)
        print(f"  Seção {si}: rodapé com PAGE limpo")

doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print("\nDocumento salvo com numeração de páginas!")
print("ABNT: numeração no canto superior direito, Arial 10pt, a partir da INTRODUÇÃO")
