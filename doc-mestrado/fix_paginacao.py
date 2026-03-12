import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

DOCX_PATH = 'doc-mestrado/doc-qualificacao-ifba.docx'
doc = Document(DOCX_PATH)
part = doc.part

def create_page_number_paragraph_center():
    """PAGE field, centered, Arial 10pt - for footer"""
    p = OxmlElement('w:p')
    pPr = OxmlElement('w:pPr')
    jc = OxmlElement('w:jc')
    jc.set(qn('w:val'), 'center')
    pPr.append(jc)
    p.append(pPr)

    def make_rPr():
        rPr = OxmlElement('w:rPr')
        rFonts = OxmlElement('w:rFonts')
        rFonts.set(qn('w:ascii'), 'Arial')
        rFonts.set(qn('w:hAnsi'), 'Arial')
        rPr.append(rFonts)
        sz = OxmlElement('w:sz')
        sz.set(qn('w:val'), '20')  # 10pt
        rPr.append(sz)
        szCs = OxmlElement('w:szCs')
        szCs.set(qn('w:val'), '20')
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

    # placeholder text
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

# === 1. Remove PAGE from ALL headers ===
print("=== Removendo PAGE de todos os headers ===")
for rid, rel in part.rels.items():
    target = rel.target_part
    ct = target.content_type if hasattr(target, 'content_type') else ''
    if 'header' in ct.lower():
        hdr_xml = target._element
        xml_str = etree.tostring(hdr_xml, encoding='unicode')
        if 'PAGE' in xml_str or 'fldChar' in xml_str:
            for child in list(hdr_xml):
                hdr_xml.remove(child)
            hdr_xml.append(OxmlElement('w:p'))
            print(f"  {rid}: PAGE removido do header")

# === 2. Put PAGE in footers of sections 5-10 ===
print("\n=== Inserindo PAGE nos footers das seções 5-10 (centralizado) ===")

for si, section in enumerate(doc.sections):
    # Unlink footer so each section is independent
    section.footer.is_linked_to_previous = False

    sectPr = section._sectPr

    # Find footer references for this section
    for ref in sectPr.findall(qn('w:footerReference')):
        ftype = ref.get(qn('w:type'))
        if ftype != 'default':
            continue
        rid = ref.get(qn('r:id'))
        try:
            footer_xml = part.rels[rid].target_part._element
            # Clear existing content
            for child in list(footer_xml):
                footer_xml.remove(child)

            if si >= 5:
                # Textual sections: add centered page number
                footer_xml.append(create_page_number_paragraph_center())
                print(f"  Seção {si} footer: PAGE centralizado inserido")
            else:
                # Pre-textual: empty footer
                footer_xml.append(OxmlElement('w:p'))
                print(f"  Seção {si} footer: esvaziado")
        except Exception as e:
            print(f"  Seção {si} footer: erro {e}")

doc.save(DOCX_PATH)

# === Verify ===
print("\n=== Verificação ===")
doc2 = Document(DOCX_PATH)
part2 = doc2.part

for si, section in enumerate(doc2.sections):
    sectPr = section._sectPr
    hdr_page = False
    ftr_page = False

    for ref in sectPr.findall(qn('w:headerReference')):
        rid = ref.get(qn('r:id'))
        try:
            xml = etree.tostring(part2.rels[rid].target_part._element, encoding='unicode')
            if 'PAGE' in xml:
                hdr_page = True
        except:
            pass

    for ref in sectPr.findall(qn('w:footerReference')):
        rid = ref.get(qn('r:id'))
        try:
            xml = etree.tostring(part2.rels[rid].target_part._element, encoding='unicode')
            if 'PAGE' in xml:
                ftr_page = True
        except:
            pass

    expected = "OK" if (not hdr_page and (ftr_page == (si >= 5))) else "PROBLEMA"
    print(f"  S{si}: header_PAGE={hdr_page}, footer_PAGE={ftr_page} [{expected}]")

print(f"\nSalvo: {DOCX_PATH}")
