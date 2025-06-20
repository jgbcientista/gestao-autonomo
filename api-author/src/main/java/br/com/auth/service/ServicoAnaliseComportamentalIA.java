package br.com.auth.service;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioDadosTreinamentoIA;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoAnaliseComportamentalIA implements IServicoAnaliseComportamentalIA {

    private final RepositorioPerfilComportamentalIA repositorioPerfilIA;
    private final RepositorioDadosTreinamentoIA repositorioTreinamento;
    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final ServicoExtracaoFeatures servicoExtracaoFeatures;
    private final ServicoIsolationForest servicoIsolationForest;
    private final ServicoRandomForest servicoRandomForest;
    private final ServicoDeepLearning servicoDeepLearning;

    // Thresholds para classificação
    private static final double THRESHOLD_ANOMALIA = 0.7;
    private static final double THRESHOLD_SUSPEITO = 0.5;
    private static final double THRESHOLD_ALTAMENTE_SUSPEITO = 0.9;

    @Override
    @Transactional
    public PerfilComportamentalIA analisarComportamento(Usuario usuario, DadosContextoAcesso dadosContexto) {
        log.info("Iniciando análise comportamental para usuário: {}", usuario.getEmail());
        
        long inicioProcessamento = System.currentTimeMillis();

        try {
            // 1. Extrair features do comportamento
            DadosFeatures features = servicoExtracaoFeatures.extrairFeatures(usuario, dadosContexto);

            // 2. Calcular scores dos algoritmos
            Double scoreIsolationForest = calcularScoreIsolationForest(features);
            Double scoreRandomForest = calcularScoreRandomForest(features);
            Double scoreDeepLearning = calcularScoreDeepLearning(features);
            Double scoreEnsemble = calcularScoreEnsemble(features);

            // 3. Classificar acesso
            PerfilComportamentalIA.ClassificacaoAcesso classificacao = 
                classificarAcessoPorScore(scoreEnsemble);

            // 4. Calcular scores individuais de comportamento
            Map<String, Double> scoresComportamentais = 
                calcularScoresComportamentais(usuario, dadosContexto, features);

            // 5. Criar perfil comportamental
            PerfilComportamentalIA perfil = PerfilComportamentalIA.builder()
                .usuario(usuario)
                .scoreAnomalia(scoreEnsemble)
                .classificacaoAcesso(classificacao)
                .algoritmoUtilizado(PerfilComportamentalIA.AlgoritmoIA.ENSEMBLE)
                .confiancaPredicao(calcularConfiancaPredicao(scoreIsolationForest, scoreRandomForest, scoreDeepLearning))
                .padraoHorarioScore(scoresComportamentais.get("horario"))
                .padraoLocalizacaoScore(scoresComportamentais.get("localizacao"))
                .padraoDispositivoScore(scoresComportamentais.get("dispositivo"))
                .frequenciaAcessoScore(scoresComportamentais.get("frequencia"))
                .sequenciaNavegacaoScore(scoresComportamentais.get("navegacao"))
                .enderecoIp(dadosContexto.enderecoIp())
                .userAgent(dadosContexto.userAgent())
                .localizacaoGeografica(dadosContexto.localizacaoGeografica())
                .horarioAcesso(LocalDateTime.now())
                .diaSemana(LocalDateTime.now().getDayOfWeek().getValue())
                .horaDia(LocalDateTime.now().getHour())
                .mediaSessoesDiarias(features.mediaSessoesDiarias())
                .desvioPadraoHorarios(features.desvioPadraoHorarios())
                .totalIpsDistintos(features.totalIpsDistintos())
                .totalDispositivosDistintos(features.totalDispositivosDistintos())
                .isolationForestScore(scoreIsolationForest)
                .randomForestScore(scoreRandomForest)
                .deepLearningScore(scoreDeepLearning)
                .ensembleScore(scoreEnsemble)
                .versaoModelo("v1.0")
                .tempoProcessamentoMs(System.currentTimeMillis() - inicioProcessamento)
                .build();

            // 6. Salvar perfil
            perfil = repositorioPerfilIA.save(perfil);

            log.info("Análise comportamental concluída. Score: {}, Classificação: {}", 
                scoreEnsemble, classificacao);

            return perfil;

        } catch (Exception e) {
            log.error("Erro na análise comportamental para usuário: {}", usuario.getEmail(), e);
            throw new RuntimeException("Erro na análise comportamental", e);
        }
    }

    @Override
    public PerfilComportamentalIA.ClassificacaoAcesso classificarAcesso(Usuario usuario, DadosContextoAcesso dadosContexto) {
        DadosFeatures features = servicoExtracaoFeatures.extrairFeatures(usuario, dadosContexto);
        Double scoreEnsemble = calcularScoreEnsemble(features);
        return classificarAcessoPorScore(scoreEnsemble);
    }

    @Override
    public Double calcularScoreIsolationForest(DadosFeatures features) {
        return servicoIsolationForest.calcularScore(features);
    }

    @Override
    public Double calcularScoreRandomForest(DadosFeatures features) {
        return servicoRandomForest.calcularScore(features);
    }

    @Override
    public Double calcularScoreDeepLearning(DadosFeatures features) {
        return servicoDeepLearning.calcularScore(features);
    }

    @Override
    public Double calcularScoreEnsemble(DadosFeatures features) {
        Double scoreIF = calcularScoreIsolationForest(features);
        Double scoreRF = calcularScoreRandomForest(features);
        Double scoreDL = calcularScoreDeepLearning(features);

        // Ensemble com pesos diferentes para cada algoritmo
        return (scoreIF * 0.4) + (scoreRF * 0.3) + (scoreDL * 0.3);
    }

    @Override
    @Transactional
    public void treinarModelos() {
        log.info("Iniciando treinamento dos modelos de IA");
        
        try {
            servicoIsolationForest.treinarModelo();
            servicoRandomForest.treinarModelo();
            servicoDeepLearning.treinarModelo();
            
            log.info("Treinamento dos modelos concluído com sucesso");
        } catch (Exception e) {
            log.error("Erro no treinamento dos modelos", e);
            throw new RuntimeException("Erro no treinamento dos modelos", e);
        }
    }

    @Override
    @Transactional
    public void atualizarModeloComFeedback(Long perfilId, boolean acessoLegitimo) {
        Optional<PerfilComportamentalIA> perfilOpt = repositorioPerfilIA.findById(perfilId);
        
        if (perfilOpt.isPresent()) {
            PerfilComportamentalIA perfil = perfilOpt.get();
            
            // Atualizar classificação baseada no feedback
            if (acessoLegitimo && perfil.getClassificacaoAcesso() != PerfilComportamentalIA.ClassificacaoAcesso.ESPERADO) {
                // Falso positivo - ajustar modelos
                servicoIsolationForest.ajustarComFeedback(perfil, true);
                servicoRandomForest.ajustarComFeedback(perfil, true);
                servicoDeepLearning.ajustarComFeedback(perfil, true);
            } else if (!acessoLegitimo && perfil.getClassificacaoAcesso() == PerfilComportamentalIA.ClassificacaoAcesso.ESPERADO) {
                // Falso negativo - ajustar modelos
                servicoIsolationForest.ajustarComFeedback(perfil, false);
                servicoRandomForest.ajustarComFeedback(perfil, false);
                servicoDeepLearning.ajustarComFeedback(perfil, false);
            }
            
            log.info("Modelo atualizado com feedback para perfil: {}", perfilId);
        }
    }

    @Override
    public List<PerfilComportamentalIA> obterHistoricoAnalises(Usuario usuario, int limite) {
        return repositorioPerfilIA.findByUsuarioOrderByCriadoEmDesc(usuario)
            .stream()
            .limit(limite)
            .collect(Collectors.toList());
    }

    @Override
    public EstatisticasAnomalias obterEstatisticasAnomalias() {
        Long totalAnalises = repositorioPerfilIA.count();
        Long totalAnomalias = (long) repositorioPerfilIA.findAnomalias(THRESHOLD_ANOMALIA).size();
        
        Double percentualAnomalias = totalAnalises > 0 ? 
            (totalAnomalias.doubleValue() / totalAnalises.doubleValue()) * 100 : 0.0;

        // Calcular acurácia por algoritmo (simulado)
        Map<PerfilComportamentalIA.AlgoritmoIA, Double> acuraciaAlgoritmos = Map.of(
            PerfilComportamentalIA.AlgoritmoIA.ISOLATION_FOREST, 0.85,
            PerfilComportamentalIA.AlgoritmoIA.RANDOM_FOREST, 0.82,
            PerfilComportamentalIA.AlgoritmoIA.DEEP_LEARNING, 0.88,
            PerfilComportamentalIA.AlgoritmoIA.ENSEMBLE, 0.91
        );

        return new EstatisticasAnomalias(
            totalAnalises,
            totalAnomalias,
            percentualAnomalias,
            THRESHOLD_ANOMALIA,
            acuraciaAlgoritmos
        );
    }

    private PerfilComportamentalIA.ClassificacaoAcesso classificarAcessoPorScore(Double score) {
        if (score >= THRESHOLD_ALTAMENTE_SUSPEITO) {
            return PerfilComportamentalIA.ClassificacaoAcesso.ALTAMENTE_SUSPEITO;
        } else if (score >= THRESHOLD_ANOMALIA) {
            return PerfilComportamentalIA.ClassificacaoAcesso.ANOMALO;
        } else if (score >= THRESHOLD_SUSPEITO) {
            return PerfilComportamentalIA.ClassificacaoAcesso.SUSPEITO;
        } else {
            return PerfilComportamentalIA.ClassificacaoAcesso.ESPERADO;
        }
    }

    private Double calcularConfiancaPredicao(Double scoreIF, Double scoreRF, Double scoreDL) {
        // Calcular variância entre os scores para determinar confiança
        List<Double> scores = Arrays.asList(scoreIF, scoreRF, scoreDL);
        Double media = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        Double variancia = scores.stream()
            .mapToDouble(score -> Math.pow(score - media, 2))
            .average().orElse(0.0);
        
        // Quanto menor a variância, maior a confiança
        return Math.max(0.0, 1.0 - variancia);
    }

    private Map<String, Double> calcularScoresComportamentais(Usuario usuario, 
                                                              DadosContextoAcesso dadosContexto, 
                                                              DadosFeatures features) {
        Map<String, Double> scores = new HashMap<>();
        
        // Score de padrão horário
        scores.put("horario", calcularScorePadraoHorario(usuario, features));
        
        // Score de padrão de localização
        scores.put("localizacao", calcularScorePadraoLocalizacao(usuario, dadosContexto));
        
        // Score de padrão de dispositivo
        scores.put("dispositivo", calcularScorePadraoDispositivo(usuario, dadosContexto));
        
        // Score de frequência de acesso
        scores.put("frequencia", calcularScoreFrequenciaAcesso(features));
        
        // Score de padrão de navegação
        scores.put("navegacao", features.padroesNavegacaoScore());
        
        return scores;
    }

    private Double calcularScorePadraoHorario(Usuario usuario, DadosFeatures features) {
        // Implementação simplificada - na prática usaria histórico real
        if (features.diferencaHorarioHabitualHoras() > 6.0) {
            return 0.8; // Alto risco - horário muito diferente do habitual
        } else if (features.diferencaHorarioHabitualHoras() > 3.0) {
            return 0.5; // Risco médio
        } else {
            return 0.1; // Baixo risco
        }
    }

    private Double calcularScorePadraoLocalizacao(Usuario usuario, DadosContextoAcesso dadosContexto) {
        // Implementação simplificada
        return 0.2; // Placeholder
    }

    private Double calcularScorePadraoDispositivo(Usuario usuario, DadosContextoAcesso dadosContexto) {
        // Implementação simplificada
        return 0.15; // Placeholder
    }

    private Double calcularScoreFrequenciaAcesso(DadosFeatures features) {
        if (features.frequenciaAcessoSemanal() < 1.0) {
            return 0.7; // Usuário pouco ativo - mais suspeito
        } else if (features.frequenciaAcessoSemanal() > 20.0) {
            return 0.6; // Usuário muito ativo - pode ser bot
        } else {
            return 0.2; // Frequência normal
        }
    }
} 