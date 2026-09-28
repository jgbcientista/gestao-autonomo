# E-mail ao Prof. Cleber: ausência em 24/09 e versão revisada

**Para:** cleberlira@ifba.edu.br
**Assunto:** Reunião de 24/09 e revisão do projeto a partir das avaliações do SBESC

---

Prof. Cleber, boa tarde!

Peço desculpas por não ter comparecido à reunião de 24/09. [MOTIVO, se quiser informar.] Sei que a pauta era a qualificação e lamento ter atrasado esse encaminhamento.

Li com atenção as duas avaliações do SBESC e já comecei os ajustes. Resumo:

**1. Correções no texto.** O revisor 1 baixou o repositório, rodou o script e apontou afirmações do artigo que o código não sustenta. A principal: a camada de blockchain está simulada no protótipo, sem uma rede Hyperledger Fabric real. Revisei o texto linha a linha contra o código. Na nova versão, a auditoria em blockchain aparece como proposta de projeto, e retirei todas as afirmações sem suporte (imutabilidade validada, integração com Keycloak, ELK/Grafana, entre outras). Também corrigi a descrição do gerador de dados e uma referência, e publiquei os hiperparâmetros.

**2. Novos experimentos,** respondendo ao que os dois revisores pediram:
- **Decisão em três faixas:** 7,3% dos ataques passam sem desafio, 62,5% são bloqueados e 2,7% dos acessos legítimos recebem MFA ou bloqueio.
- **Ensemble contra cada modelo isolado:** o ensemble ganha cerca de 3 pontos de F1, o que confirma a observação do revisor.
- **Pesos do ensemble:** os pesos atuais não são os melhores (19º lugar entre 66 combinações).
- **Proporção de ataques:** com 1% de ataques, o F1 cai para 0,63.
- **Evasão adversarial:** um atacante que imita o horário e o ritmo das tentativas reduz a detecção de 77,6% para 50,7%, e com um proxy residencial, para 18%. É a limitação mais importante que apareceu e mostra a necessidade de critérios mais difíceis de forjar.

**3. Pendências que gostaria de discutir com o senhor:**
- Implementar a rede Fabric real (com pelo menos duas organizações, medindo latência e teste de adulteração) antes da qualificação, ou tratá-la como trabalho futuro.
- Submeter o artigo revisado a outro evento, e em qual formato (artigo curto ou *work in progress*, como sugeriu o revisor 2).
- Como incorporar essas mudanças ao texto do projeto de qualificação.

Preparei um documento que relaciona cada crítica dos revisores à alteração feita e ao que ainda falta. Levo esse documento e a versão revisada para a reunião.

Teria disponibilidade em [DIA/HORÁRIO 1] ou [DIA/HORÁRIO 2]?

Atenciosamente,
João Guedes de Brito
PPGESP / IFBA
