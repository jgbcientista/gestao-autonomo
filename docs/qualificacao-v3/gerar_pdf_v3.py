# -*- coding: utf-8 -*-
"""Gera o PDF da qualificacao v3 a partir de conteudo.py (mesma fonte do .docx), via Edge headless.

Formatacao ABNT: A4, margens 3/2 cm, Arial 12, entrelinha 1,5; numeracao no canto superior direito,
contada a partir da folha de rosto e exibida a partir da introducao. O sumario e as listas recebem as
paginas em duas passadas: a 1a renderizacao marca cada titulo com um texto invisivel e o pypdf localiza
em que pagina ele caiu; a 2a renderizacao preenche os numeros.

    python -I gerar_pdf_v3.py
"""
import html, os, re, subprocess, sys, tempfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import conteudo as K
from pypdf import PdfReader, PdfWriter

HERE = os.path.dirname(os.path.abspath(__file__))
FIG = os.path.join(HERE, "figuras")
DST = os.path.join(HERE, "..", "doc-qualificacao-ifba-v3.pdf")
EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"

CSS = """
@page { size: A4; margin: 3cm 2cm 2cm 3cm;
        @top-right { content: counter(page); font: 10pt Arial, sans-serif; vertical-align: bottom; padding-bottom: 0.6cm; } }
@page semnum { @top-right { content: none; } }
html { font: 12pt/1.5 Arial, sans-serif; color: #000; }
body { margin: 0; }
.pre { page: semnum; }
p { margin: 0; text-align: justify; text-indent: 1.25cm; hyphens: auto; }
.quebra { break-before: page; }
h1, h2, h3 { font-size: 12pt; margin: 0; break-after: avoid; position: relative; }
h1 { text-transform: uppercase; font-weight: bold; margin-bottom: 18pt; }
h1.cap { break-before: page; }
h2 { font-weight: bold; margin: 18pt 0 12pt; }
h3 { font-weight: normal; margin: 12pt 0 6pt; }
.titpre { text-align: center; font-weight: bold; text-transform: uppercase; margin-bottom: 18pt; position: relative; }
.quebra.titpre, h1.cap { padding-top: 4pt; }  /* evita que acentos (REFERÊNCIAS) vazem para a pagina anterior */
th.leg { font-weight: normal; border: 0; padding: 0 0 4pt; }
.al { margin: 0 0 0 2cm; text-indent: -0.75cm; }
.cod { font: 10pt/1.2 "Courier New", monospace; margin: 6pt 1cm 6pt 1.25cm; text-indent: 0; text-align: left; }
.eq { display: flex; justify-content: space-between; margin: 6pt 0; text-indent: 0; }
.eq span:first-child { flex: 1; text-align: center; }
figure, .tabela { margin: 12pt 0; break-inside: avoid; }
.leg { font-size: 10pt; line-height: 1.2; text-align: center; margin: 0 0 4pt; text-indent: 0; position: relative; }
.fonte { font-size: 10pt; line-height: 1.2; text-align: center; margin-top: 4pt; text-indent: 0; }
figure { text-align: center; }
figure img { max-width: 100%; }
table { width: 100%; border-collapse: collapse; font-size: 10pt; line-height: 1.2;
        border-bottom: 1px solid #000; }
th.h { border-top: 1px solid #000; border-bottom: 1px solid #000; padding: 3pt 4pt; text-align: center; vertical-align: middle; }
td { padding: 2pt 4pt; vertical-align: middle; text-align: left; }
thead { display: table-header-group; }
tr { break-inside: avoid; }
.tabela { break-inside: auto; }
.ref p { text-indent: 0; text-align: left; line-height: 1.0; margin-bottom: 12pt; }
.toc div { display: flex; align-items: baseline; text-indent: 0; }
.toc .t { flex: 0 1 auto; }
.toc .d { flex: 1 1 auto; border-bottom: 1px dotted #000; margin: 0 4pt; transform: translateY(-3pt); min-width: 1cm; }
.toc .n { flex: 0 0 auto; }
.toc .l1 { font-weight: bold; margin-top: 6pt; }
.toc .l1.ref { font-weight: bold; }
.toc .l3 { padding-left: 1cm; }
.sig div { display: flex; text-indent: 0; margin-bottom: 3pt; line-height: 1.3; }
.sig b { flex: 0 0 2.5cm; font-weight: normal; }
.resumo p { text-indent: 0; }
.mk { position: absolute; left: 0; top: 0; font-size: 3pt; color: #fff; white-space: nowrap; }
.capa { page: semnum; height: 24.6cm; display: flex; flex-direction: column; align-items: center; text-align: center; }
.capa .ins { font-weight: bold; text-transform: uppercase; }
.capa .tit { font-weight: bold; margin-top: auto; }
.capa .rodape { margin-top: auto; }
.rosto { height: 24.6cm; display: flex; flex-direction: column; text-align: center; }
.rosto .nat { margin: 2cm 0 0 8cm; text-align: justify; line-height: 1.2; font-size: 11pt; }
"""


def inline(t):
    """*italico* **negrito** _{sub} -> HTML."""
    t = html.escape(t, quote=False)
    t = re.sub(r"_\{(.*?)\}", r"<sub>\1</sub>", t)
    out, b, i, k = [], False, False, 0
    while k < len(t):
        if t.startswith("**", k):
            out.append("</b>" if b else "<b>"); b = not b; k += 2
        elif t[k] == "*":
            out.append("</i>" if i else "<i>"); i = not i; k += 1
        else:
            out.append(t[k]); k += 1
    return "".join(out)


def plain(t):
    return re.sub(r"_\{(.*?)\}", r"\1", t).replace("*", "")


class Doc:
    def __init__(self, paginas):
        self.pag = paginas      # marcador -> numero da pagina (2a passada)
        self.marcas = []        # (marcador, nivel, texto) na ordem

    def mk(self, chave, nivel, texto):
        self.marcas.append((chave, nivel, texto))
        return '<span class="mk">@@%s@@</span>' % chave if self.pag is None else ""

    def corpo(self):
        h, figs, tabs, toc = [], [], [], []
        ncap = nsec = nsub = nfig = ntab = 0
        for bl in K.C:
            tp = bl[0]
            if tp == "cap":
                ncap += 1; nsec = nsub = 0
                t = "%d %s" % (ncap, bl[1]); key = "C%d" % ncap
                toc.append((1, t, key))
                h.append('<h1 class="cap">%s%s</h1>' % (self.mk(key, 1, t), inline(t)))
            elif tp == "sec":
                nsec += 1; nsub = 0
                t = "%d.%d %s" % (ncap, nsec, bl[1]); key = "S%d_%d" % (ncap, nsec)
                toc.append((2, t, key))
                h.append("<h2>%s%s</h2>" % (self.mk(key, 2, t), inline(t)))
            elif tp == "sub":
                nsub += 1
                t = "%d.%d.%d %s" % (ncap, nsec, nsub, bl[1]); key = "U%d_%d_%d" % (ncap, nsec, nsub)
                toc.append((3, t, key))
                h.append("<h3>%s%s</h3>" % (self.mk(key, 3, t), inline(t)))
            elif tp == "p":
                h.append("<p>%s</p>" % inline(bl[1]))
            elif tp == "al":
                for k, it in enumerate(bl[1]):
                    h.append('<p class="al">%s) %s</p>' % ("abcdefghij"[k], inline(it)))
            elif tp == "cod":
                h.append('<p class="cod">%s</p>' % html.escape(bl[1]))
            elif tp == "eq":
                h.append('<p class="eq"><span>%s</span><span>(%s)</span></p>' % (inline(bl[1]), bl[2]))
            elif tp == "fig":
                _, arq, tit, fnt, larg = bl
                nfig += 1; key = "F%d" % nfig; t = "Figura %d – %s" % (nfig, tit)
                figs.append((t, key))
                h.append('<figure><p class="leg">%s%s</p><img src="%s" style="width:%.1fcm">'
                         '<p class="fonte">Fonte: %s</p></figure>'
                         % (self.mk(key, 0, t), inline(t), "file:///" + os.path.join(FIG, arq).replace("\\", "/"),
                            min(larg, 16.0), inline(fnt)))
            elif tp == "tab":
                _, tit, cab, linhas, larg, fnt = bl
                ntab += 1; key = "T%d" % ntab; t = "Tabela %d – %s" % (ntab, tit)
                tabs.append((t, key))
                tot = sum(larg)
                cols = "".join('<col style="width:%.1f%%">' % (100 * w / tot) for w in larg)
                th = "".join('<th class="h">%s</th>' % inline(c) for c in cab)
                trs = "".join("<tr>%s</tr>" % "".join("<td>%s</td>" % inline(c) for c in ln) for ln in linhas)
                # legenda dentro do <thead>: nunca se separa da 1a linha e se repete se a tabela continuar
                # na pagina seguinte (o Chromium permite quebrar entre <caption> e a tabela)
                h.append('<div class="tabela"><table><colgroup>%s</colgroup><thead><tr><th class="leg" colspan="%d">%s%s</th></tr>'
                         '<tr>%s</tr></thead><tbody>%s</tbody></table><p class="fonte">Fonte: %s</p></div>'
                         % (cols, len(cab), self.mk(key, 0, t), inline(t), th, trs, inline(fnt)))
        toc.append((1, "REFERÊNCIAS", "REF"))
        h.append('<div class="ref"><p class="titpre quebra">%sREFERÊNCIAS</p>%s</div>'
                 % (self.mk("REF", 1, "REFERÊNCIAS"), "".join("<p>%s</p>" % inline(r) for r in K.REFERENCIAS)))
        return "".join(h), figs, tabs, toc

    def linha_toc(self, nivel, texto, key):
        n = self.pag.get(key, "") if self.pag else ""
        cls = "l%d%s" % (nivel, " ref" if key == "REF" else "")
        return '<div class="%s"><span class="t">%s</span><span class="d"></span><span class="n">%s</span></div>' % (
            cls, inline(texto), n)

    def html(self):
        corpo, figs, tabs, toc = self.corpo()
        pre = []
        pre.append('<section class="pre rosto"><div>JOÃO GUEDES DE BRITO</div>'
                   '<div style="margin-top:auto"><b>%s</b></div>'
                   '<div class="nat">Qualificação de Mestrado apresentada ao Programa de Pós-Graduação em Engenharia de '
                   'Sistemas e Produtos do Instituto Federal de Educação, Ciência e Tecnologia da Bahia, como requisito '
                   'parcial para obtenção do grau de Mestre em Engenharia de Sistemas e Produtos.<br><br>'
                   'Orientador: Cleber Jorge Lira de Santana</div>'
                   '<div style="margin-top:auto">Salvador<br>2026</div></section>' % html.escape(K.TITULO))
        pre.append('<section class="pre resumo quebra"><p class="titpre">RESUMO</p><p>%s</p><br><p><b>Palavras-chave:</b> %s</p></section>'
                   % (inline(K.RESUMO), inline(K.PALAVRAS_CHAVE)))
        pre.append('<section class="pre resumo quebra" lang="en"><p class="titpre">ABSTRACT</p><p>%s</p><br><p><b>Keywords:</b> %s</p></section>'
                   % (inline(K.ABSTRACT), inline(K.KEYWORDS)))
        pre.append('<section class="pre toc quebra"><p class="titpre">LISTA DE FIGURAS</p>%s</section>'
                   % "".join(self.linha_toc(0, t, k) for t, k in figs))
        pre.append('<section class="pre toc quebra"><p class="titpre">LISTA DE TABELAS</p>%s</section>'
                   % "".join(self.linha_toc(0, t, k) for t, k in tabs))
        pre.append('<section class="pre sig quebra"><p class="titpre">LISTA DE SIGLAS</p>%s</section>'
                   % "".join("<div><b>%s</b><span>%s</span></div>" % (inline(s), inline(d)) for s, d in K.SIGLAS))
        pre.append('<section class="pre toc quebra"><p class="titpre">SUMÁRIO</p>%s</section>'
                   % "".join(self.linha_toc(n, t, k) for n, t, k in toc))
        return ('<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>%s</title><style>%s</style></head>'
                '<body>%s<main>%s</main></body></html>' % (html.escape(K.TITULO), CSS, "".join(pre), corpo))


def capa_html():
    logo = "file:///" + os.path.join(FIG, "logo-ifba.png").replace("\\", "/")
    return ('<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><style>%s</style></head><body>'
            '<section class="capa"><img src="%s" style="width:2.2cm">'
            '<div class="ins" style="margin-top:12pt">Instituto Federal de Educação, Ciência e Tecnologia da Bahia</div>'
            '<div class="ins">Programa de Pós-Graduação em Engenharia de Sistemas e Produtos</div>'
            '<div style="margin-top:2.5cm">JOÃO GUEDES DE BRITO</div>'
            '<div class="tit">%s</div><div style="margin-top:12pt">Qualificação de Mestrado</div>'
            '<div class="rodape">Salvador<br>2026</div></section></body></html>'
            % (CSS.replace("@top-right { content: counter(page);", "@top-right { content: none;"), logo,
               html.escape(K.TITULO)))


def imprimir(html_txt, pdf, tmp):
    src = os.path.join(tmp, os.path.basename(pdf) + ".html")
    open(src, "w", encoding="utf-8").write(html_txt)
    subprocess.run([EDGE, "--headless", "--disable-gpu", "--no-pdf-header-footer", "--allow-file-access-from-files",
                    "--print-to-pdf=" + pdf, "file:///" + src.replace("\\", "/")],
                   check=True, capture_output=True, timeout=180)
    assert os.path.exists(pdf), "Edge nao gerou " + pdf


def localizar(pdf):
    pag = {}
    for i, page in enumerate(PdfReader(pdf).pages):
        for m in re.finditer(r"@@(\w+)@@", page.extract_text() or ""):
            pag.setdefault(m.group(1), i + 1)   # pagina 1 = folha de rosto
    return pag


def main():
    with tempfile.TemporaryDirectory() as tmp:
        p1 = os.path.join(tmp, "passada1.pdf")
        d1 = Doc(None)
        imprimir(d1.html(), p1, tmp)
        pag = localizar(p1)
        faltam = [k for k, _, _ in d1.marcas if k not in pag]
        assert not faltam, "titulos nao localizados no PDF: %s" % faltam
        n1 = len(PdfReader(p1).pages)

        p2 = os.path.join(tmp, "passada2.pdf")
        imprimir(Doc(pag).html(), p2, tmp)
        n2 = len(PdfReader(p2).pages)
        assert n1 == n2, "paginacao mudou entre as passadas (%d -> %d)" % (n1, n2)

        pc = os.path.join(tmp, "capa.pdf")
        imprimir(capa_html(), pc, tmp)

        w = PdfWriter()
        w.append(pc, pages=(0, 1))   # capa: nao entra na contagem de paginas (ABNT)
        w.append(p2)
        w.add_metadata({"/Title": K.TITULO.title(), "/Author": "João Guedes de Brito",
                        "/Subject": "Qualificação de Mestrado - PPGESP/IFBA"})
        if os.path.exists(DST): os.remove(DST)
        with open(DST, "wb") as f:
            w.write(f)
    print("gerado:", os.path.normpath(DST), "| paginas:", n2 + 1,
          "| cap.1 na pag.", pag["C1"], "| referencias na pag.", pag["REF"])


if __name__ == "__main__":
    main()
