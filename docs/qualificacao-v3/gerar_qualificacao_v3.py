# -*- coding: utf-8 -*-
"""Gera a qualificacao v3 a partir do documento comentado pelo orientador.

Mantem capa, folha de rosto, cabecalhos/rodapes, estilos e numeracao de paginas do original;
reescreve resumo/abstract, listas, sumario e todo o corpo a partir de conteudo.py (formatacao ABNT).
Os comentarios do orientador sao removidos (a resposta ponto a ponto fica em RESPOSTA-COMENTARIOS.md).

    python -I gerar_qualificacao_v3.py
"""
import os, re, struct, sys, zipfile
from xml.sax.saxutils import escape

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))  # rodar com python -I
import conteudo as K

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "doc-qualificacao-ifba-REVISADO-para-ajustar-v1.docx")
DST = os.path.join(HERE, "..", "doc-qualificacao-ifba-v3.docx")
FIG = os.path.join(HERE, "figuras")

TEXT_W = 9071          # largura util em twips (21 cm - 3 cm - 2 cm)
CM = 567               # twips por cm


# ----------------------------------------------------------------------------- utilidades XML
def split_body(x):
    body = x[x.index("<w:body>") + 8:x.index("</w:body>")]
    tok = re.compile(r"<(/?)w:(p|tbl|sdt|bookmarkStart|bookmarkEnd|sectPr)\b[^>]*?(/?)>")
    items, depth, start, cur = [], 0, None, None
    for m in tok.finditer(body):
        close, name, selfc = m.group(1), m.group(2), m.group(3)
        if depth == 0 and not close:
            if selfc:
                items.append(body[m.start():m.end()]); continue
            start, cur, depth = m.start(), name, 1; continue
        if depth > 0 and name == cur:
            if close: depth -= 1
            elif not selfc: depth += 1
            if depth == 0: items.append(body[start:m.end()])
    assert "".join(items) == body, "falha ao dividir o corpo"
    return items


def sem_comentarios(xml):
    xml = re.sub(r"<w:sdt><w:sdtPr><w:id w:val=\"\d+\"/><w:tag w:val=\"goog_rdk_\d+\"/></w:sdtPr>"
                 r"<w:sdtContent>(.*?)</w:sdtContent></w:sdt>", r"\1", xml, flags=re.S)
    xml = re.sub(r"<w:commentRange(Start|End) w:id=\"\d+\"/>", "", xml)
    xml = re.sub(r"<w:r\b[^>]*>(<w:rPr>.*?</w:rPr>)?<w:commentReference w:id=\"\d+\"/></w:r>", "", xml, flags=re.S)
    return xml


def runs(texto, base=""):
    """Converte a marcacao *italico* **negrito** _{sub} em runs."""
    out, b, i, buf = [], False, False, ""
    pos = 0

    def flush(t, sub=False):
        if not t: return
        rpr = base + ("<w:b/><w:bCs/>" if b else "") + ("<w:i/><w:iCs/>" if i else "") + \
              ('<w:vertAlign w:val="subscript"/>' if sub else "")
        rp = "<w:rPr>%s</w:rPr>" % rpr if rpr else ""
        out.append('<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % (rp, escape(t)))

    while pos < len(texto):
        if texto.startswith("**", pos):
            flush(buf); buf = ""; b = not b; pos += 2
        elif texto[pos] == "*":
            flush(buf); buf = ""; i = not i; pos += 1
        elif texto.startswith("_{", pos):
            flush(buf); buf = ""
            fim = texto.index("}", pos)
            flush(texto[pos + 2:fim], sub=True); pos = fim + 1
        else:
            buf += texto[pos]; pos += 1
    flush(buf)
    assert not b and not i, "marcacao desbalanceada: " + texto[:80]
    return "".join(out)


def par(conteudo_runs, ppr):
    return "<w:p><w:pPr>%s</w:pPr>%s</w:p>" % (ppr, conteudo_runs)


PPR_CORPO = '<w:spacing w:before="0" w:after="0" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="709"/><w:jc w:val="both"/>'
PPR_VAZIO = '<w:spacing w:before="0" w:after="0" w:line="360" w:lineRule="auto"/>'
PPR_ALINEA = '<w:spacing w:before="0" w:after="0" w:line="360" w:lineRule="auto"/><w:ind w:left="1134" w:hanging="425"/><w:jc w:val="both"/>'
RPR_10 = '<w:sz w:val="20"/><w:szCs w:val="20"/>'


def p(texto): return par(runs(texto), PPR_CORPO)
def vazio(): return par("", PPR_VAZIO)


def titulo_pre(t, page_break=True):
    """Titulo de elemento pre ou pos-textual (sem indicativo numerico, centralizado)."""
    ppr = ('<w:pStyle w:val="TituloPre"/>' + ('<w:pageBreakBefore/>' if page_break else ""))
    return par(runs(t), ppr)


# ----------------------------------------------------------------------------- imagens
class Midia:
    def __init__(self):
        self.rels, self.arquivos, self.n = [], [], 0

    def add(self, caminho):
        self.n += 1
        rid, nome = "rIdV3img%d" % self.n, "v3_fig%d.png" % self.n
        self.rels.append('<Relationship Id="%s" Type="http://schemas.openxmlformats.org/officeDocument/2006/'
                         'relationships/image" Target="media/%s"/>' % (rid, nome))
        self.arquivos.append(("word/media/" + nome, open(caminho, "rb").read()))
        return rid


def png_dim(caminho):
    with open(caminho, "rb") as f:
        f.read(16)
        return struct.unpack(">II", f.read(8))


DOCPR = [5000]


def imagem(midia, arquivo, largura_cm):
    caminho = os.path.join(FIG, arquivo)
    w, h = png_dim(caminho)
    cx = int(largura_cm * 360000); cy = int(cx * h / w)
    rid = midia.add(caminho)
    DOCPR[0] += 1
    d = ('<w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="%d" cy="%d"/>'
         '<wp:docPr id="%d" name="%s"/><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">'
         '<pic:pic><pic:nvPicPr><pic:cNvPr id="0" name="%s"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="%s"/>'
         '<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="%d" cy="%d"/></a:xfrm>'
         '<a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>'
         % (cx, cy, DOCPR[0], arquivo, arquivo, rid, cx, cy))
    return par(d, '<w:keepNext/><w:spacing w:before="0" w:after="0"/><w:jc w:val="center"/>')


# ----------------------------------------------------------------------------- tabelas
def tabela(cab, linhas, larguras_cm):
    ws = [int(round(c * CM)) for c in larguras_cm]
    escala = TEXT_W / sum(ws)
    ws = [int(w * escala) for w in ws]
    ws[-1] += TEXT_W - sum(ws)
    borda = '<w:%s w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
    tblpr = ('<w:tblPr><w:tblW w:w="%d" w:type="dxa"/><w:jc w:val="center"/><w:tblBorders>%s%s</w:tblBorders>'
             '<w:tblLayout w:type="fixed"/><w:tblCellMar><w:left w:w="85" w:type="dxa"/><w:right w:w="85" w:type="dxa"/>'
             '</w:tblCellMar><w:tblLook w:val="04A0"/></w:tblPr>' % (TEXT_W, borda % "top", borda % "bottom"))
    grid = "<w:tblGrid>%s</w:tblGrid>" % "".join('<w:gridCol w:w="%d"/>' % w for w in ws)

    def cel(t, w, cabecalho):
        tcb = '<w:tcBorders>%s</w:tcBorders>' % (borda % "bottom") if cabecalho else ""
        # keepNext so no cabecalho: manter em todas as linhas impediria a tabela de quebrar entre paginas
        ppr = '%s<w:spacing w:before="20" w:after="20" w:line="240" w:lineRule="auto"/><w:jc w:val="%s"/>' % (
            "<w:keepNext/>" if cabecalho else "", "center" if cabecalho else "left")
        txt = "**%s**" % t if cabecalho and not t.startswith("**") else t
        return ('<w:tc><w:tcPr><w:tcW w:w="%d" w:type="dxa"/>%s<w:vAlign w:val="center"/></w:tcPr>%s</w:tc>'
                % (w, tcb, par(runs(txt, RPR_10), ppr)))

    trs = ['<w:tr><w:trPr><w:tblHeader/><w:cantSplit/></w:trPr>%s</w:tr>' % "".join(cel(t, w, True) for t, w in zip(cab, ws))]
    for ln in linhas:
        assert len(ln) == len(cab), ln
        trs.append('<w:tr><w:trPr><w:cantSplit/></w:trPr>%s</w:tr>' % "".join(cel(t, w, False) for t, w in zip(ln, ws)))
    return "<w:tbl>%s%s%s</w:tbl>" % (tblpr, grid, "".join(trs))


def legenda(estilo, rotulo, n, titulo):
    return par(runs("%s %d – %s" % (rotulo, n, titulo), ""), '<w:pStyle w:val="%s"/>' % estilo)


def fonte(t):
    return par(runs("Fonte: " + t, RPR_10), '<w:pStyle w:val="FonteIlustracao"/>')


# ----------------------------------------------------------------------------- corpo
def montar_corpo(midia):
    xml, figuras, tabelas, sumario = [], [], [], []
    ncap = nsec = nsub = nfig = ntab = 0
    for bl in K.C:
        tipo = bl[0]
        if tipo == "cap":
            ncap += 1; nsec = nsub = 0
            t = "%d %s" % (ncap, bl[1])
            # o cap. 1 ja abre secao nova (quebra de pagina); pageBreakBefore ali geraria pagina em branco
            quebra = "<w:pageBreakBefore/>" if ncap > 1 else ""
            xml.append(par(runs(t), '<w:pStyle w:val="Heading1"/><w:keepNext/>' + quebra +
                           '<w:spacing w:before="0" w:after="360" w:line="360" w:lineRule="auto"/><w:ind w:left="0" w:firstLine="0"/><w:jc w:val="left"/>'))
            sumario.append((1, t))
        elif tipo == "sec":
            nsec += 1; nsub = 0
            t = "%d.%d %s" % (ncap, nsec, bl[1])
            xml.append(par(runs(t), '<w:pStyle w:val="Heading2"/><w:keepNext/>'
                           '<w:spacing w:before="360" w:after="240" w:line="360" w:lineRule="auto"/><w:ind w:left="0" w:firstLine="0"/><w:jc w:val="left"/>'))
            sumario.append((2, t))
        elif tipo == "sub":
            nsub += 1
            t = "%d.%d.%d %s" % (ncap, nsec, nsub, bl[1])
            xml.append(par(runs(t, '<w:b w:val="0"/><w:bCs w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/>'),
                           '<w:pStyle w:val="Heading3"/><w:keepNext/>'
                           '<w:spacing w:before="240" w:after="120" w:line="360" w:lineRule="auto"/><w:ind w:left="0" w:firstLine="0"/><w:jc w:val="left"/>'))
            sumario.append((3, t))
        elif tipo == "p":
            xml.append(p(bl[1]))
        elif tipo == "al":
            for k, item in enumerate(bl[1]):
                xml.append(par(runs("%s) " % "abcdefghij"[k]) + runs(item), PPR_ALINEA))
        elif tipo == "cod":
            xml.append(par(runs(bl[1], '<w:rFonts w:ascii="Courier New" w:hAnsi="Courier New" w:cs="Courier New"/>' + RPR_10),
                           '<w:spacing w:before="120" w:after="120" w:line="240" w:lineRule="auto"/><w:ind w:left="709" w:right="567"/><w:jc w:val="left"/>'))
        elif tipo == "eq":
            xml.append(par('<w:r><w:tab/></w:r>' + runs(bl[1]) + '<w:r><w:tab/><w:t>(%s)</w:t></w:r>' % bl[2],
                           '<w:tabs><w:tab w:val="center" w:pos="4535"/><w:tab w:val="right" w:pos="9071"/></w:tabs>'
                           '<w:spacing w:before="120" w:after="120" w:line="360" w:lineRule="auto"/>'))
        elif tipo == "fig":
            _, arq, tit, fnt, larg = bl
            nfig += 1; figuras.append("Figura %d – %s" % (nfig, tit))
            xml += [legenda("LegendaFigura", "Figura", nfig, tit), imagem(midia, arq, larg), fonte(fnt)]
        elif tipo == "tab":
            _, tit, cab, linhas, larg, fnt = bl
            ntab += 1; tabelas.append("Tabela %d – %s" % (ntab, tit))
            xml += [legenda("LegendaTabela", "Tabela", ntab, tit), tabela(cab, linhas, larg), fonte(fnt)]
        else:
            raise ValueError(tipo)
    # referencias (elemento pos-textual, sem numero)
    xml.append(par(runs("REFERÊNCIAS"), '<w:pStyle w:val="Heading1"/><w:pageBreakBefore/>'
                   '<w:spacing w:before="0" w:after="360" w:line="360" w:lineRule="auto"/><w:ind w:left="0" w:firstLine="0"/><w:jc w:val="center"/>'))
    sumario.append((1, "REFERÊNCIAS"))
    for r in K.REFERENCIAS:
        xml.append(par(runs(r), '<w:spacing w:before="0" w:after="240" w:line="240" w:lineRule="auto"/><w:ind w:left="0" w:firstLine="0"/><w:jc w:val="left"/>'))
    return xml, figuras, tabelas, sumario


def campo_lista(instr, entradas, estilo_entrada):
    """Lista gerada por campo TOC; as entradas pre-preenchidas sao trocadas pelo Word ao atualizar (com paginas)."""
    if not entradas:
        entradas = [(estilo_entrada, "")]
    out = []
    for k, (est, t) in enumerate(entradas):
        ini = ('<w:r><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText xml:space="preserve"> %s </w:instrText></w:r>'
               '<w:r><w:fldChar w:fldCharType="separate"/></w:r>' % escape(instr)) if k == 0 else ""
        fim = '<w:r><w:fldChar w:fldCharType="end"/></w:r>' if k == len(entradas) - 1 else ""
        out.append(par(ini + runs(t) + fim,
                       '<w:pStyle w:val="%s"/><w:tabs><w:tab w:val="right" w:leader="dot" w:pos="9061"/></w:tabs>' % est))
    return out


# ----------------------------------------------------------------------------- pre-textuais
def pre_textuais(items, figuras, tabelas, sumario):
    pre = [sem_comentarios(it) for it in items[:88]]
    # titulos da capa/folha de rosto/resumo nao devem entrar no sumario
    pre = [it.replace('<w:pStyle w:val="Heading1"/>', '<w:pStyle w:val="TituloCapa"/>') for it in pre]
    # data da capa: local e ano (ABNT)
    pre[39] = pre[39].replace("Salvador - 20/06/2026", "Salvador</w:t><w:br/><w:t>2026")
    assert "<w:br/><w:t>2026" in pre[39]
    # RESUMO / ABSTRACT: titulo como elemento pre-textual
    for k, t in ((62, "RESUMO"), (75, "ABSTRACT")):
        assert t in pre[k]
        sect = re.search(r"<w:sectPr>.*?</w:sectPr>", pre[k], re.S)
        pre[k] = titulo_pre(t, page_break=False) if not sect else pre[k]
    ppr_resumo = '<w:spacing w:before="0" w:after="0" w:line="360" w:lineRule="auto"/><w:jc w:val="both"/>'
    pre[63:74] = [vazio(), par(runs(K.RESUMO), ppr_resumo), vazio()]
    i_pal = next(k for k, it in enumerate(pre) if "Palavras-chave" in it)
    pre[i_pal] = re.sub(r"(</w:pPr>).*(</w:p>)$", lambda m: m.group(1) + runs("**Palavras-chave:** " + K.PALAVRAS_CHAVE) + m.group(2),
                        pre[i_pal], flags=re.S)
    i_abs = next(k for k, it in enumerate(pre) if "ABSTRACT" in it)
    i_kw = next(k for k, it in enumerate(pre) if "Keywords" in it)
    pre[i_abs + 1:i_kw] = [vazio(), par(runs(K.ABSTRACT), ppr_resumo), vazio()]
    i_kw = next(k for k, it in enumerate(pre) if "Keywords" in it)
    pre[i_kw] = re.sub(r"(</w:pPr>).*(</w:p>)$", lambda m: m.group(1) + runs("**Keywords:** " + K.KEYWORDS) + m.group(2),
                       pre[i_kw], flags=re.S)

    out = list(pre)
    out.append(titulo_pre("LISTA DE FIGURAS", page_break=False))  # a secao do abstract ja termina em quebra
    out += campo_lista('TOC \\h \\z \\t "LegendaFigura,1"', [("Sumrio1", f) for f in figuras], "Sumrio1")
    out.append(titulo_pre("LISTA DE TABELAS"))
    out += campo_lista('TOC \\h \\z \\t "LegendaTabela,1"', [("Sumrio1", t) for t in tabelas], "Sumrio1")
    out.append(titulo_pre("LISTA DE SIGLAS"))
    for s, d in K.SIGLAS:
        out.append(par(runs(s) + '<w:r><w:tab/></w:r>' + runs(d),
                       '<w:tabs><w:tab w:val="left" w:pos="1418"/></w:tabs><w:spacing w:before="0" w:after="60" w:line="276" w:lineRule="auto"/>'
                       '<w:ind w:left="1418" w:hanging="1418"/>'))
    out.append(titulo_pre("SUMÁRIO"))
    est = {1: "Sumrio1", 2: "Sumrio2", 3: "Sumrio3"}
    out += campo_lista('TOC \\o "1-3" \\h \\z \\u', [(est[n], t) for n, t in sumario], "Sumrio1")
    # quebra de secao: pre-textuais sem numero de pagina visivel
    out.append('<w:p><w:pPr><w:sectPr><w:headerReference w:type="default" r:id="rId20"/><w:footerReference w:type="default" r:id="rId21"/>'
               '<w:type w:val="nextPage"/><w:pgSz w:w="11900" w:h="16840" w:orient="portrait"/>'
               '<w:pgMar w:top="1701" w:right="1134" w:bottom="1134" w:left="1701" w:header="1134" w:footer="720" w:gutter="0"/></w:sectPr></w:pPr></w:p>')
    return out


NOVOS_ESTILOS = (
    '<w:style w:type="paragraph" w:customStyle="1" w:styleId="TituloPre"><w:name w:val="Titulo pre-textual"/><w:basedOn w:val="Normal"/>'
    '<w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="0" w:after="360" w:line="360" w:lineRule="auto"/><w:jc w:val="center"/></w:pPr>'
    '<w:rPr><w:b/><w:bCs/><w:caps/></w:rPr></w:style>'
    '<w:style w:type="paragraph" w:customStyle="1" w:styleId="TituloCapa"><w:name w:val="Titulo capa"/><w:basedOn w:val="Normal"/>'
    '<w:pPr><w:keepNext/></w:pPr><w:rPr><w:b/><w:bCs/></w:rPr></w:style>'
    '<w:style w:type="paragraph" w:customStyle="1" w:styleId="LegendaFigura"><w:name w:val="Legenda Figura"/><w:basedOn w:val="Normal"/>'
    '<w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="240" w:after="60" w:line="240" w:lineRule="auto"/><w:jc w:val="center"/></w:pPr>'
    '<w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>'
    '<w:style w:type="paragraph" w:customStyle="1" w:styleId="LegendaTabela"><w:name w:val="Legenda Tabela"/><w:basedOn w:val="Normal"/>'
    '<w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="240" w:after="60" w:line="240" w:lineRule="auto"/><w:jc w:val="center"/></w:pPr>'
    '<w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>'
    '<w:style w:type="paragraph" w:customStyle="1" w:styleId="FonteIlustracao"><w:name w:val="Fonte ilustracao"/><w:basedOn w:val="Normal"/>'
    '<w:next w:val="Normal"/><w:pPr><w:spacing w:before="60" w:after="240" w:line="240" w:lineRule="auto"/><w:jc w:val="center"/></w:pPr>'
    '<w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>'
)


def main():
    zin = zipfile.ZipFile(SRC)
    xml = zin.read("word/document.xml").decode("utf-8")
    items = split_body(xml)
    assert "Keywords" in "".join(items[87:88]) and "CAPÍTULO" in "".join(re.findall(r"<w:t[^>]*>([^<]*)", items[184]))
    sect_final = items[-1]
    assert sect_final.startswith("<w:sectPr")

    midia = Midia()
    corpo, figuras, tabelas, sumario = montar_corpo(midia)
    novo = pre_textuais(items, figuras, tabelas, sumario) + corpo + [sect_final]
    cab = xml[:xml.index("<w:body>") + 8]
    doc = cab + "".join(novo) + "</w:body></w:document>"
    assert "commentRangeStart" not in doc and "commentReference" not in doc

    rels = zin.read("word/_rels/document.xml.rels").decode("utf-8")
    rels = re.sub(r'<Relationship Id="rId(2|8)" [^>]*/>', "", rels)                      # comentarios
    rels = re.sub(r'<Relationship Id="rId(29|30|31|32|35)" [^>]*/>', "", rels)           # figuras antigas
    rels = rels.replace("</Relationships>", "".join(midia.rels) + "</Relationships>")
    ct = zin.read("[Content_Types].xml").decode("utf-8")
    ct = re.sub(r'<Override PartName="/word/comments(Extended)?\.xml"[^>]*/>', "", ct)
    styles = zin.read("word/styles.xml").decode("utf-8")
    for n, lvl in (("Heading1", 0), ("Heading2", 1), ("Heading3", 2)):
        styles = re.sub(r'(<w:style w:type="paragraph" w:styleId="%s">.*?)(</w:pPr>)' % n,
                        r'\1<w:outlineLvl w:val="%d"/>\2' % lvl, styles, count=1, flags=re.S)
    styles = styles.replace("</w:styles>", NOVOS_ESTILOS + "</w:styles>")
    settings = zin.read("word/settings.xml").decode("utf-8")

    pular = {"word/document.xml", "word/_rels/document.xml.rels", "[Content_Types].xml", "word/styles.xml",
             "word/settings.xml", "word/comments.xml", "word/commentsExtended.xml",
             "word/media/image3.png", "word/media/image1.png", "word/media/image7.png",
             "word/media/image4.png", "word/media/image6.png"}
    if os.path.exists(DST): os.remove(DST)
    with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", ct)
        for info in zin.infolist():
            if info.filename in pular or info.filename.endswith("/"): continue
            z.writestr(info.filename, zin.read(info.filename))
        z.writestr("word/document.xml", doc)
        z.writestr("word/_rels/document.xml.rels", rels)
        z.writestr("word/styles.xml", styles)
        z.writestr("word/settings.xml", settings_com_update(settings))
        for nome, dados in midia.arquivos:
            z.writestr(nome, dados)
    print("gerado:", os.path.normpath(DST), "| figuras:", len(figuras), "| tabelas:", len(tabelas),
          "| secoes no sumario:", len(sumario))


def settings_com_update(s):
    """Pede ao Word que atualize sumario e listas ao abrir (paginas reais).
    No schema CT_Settings, updateFields vem antes de compat."""
    assert "<w:compat>" in s
    return s.replace("<w:compat>", '<w:updateFields w:val="true"/><w:compat>', 1)


if __name__ == "__main__":
    main()
