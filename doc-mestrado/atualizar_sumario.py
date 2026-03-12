import sys, io, copy
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from lxml import etree

doc = Document('doc-qualificacao-ifba-12-03-2026.docx')
body = doc.element.body

# === Configuration ===
# New chapter headings (H1 and H2s) with their TOC text
# The chapter number will be 5 (after RESULTADOS PARCIAIS which is 4)
CHAPTER_NUM = 5
new_toc_entries = [
    ("Sumrio1", "MOTOR DE INTELIGÊNCIA ARTIFICIAL: ARQUITETURA, TREINAMENTO E DECISÃO", None),
    ("Sumrio2", "Fluxo de Decisão Automatizada", f"{CHAPTER_NUM}.1"),
    ("Sumrio2", "Vetor de Características (Features) para Análise Comportamental", f"{CHAPTER_NUM}.2"),
    ("Sumrio2", "Arquitetura do Ensemble de Modelos de IA", f"{CHAPTER_NUM}.3"),
    ("Sumrio2", "Processo de Treinamento com Dados Reais", f"{CHAPTER_NUM}.4"),
    ("Sumrio2", "Limiares de Decisão e Política de Segurança", f"{CHAPTER_NUM}.5"),
    ("Sumrio2", "Integração com Blockchain para Auditoria", f"{CHAPTER_NUM}.6"),
]

# Bookmark IDs - start after max existing
BOOKMARK_START_ID = 62
TOC_START_NUM = 223603191

# === STEP 1: Add bookmarks to chapter headings ===
print("=== Adicionando bookmarks aos headings ===")

# Map heading text to paragraph index
heading_map = {}
for i, p in enumerate(doc.paragraphs):
    if p.style and 'Heading' in p.style.name and p.text.strip():
        heading_map[p.text.strip()] = i

bookmark_id = BOOKMARK_START_ID
toc_num = TOC_START_NUM
bookmark_names = {}  # text -> bookmark name

for style, text, num in new_toc_entries:
    # Find the heading paragraph
    para_idx = heading_map.get(text)
    if para_idx is None:
        print(f"  AVISO: Heading não encontrado: {text[:50]}")
        continue

    para_elem = doc.paragraphs[para_idx]._element

    # Check if already has bookmark
    existing = para_elem.findall(qn('w:bookmarkStart'))
    if existing:
        bk_name = existing[0].get(qn('w:name'))
        bookmark_names[text] = bk_name
        print(f"  Bookmark já existe: {text[:40]} -> {bk_name}")
        continue

    # Create bookmark
    bk_name = f"_Toc{toc_num}"
    bookmark_names[text] = bk_name

    # Create bookmarkStart element
    bk_start = OxmlElement('w:bookmarkStart')
    bk_start.set(qn('w:id'), str(bookmark_id))
    bk_start.set(qn('w:name'), bk_name)

    # Create bookmarkEnd element
    bk_end = OxmlElement('w:bookmarkEnd')
    bk_end.set(qn('w:id'), str(bookmark_id))

    # Insert at beginning and end of paragraph
    para_elem.insert(0, bk_start)
    para_elem.append(bk_end)

    print(f"  Bookmark adicionado: {text[:40]} -> {bk_name} (id={bookmark_id})")

    bookmark_id += 1
    toc_num += 1

# === STEP 2: Find TOC SDT and insert entries ===
print("\n=== Inserindo entradas no sumário ===")

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
    paras = sdtContent.findall(qn('w:p'))

    # Find the CRONOGRAMA entry (index 27) to insert before it
    cronograma_idx = None
    for idx, p in enumerate(paras):
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if 'CRONOGRAMA' in text:
            cronograma_idx = idx
            break

    if cronograma_idx is None:
        print("  ERRO: Não encontrou CRONOGRAMA no sumário!")
        break

    print(f"  Inserindo antes do índice {cronograma_idx} (CRONOGRAMA)")

    # Get reference entry to copy run formatting
    ref_sumrio1 = paras[21]  # RESULTADOS PARCIAIS
    ref_sumrio2 = paras[22]  # 4.1 Configuração...

    # Get the rPr (run properties) from reference entries
    def get_run_props(ref_para):
        """Extract rPr from the first hyperlink run in reference paragraph"""
        hyperlink = ref_para.find(qn('w:hyperlink'))
        if hyperlink is not None:
            runs = hyperlink.findall(qn('w:r'))
            if runs:
                rPr = runs[0].find(qn('w:rPr'))
                if rPr is not None:
                    return copy.deepcopy(rPr)
        return None

    rPr_h1 = get_run_props(ref_sumrio1)
    rPr_h2 = get_run_props(ref_sumrio2)

    # Get tab properties from reference
    def get_tabs(ref_para):
        pPr = ref_para.find(qn('w:pPr'))
        if pPr is not None:
            tabs = pPr.find(qn('w:tabs'))
            if tabs is not None:
                return copy.deepcopy(tabs)
        return None

    tabs_h1 = get_tabs(ref_sumrio1)
    tabs_h2 = get_tabs(ref_sumrio2)

    # Create TOC entries
    cronograma_elem = paras[cronograma_idx]

    for entry_style, entry_text, entry_num in new_toc_entries:
        bk_name = bookmark_names.get(entry_text)
        if bk_name is None:
            print(f"  Pulando entrada sem bookmark: {entry_text[:40]}")
            continue

        # Create new paragraph
        new_p = OxmlElement('w:p')

        # Create pPr with style
        pPr = OxmlElement('w:pPr')
        pStyle = OxmlElement('w:pStyle')
        pStyle.set(qn('w:val'), entry_style)
        pPr.append(pStyle)

        # Add tabs
        if entry_style == 'Sumrio1' and tabs_h1 is not None:
            pPr.append(copy.deepcopy(tabs_h1))
        elif entry_style == 'Sumrio2' and tabs_h2 is not None:
            pPr.append(copy.deepcopy(tabs_h2))

        # Add rPr to pPr (paragraph-level run properties)
        pPr_rPr = OxmlElement('w:rPr')
        rFonts = OxmlElement('w:rFonts')
        rFonts.set(qn('w:ascii'), 'Arial')
        rFonts.set(qn('w:hAnsi'), 'Arial')
        pPr_rPr.append(rFonts)
        noProof = OxmlElement('w:noProof')
        pPr_rPr.append(noProof)
        pPr.append(pPr_rPr)

        new_p.append(pPr)

        # Create hyperlink
        hyperlink = OxmlElement('w:hyperlink')
        hyperlink.set(qn('w:anchor'), bk_name)
        hyperlink.set(qn('w:history'), '1')

        # Determine rPr to use
        ref_rPr = rPr_h1 if entry_style == 'Sumrio1' else rPr_h2

        if entry_style == 'Sumrio1':
            # H1 entry: just text + tab + page number
            # Text runs (split words for consistency with existing format)
            words = entry_text.split()
            for wi, word in enumerate(words):
                run = OxmlElement('w:r')
                if ref_rPr is not None:
                    run.append(copy.deepcopy(ref_rPr))
                t = OxmlElement('w:t')
                t.text = word
                t.set(qn('xml:space'), 'preserve')
                run.append(t)
                hyperlink.append(run)

                # Add space between words (except last)
                if wi < len(words) - 1:
                    space_run = OxmlElement('w:r')
                    if ref_rPr is not None:
                        space_run.append(copy.deepcopy(ref_rPr))
                    space_t = OxmlElement('w:t')
                    space_t.text = ' '
                    space_t.set(qn('xml:space'), 'preserve')
                    space_run.append(space_t)
                    hyperlink.append(space_run)
        else:
            # H2 entry: number + tab + text + tab + page
            # Number run
            if entry_num:
                num_run = OxmlElement('w:r')
                if ref_rPr is not None:
                    num_run.append(copy.deepcopy(ref_rPr))
                num_t = OxmlElement('w:t')
                num_t.text = entry_num
                num_t.set(qn('xml:space'), 'preserve')
                num_run.append(num_t)
                hyperlink.append(num_run)

            # Tab run
            tab_run = OxmlElement('w:r')
            if ref_rPr is not None:
                tab_run.append(copy.deepcopy(ref_rPr))
            tab_elem = OxmlElement('w:tab')
            tab_run.append(tab_elem)
            hyperlink.append(tab_run)

            # Text run
            text_run = OxmlElement('w:r')
            if ref_rPr is not None:
                text_run.append(copy.deepcopy(ref_rPr))
            text_t = OxmlElement('w:t')
            text_t.text = entry_text
            text_t.set(qn('xml:space'), 'preserve')
            text_run.append(text_t)
            hyperlink.append(text_run)

        # Tab before page number
        tab_run2 = OxmlElement('w:r')
        if ref_rPr is not None:
            tab_run2.append(copy.deepcopy(ref_rPr))
        tab_elem2 = OxmlElement('w:tab')
        tab_run2.append(tab_elem2)
        hyperlink.append(tab_run2)

        # PAGEREF field: begin
        fld_begin_run = OxmlElement('w:r')
        if ref_rPr is not None:
            fld_begin_run.append(copy.deepcopy(ref_rPr))
        fld_begin = OxmlElement('w:fldChar')
        fld_begin.set(qn('w:fldCharType'), 'begin')
        fld_begin_run.append(fld_begin)
        hyperlink.append(fld_begin_run)

        # PAGEREF field: instrText
        instr_run = OxmlElement('w:r')
        if ref_rPr is not None:
            instr_run.append(copy.deepcopy(ref_rPr))
        instr_text = OxmlElement('w:instrText')
        instr_text.set(qn('xml:space'), 'preserve')
        instr_text.text = f' PAGEREF {bk_name} \\h '
        instr_run.append(instr_text)
        hyperlink.append(instr_run)

        # PAGEREF field: separate
        fld_sep_run = OxmlElement('w:r')
        if ref_rPr is not None:
            fld_sep_run.append(copy.deepcopy(ref_rPr))
        fld_sep = OxmlElement('w:fldChar')
        fld_sep.set(qn('w:fldCharType'), 'separate')
        fld_sep_run.append(fld_sep)
        hyperlink.append(fld_sep_run)

        # Page number (placeholder - Word will update)
        page_run = OxmlElement('w:r')
        if ref_rPr is not None:
            page_run.append(copy.deepcopy(ref_rPr))
        page_t = OxmlElement('w:t')
        page_t.text = '??'
        page_run.append(page_t)
        hyperlink.append(page_run)

        # PAGEREF field: end
        fld_end_run = OxmlElement('w:r')
        if ref_rPr is not None:
            fld_end_run.append(copy.deepcopy(ref_rPr))
        fld_end = OxmlElement('w:fldChar')
        fld_end.set(qn('w:fldCharType'), 'end')
        fld_end_run.append(fld_end)
        hyperlink.append(fld_end_run)

        new_p.append(hyperlink)

        # Insert before CRONOGRAMA entry
        cronograma_elem.addprevious(new_p)

        print(f"  Inserido: [{entry_style}] {entry_text[:50]}")

    # === STEP 3: Update CRONOGRAMA and REFERÊNCIAS chapter numbers ===
    # CRONOGRAMA was 5, now becomes 6
    # REFERÊNCIAS was after CRONOGRAMA
    print("\n=== Atualizando numeração dos capítulos seguintes ===")

    # Update TOC entries for CRONOGRAMA (change 5.1 to 6.1)
    paras = sdtContent.findall(qn('w:p'))  # Re-read after insertion
    for p in paras:
        text = ''.join(t.text for t in p.iter(qn('w:t')) if t.text)
        if '5.1' in text and 'Cronograma' in text:
            # Find the run with "5.1" and change to "6.1"
            for t_elem in p.iter(qn('w:t')):
                if t_elem.text and '5.1' in t_elem.text:
                    t_elem.text = t_elem.text.replace('5.1', '6.1')
                    print(f"  TOC: 5.1 -> 6.1 (Cronograma)")

    break

# === STEP 4: Add "updateFields" setting to force TOC update on open ===
print("\n=== Configurando atualização automática de campos ===")

# Add w:updateFields to document settings
settings = doc.settings.element
update_fields = settings.find(qn('w:updateFields'))
if update_fields is None:
    update_fields = OxmlElement('w:updateFields')
    update_fields.set(qn('w:val'), 'true')
    settings.append(update_fields)
    print("  updateFields=true adicionado (Word atualizará o sumário ao abrir)")
else:
    update_fields.set(qn('w:val'), 'true')
    print("  updateFields já existe, atualizado para true")

# === STEP 5: Save ===
doc.save('doc-qualificacao-ifba-12-03-2026.docx')
print("\nDocumento salvo com sucesso!")
print("NOTA: Ao abrir o documento no Word, ele perguntará se deseja atualizar")
print("os campos - clique 'Sim' para atualizar os números de página do sumário.")
