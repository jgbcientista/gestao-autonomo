import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
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

    # Find a working Sumrio2 entry to use as template
    template_h2 = None
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if '2.4' in text:  # Known good entry
            template_h2 = p
            break

    # Find a working Sumrio1 entry for MOTOR H1
    template_h1 = None
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'FUNDAMENTAÇÃO' in text:
            template_h1 = p
            break

    if template_h2 is None or template_h1 is None:
        print("Templates não encontrados!")
        break

    print("=== Templates encontrados ===")

    # Extract rPr from template for reuse
    def get_rPr_from_template(template):
        """Get the rPr from the first run with text in the hyperlink"""
        for hl in template.findall(qn('w:hyperlink')):
            for r in hl.findall(qn('w:r')):
                rPr = r.find(qn('w:rPr'))
                if rPr is not None:
                    return rPr
        return None

    def create_toc_entry_h2(num, title, page_num, bookmark_id, template):
        """Create a Sumrio2 TOC entry matching the working pattern"""
        # Deep copy the template
        new_p = copy.deepcopy(template)

        # Find the hyperlink
        hl = new_p.find(qn('w:hyperlink'))
        if hl is None:
            return new_p

        # Update anchor
        hl.set(qn('w:anchor'), bookmark_id)

        # Remove all existing runs
        for r in list(hl.findall(qn('w:r'))):
            hl.remove(r)

        # Get rPr template
        rPr_template = get_rPr_from_template(template)

        def make_run_text(text):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            t = OxmlElement('w:t')
            t.set(qn('xml:space'), 'preserve')
            t.text = text
            r.append(t)
            return r

        def make_run_tab():
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            tab = OxmlElement('w:tab')
            r.append(tab)
            return r

        def make_run_fldchar(ftype):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            fc = OxmlElement('w:fldChar')
            fc.set(qn('w:fldCharType'), ftype)
            r.append(fc)
            return r

        def make_run_instr(bookmark):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            instr = OxmlElement('w:instrText')
            instr.set(qn('xml:space'), 'preserve')
            instr.text = f' PAGEREF {bookmark} \\h '
            r.append(instr)
            return r

        # Build runs in correct order:
        # 1. Number text (e.g., "3.1")
        hl.append(make_run_text(num))
        # 2. TAB
        hl.append(make_run_tab())
        # 3. Title text
        hl.append(make_run_text(title))
        # 4. TAB (before page number with dot leader)
        hl.append(make_run_tab())
        # 5. fldChar begin
        hl.append(make_run_fldchar('begin'))
        # 6. instrText PAGEREF
        hl.append(make_run_instr(bookmark_id))
        # 7. empty run (for field update)
        r_empty = OxmlElement('w:r')
        hl.append(r_empty)
        # 8. fldChar separate
        hl.append(make_run_fldchar('separate'))
        # 9. Page number text
        hl.append(make_run_text(str(page_num)))
        # 10. fldChar end
        hl.append(make_run_fldchar('end'))

        return new_p

    def create_toc_entry_h1(title, page_num, bookmark_id, template):
        """Create a Sumrio1 TOC entry"""
        new_p = copy.deepcopy(template)

        hl = new_p.find(qn('w:hyperlink'))
        if hl is None:
            return new_p

        hl.set(qn('w:anchor'), bookmark_id)

        for r in list(hl.findall(qn('w:r'))):
            hl.remove(r)

        rPr_template = get_rPr_from_template(template)

        def make_run_text(text):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            t = OxmlElement('w:t')
            t.set(qn('xml:space'), 'preserve')
            t.text = text
            r.append(t)
            return r

        def make_run_tab():
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            tab = OxmlElement('w:tab')
            r.append(tab)
            return r

        def make_run_fldchar(ftype):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            fc = OxmlElement('w:fldChar')
            fc.set(qn('w:fldCharType'), ftype)
            r.append(fc)
            return r

        def make_run_instr(bookmark):
            r = OxmlElement('w:r')
            if rPr_template is not None:
                r.append(copy.deepcopy(rPr_template))
            instr = OxmlElement('w:instrText')
            instr.set(qn('xml:space'), 'preserve')
            instr.text = f' PAGEREF {bookmark} \\h '
            r.append(instr)
            return r

        # H1 pattern: title, TAB, PAGEREF
        hl.append(make_run_text(title))
        hl.append(make_run_tab())
        hl.append(make_run_fldchar('begin'))
        hl.append(make_run_instr(bookmark_id))
        r_empty = OxmlElement('w:r')
        hl.append(r_empty)
        hl.append(make_run_fldchar('separate'))
        hl.append(make_run_text(str(page_num)))
        hl.append(make_run_fldchar('end'))

        return new_p

    # Define MOTOR chapter entries
    motor_entries = [
        ('h1', 'MOTOR DE INTELIGÊNCIA ARTIFICIAL: ARQUITETURA, TREINAMENTO E DECISÃO', '_Toc223603190'),
        ('h2', '3.1', 'Arquitetura do Ensemble de Modelos', '_Toc223603191'),
        ('h2', '3.2', 'Critérios Comportamentais (14 Dimensões)', '_Toc223603192'),
        ('h2', '3.3', 'Processo de Treinamento com Dados Reais', '_Toc223603193'),
        ('h2', '3.4', 'Limiares de Decisão Autônoma', '_Toc223603194'),
        ('h2', '3.5', 'Pipeline de Inferência em Tempo Real', '_Toc223603195'),
        ('h2', '3.6', 'Integração com Blockchain para Auditoria', '_Toc223603196'),
    ]

    # Find and remove the old broken MOTOR entries
    print("\n=== Removendo entradas antigas do MOTOR ===")
    to_remove = []
    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'MOTOR DE INTELIG' in text or (text.strip().startswith('3.') and any(
            sub in text for sub in ['Arquitetura', 'Critérios', 'Processo', 'Limiares', 'Pipeline', 'Integração com Blockchain para Auditoria'])):
            to_remove.append((idx, p, text[:60]))

    for idx, p, text in to_remove:
        sdtContent.remove(p)
        print(f"  Removida: {text}")

    # Re-get paras after removal
    paras = list(sdtContent.findall(qn('w:p')))

    # Find insertion point (before DESENVOLVIMENTO)
    dev_idx = None
    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'DESENVOLVIMENTO' in text:
            dev_idx = idx
            break

    print(f"\n=== Inserindo novas entradas antes de DESENVOLVIMENTO (idx={dev_idx}) ===")

    if dev_idx is not None:
        dev_elem = paras[dev_idx]

        # Create and insert new entries (in reverse since addprevious inserts before)
        new_elements = []
        for entry in motor_entries:
            if entry[0] == 'h1':
                new_p = create_toc_entry_h1(entry[1], '', entry[2], template_h1)
                new_elements.append(new_p)
            else:
                new_p = create_toc_entry_h2(entry[1], entry[2], '', entry[3], template_h2)
                new_elements.append(new_p)

        # Insert in order before DESENVOLVIMENTO
        for elem in new_elements:
            dev_elem.addprevious(elem)
            texts = ''.join(t.text for t in elem.iter(qn('w:t')) if t.text)
            print(f"  Inserida: {texts[:60]}")

    break

doc.save(DOCX_PATH)

# Verify
print("\n=== Verificação TOC ===")
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
