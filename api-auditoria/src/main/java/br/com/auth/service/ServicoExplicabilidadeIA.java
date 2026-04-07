package br.com.auth.service;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoExplicabilidadeIA {

    private final RepositorioPerfilComportamentalIA repositorioPerfil;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioScoreConfianca repositorioScoreConfianca;

    // Pesos dos fatores comportamentais
    private static final double PESO_PADRAO_HORARIO = 0.25;
    private static final double PESO_PADRAO_LOCALIZACAO = 0.20;
    private static final double PESO_PADRAO_DISPOSITIVO = 0.20;
    private static final double PESO_FREQUENCIA_ACESSO = 0.15;
    private static final double PESO_SEQUENCIA_NAVEGACAO = 0.20;

    // Pesos dos algoritmos
    private static final double PESO_ISOLATION_FOREST = 0.4;
    private static final double PESO_RANDOM_FOREST = 0.3;
    private static final double PESO_DEEP_LEARNING = 0.3;

    public ExplicabilidadeResponse gerarExplicacao(Long usuarioId) {
        log.info("Gerando explicacao de IA para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + usuarioId));

        PerfilComportamentalIA perfilAtual = repositorioPerfil.findTopByUsuarioOrderByCriadoEmDesc(usuario)
                .orElseThrow(() -> new RuntimeException("Nenhum perfil comportamental encontrado para usuario: " + usuarioId));

        // Buscar score de confianca do usuario
        Double scoreConfianca = repositorioScoreConfianca.findFirstByUsuarioOrderByIdDesc(usuario)
                .map(ScoreConfianca::getScoreAtual)
                .orElse(0.5);

        List<FatorContribuicao> fatores = calcularFatoresContribuicao(perfilAtual);

        String classificacaoIA = perfilAtual.getClassificacaoAcesso() != null
                ? perfilAtual.getClassificacaoAcesso().name()
                : "DESCONHECIDO";

        Double scoreEnsemble = perfilAtual.getEnsembleScore() != null ? perfilAtual.getEnsembleScore() : 0.0;

        // Ajustar classificacao com base no score de confianca
        String classificacao = ajustarClassificacaoComScoreConfianca(classificacaoIA, scoreConfianca);

        String decisaoRecomendada = gerarDecisaoRecomendada(classificacao, scoreEnsemble, scoreConfianca);
        String motivoDecisao = gerarMotivoDecisao(fatores, classificacao);

        Map<String, double[]> comparacaoHabitual = compararComPerfilHabitual(usuarioId);

        return new ExplicabilidadeResponse(
                usuarioId,
                classificacao,
                scoreEnsemble,
                scoreConfianca,
                decisaoRecomendada,
                motivoDecisao,
                fatores,
                comparacaoHabitual,
                LocalDateTime.now()
        );
    }

    private String ajustarClassificacaoComScoreConfianca(String classificacaoIA, double scoreConfianca) {
        // Se o score de confianca e muito baixo, ajustar a classificacao independente da IA
        if (scoreConfianca < 0.2) {
            return "ALTAMENTE_SUSPEITO";
        } else if (scoreConfianca < 0.5) {
            // Se a IA diz ESPERADO mas o score e baixo, promover para SUSPEITO
            if ("ESPERADO".equals(classificacaoIA)) {
                return "SUSPEITO";
            }
            return classificacaoIA;
        }
        return classificacaoIA;
    }

    public Map<String, double[]> compararComPerfilHabitual(Long usuarioId) {
        log.info("Comparando perfil atual com habitual para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + usuarioId));

        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        List<PerfilComportamentalIA> historico = repositorioPerfil.findByUsuarioAndDataMaiorQue(usuario, trintaDiasAtras);

        PerfilComportamentalIA perfilAtual = repositorioPerfil.findTopByUsuarioOrderByCriadoEmDesc(usuario)
                .orElse(null);

        Map<String, double[]> comparacao = new LinkedHashMap<>();

        if (historico.isEmpty() || perfilAtual == null) {
            return comparacao;
        }

        // Calcular medias historicas
        double mediaHorario = historico.stream()
                .filter(p -> p.getPadraoHorarioScore() != null)
                .mapToDouble(PerfilComportamentalIA::getPadraoHorarioScore)
                .average().orElse(0.0);

        double mediaLocalizacao = historico.stream()
                .filter(p -> p.getPadraoLocalizacaoScore() != null)
                .mapToDouble(PerfilComportamentalIA::getPadraoLocalizacaoScore)
                .average().orElse(0.0);

        double mediaDispositivo = historico.stream()
                .filter(p -> p.getPadraoDispositivoScore() != null)
                .mapToDouble(PerfilComportamentalIA::getPadraoDispositivoScore)
                .average().orElse(0.0);

        double mediaFrequencia = historico.stream()
                .filter(p -> p.getFrequenciaAcessoScore() != null)
                .mapToDouble(PerfilComportamentalIA::getFrequenciaAcessoScore)
                .average().orElse(0.0);

        double mediaNavegacao = historico.stream()
                .filter(p -> p.getSequenciaNavegacaoScore() != null)
                .mapToDouble(PerfilComportamentalIA::getSequenciaNavegacaoScore)
                .average().orElse(0.0);

        // [atual, media, desvio]
        double atualHorario = perfilAtual.getPadraoHorarioScore() != null ? perfilAtual.getPadraoHorarioScore() : 0.0;
        double atualLocalizacao = perfilAtual.getPadraoLocalizacaoScore() != null ? perfilAtual.getPadraoLocalizacaoScore() : 0.0;
        double atualDispositivo = perfilAtual.getPadraoDispositivoScore() != null ? perfilAtual.getPadraoDispositivoScore() : 0.0;
        double atualFrequencia = perfilAtual.getFrequenciaAcessoScore() != null ? perfilAtual.getFrequenciaAcessoScore() : 0.0;
        double atualNavegacao = perfilAtual.getSequenciaNavegacaoScore() != null ? perfilAtual.getSequenciaNavegacaoScore() : 0.0;

        comparacao.put("padraoHorario", new double[]{atualHorario, mediaHorario, atualHorario - mediaHorario});
        comparacao.put("padraoLocalizacao", new double[]{atualLocalizacao, mediaLocalizacao, atualLocalizacao - mediaLocalizacao});
        comparacao.put("padraoDispositivo", new double[]{atualDispositivo, mediaDispositivo, atualDispositivo - mediaDispositivo});
        comparacao.put("frequenciaAcesso", new double[]{atualFrequencia, mediaFrequencia, atualFrequencia - mediaFrequencia});
        comparacao.put("sequenciaNavegacao", new double[]{atualNavegacao, mediaNavegacao, atualNavegacao - mediaNavegacao});

        return comparacao;
    }

    private List<FatorContribuicao> calcularFatoresContribuicao(PerfilComportamentalIA perfil) {
        List<FatorContribuicao> fatores = new ArrayList<>();

        // Fatores comportamentais
        fatores.add(criarFator("Padrao Horario",
                perfil.getPadraoHorarioScore(), PESO_PADRAO_HORARIO));
        fatores.add(criarFator("Padrao Localizacao",
                perfil.getPadraoLocalizacaoScore(), PESO_PADRAO_LOCALIZACAO));
        fatores.add(criarFator("Padrao Dispositivo",
                perfil.getPadraoDispositivoScore(), PESO_PADRAO_DISPOSITIVO));
        fatores.add(criarFator("Frequencia Acesso",
                perfil.getFrequenciaAcessoScore(), PESO_FREQUENCIA_ACESSO));
        fatores.add(criarFator("Sequencia Navegacao",
                perfil.getSequenciaNavegacaoScore(), PESO_SEQUENCIA_NAVEGACAO));

        // Fatores de algoritmos
        fatores.add(criarFator("Isolation Forest",
                perfil.getIsolationForestScore(), PESO_ISOLATION_FOREST));
        fatores.add(criarFator("Random Forest",
                perfil.getRandomForestScore(), PESO_RANDOM_FOREST));
        fatores.add(criarFator("Deep Learning",
                perfil.getDeepLearningScore(), PESO_DEEP_LEARNING));

        return fatores;
    }

    private FatorContribuicao criarFator(String nome, Double score, double peso) {
        double scoreAtual = score != null ? score : 0.0;
        double contribuicao = scoreAtual * peso;
        String nivelRisco = classificarNivelRisco(scoreAtual);
        String descricao = gerarDescricaoFator(nome, scoreAtual, contribuicao);

        return new FatorContribuicao(nome, scoreAtual, peso, contribuicao, nivelRisco, descricao);
    }

    private String classificarNivelRisco(double score) {
        if (score <= 0.3) return "BAIXO";
        if (score <= 0.5) return "MODERADO";
        if (score <= 0.7) return "ALTO";
        return "CRITICO";
    }

    private String gerarDescricaoFator(String nome, double score, double contribuicao) {
        String percentual = String.format("%.0f%%", contribuicao * 100);
        if (score <= 0.3) {
            return nome + " dentro do padrao normal (contribuicao " + percentual + ")";
        } else if (score <= 0.7) {
            return nome + " com desvio moderado (contribuicao " + percentual + ")";
        } else {
            return nome + " com desvio significativo (contribuicao " + percentual + ")";
        }
    }

    private String gerarDecisaoRecomendada(String classificacao, double scoreEnsemble, double scoreConfianca) {
        // Score de confianca tem prioridade: thresholds alinhados com ServicoScoreConfianca
        if (scoreConfianca < 0.2) {
            return "BLOQUEAR - Score de confianca muito baixo (" + String.format("%.3f", scoreConfianca) + ")";
        }
        if (scoreConfianca < 0.5) {
            return "VERIFICAR - Score de confianca abaixo do limiar (" + String.format("%.3f", scoreConfianca) + ")";
        }

        return switch (classificacao) {
            case "ESPERADO" -> "PERMITIR - Acesso dentro dos padroes normais";
            case "SUSPEITO" -> "VERIFICAR - Solicitar autenticacao adicional";
            case "ANOMALO" -> "BLOQUEAR - Acesso com padrao significativamente diferente";
            case "ALTAMENTE_SUSPEITO" -> "BLOQUEAR - Alto risco de acesso fraudulento";
            default -> scoreEnsemble > 0.7 ? "BLOQUEAR" : scoreEnsemble > 0.4 ? "VERIFICAR" : "PERMITIR";
        };
    }

    private String gerarMotivoDecisao(List<FatorContribuicao> fatores, String classificacao) {
        // Ordenar fatores por contribuicao decrescente
        List<FatorContribuicao> fatoresOrdenados = fatores.stream()
                .sorted(Comparator.comparingDouble(FatorContribuicao::contribuicao).reversed())
                .toList();

        StringBuilder motivo = new StringBuilder();

        if ("ESPERADO".equals(classificacao)) {
            motivo.append("Acesso permitido: todos os fatores dentro dos padroes normais");
        } else {
            String acao = "ANOMALO".equals(classificacao) || "ALTAMENTE_SUSPEITO".equals(classificacao)
                    ? "Acesso bloqueado" : "Acesso suspeito";
            motivo.append(acao).append(": ");

            List<String> motivos = new ArrayList<>();
            for (FatorContribuicao fator : fatoresOrdenados) {
                if (fator.scoreAtual() > 0.3) {
                    String percentual = String.format("%.0f%%", fator.contribuicao() * 100);
                    String descricaoFator = fator.nome().toLowerCase().replace("padrao ", "").replace("sequencia ", "");
                    if (fator.scoreAtual() > 0.7) {
                        motivos.add(descricaoFator + " incomum (contribuicao " + percentual + ")");
                    } else {
                        motivos.add(descricaoFator + " moderadamente diferente (contribuicao " + percentual + ")");
                    }
                }
            }

            if (motivos.isEmpty()) {
                motivo.append("combinacao de fatores com desvio acumulado");
            } else {
                motivo.append(String.join(" e ", motivos));
            }
        }

        return motivo.toString();
    }

    // Inner records

    public record ExplicabilidadeResponse(
            Long usuarioId,
            String classificacao,
            Double scoreEnsemble,
            Double scoreConfianca,
            String decisaoRecomendada,
            String motivoDecisao,
            List<FatorContribuicao> fatores,
            Map<String, double[]> comparacaoHabitual,
            LocalDateTime timestamp
    ) {}

    public record FatorContribuicao(
            String nome,
            Double scoreAtual,
            Double peso,
            Double contribuicao,
            String nivelRisco,
            String descricao
    ) {}
}
