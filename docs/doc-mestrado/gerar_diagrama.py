import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

# Page usable width = 16cm = 6.3in. Height to fit well on page.
fig, ax = plt.subplots(1, 1, figsize=(6.3, 8.5))
ax.set_xlim(0, 12.6)
ax.set_ylim(0, 8.5)
ax.axis('off')

HEADER = '#2C3E50'
BOX = '#3498DB'
GREEN = '#27AE60'
ORANGE = '#F39C12'
RED = '#E74C3C'

fig.patch.set_facecolor('white')
ax.set_facecolor('white')

# Title
ax.text(6.3, 8.2, 'Fluxo de Autenticação Contextual', fontsize=12, fontweight='bold',
        ha='center', va='center', color=HEADER)
ax.text(6.3, 7.95, 'Diagrama de Sequência', fontsize=9, ha='center', va='center', color='#7F8C8D')

# Actors
actors = [
    (1.3, 'Usuário'),
    (3.7, 'Angular\n(Frontend)'),
    (6.3, 'Spring Boot\n(Backend)'),
    (8.9, 'Serviço ML\n(IA)'),
    (11.3, 'Blockchain'),
]

bw, bh = 1.5, 0.5
for x, label in actors:
    box = FancyBboxPatch((x - bw/2, 7.2), bw, bh, boxstyle="round,pad=0.08",
                         facecolor=BOX, edgecolor=HEADER, linewidth=1.2)
    ax.add_patch(box)
    ax.text(x, 7.2 + bh/2, label, fontsize=6.5, fontweight='bold',
            ha='center', va='center', color='white')

# Lifelines
for x, _ in actors:
    ax.plot([x, x], [7.2, 0.4], color='#BDC3C7', linewidth=0.8, linestyle='--', zorder=0)

y = 6.85
dy = 0.5

def arrow(x1, x2, y, label, color=HEADER, fs=6.5):
    ax.annotate('', xy=(x2, y), xytext=(x1, y),
                arrowprops=dict(arrowstyle='->', color=color, lw=1.1))
    mid = (x1 + x2) / 2
    ax.text(mid, y + 0.08, label, fontsize=fs, ha='center', va='bottom', color=HEADER)

def self_arrow(x, y, label, fs=6):
    ax.annotate('', xy=(x + 0.1, y - 0.12), xytext=(x + 0.1, y + 0.03),
                arrowprops=dict(arrowstyle='->', color=HEADER, lw=1.0,
                               connectionstyle='arc3,rad=0.35'))
    ax.text(x + 0.5, y - 0.04, label, fontsize=fs, ha='left', va='center', color=HEADER)

# 1. Credenciais
arrow(1.3, 3.7, y, '1. Credenciais (email + senha)', fs=6)
y -= dy

# 2. POST
arrow(3.7, 6.3, y, '2. POST /api/v1/auth/login', fs=6)
y -= dy

# 3. Validar
self_arrow(6.3, y, '3. Validar credenciais\n    + extrair 14 critérios')
y -= dy

# 4. Analisar
arrow(6.3, 8.9, y, '4. Analisar comportamento\n    (vetor 14 dimensões)', fs=5.5)
y -= dy

# 5. Ensemble
self_arrow(8.9, y, '5. Ensemble:\n    IF(40%)+RF(30%)+DL(30%)')
y -= dy

# 6. Score
arrow(8.9, 6.3, y, '6. Score de risco (0.0-1.0)', color='#7F8C8D', fs=6)
y -= dy

# 7. Decision box
dw, dh = 2.0, 0.35
dec = FancyBboxPatch((6.3 - dw/2, y - dh/2), dw, dh,
                      boxstyle="round,pad=0.04", facecolor='#F8F9FA',
                      edgecolor=HEADER, linewidth=1.0)
ax.add_patch(dec)
ax.text(6.3, y, '7. Decisão Autônoma', fontsize=6.5, fontweight='bold',
        ha='center', va='center', color=HEADER)

y -= 0.45

# Decision outcomes - more vertical spacing, labels clearly separated
# PERMITIDO
ax.annotate('', xy=(3.7, y), xytext=(6.3, y),
            arrowprops=dict(arrowstyle='->', color=GREEN, lw=1.2))
ax.text(5.0, y + 0.08, 'score < 0.3', fontsize=5.5, ha='center', va='bottom',
        color=GREEN, fontweight='bold')
ax.text(3.2, y, 'PERMITIDO', fontsize=6, ha='right', va='center',
        color=GREEN, fontweight='bold')

y -= 0.4

# MFA
ax.annotate('', xy=(3.7, y), xytext=(6.3, y),
            arrowprops=dict(arrowstyle='->', color=ORANGE, lw=1.2))
ax.text(5.0, y + 0.08, '0.3 <= score < 0.7', fontsize=5.5, ha='center', va='bottom',
        color=ORANGE, fontweight='bold')
ax.text(3.2, y, 'REQUER MFA', fontsize=6, ha='right', va='center',
        color=ORANGE, fontweight='bold')

y -= 0.4

# BLOQUEADO
ax.annotate('', xy=(3.7, y), xytext=(6.3, y),
            arrowprops=dict(arrowstyle='->', color=RED, lw=1.2))
ax.text(5.0, y + 0.08, 'score >= 0.7', fontsize=5.5, ha='center', va='bottom',
        color=RED, fontweight='bold')
ax.text(3.2, y, 'BLOQUEADO', fontsize=6, ha='right', va='center',
        color=RED, fontweight='bold')

y -= 0.45

# 8. Blockchain
arrow(6.3, 11.3, y, '8. Registrar evento (hash SHA-3)', fs=5.5)
y -= 0.4
arrow(11.3, 6.3, y, '9. Confirmação (tx hash)', color='#7F8C8D', fs=5.5)

plt.tight_layout(pad=0.2)
plt.savefig('diagrama_sequencia.png', dpi=200, bbox_inches='tight',
            facecolor='white', edgecolor='none')
print('Diagrama salvo: diagrama_sequencia.png')
