package br.com.auth.dominio.interfaces;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;

import java.util.List;
import java.util.Map;

public interface IServicoAnaliseComportamentalIA {

    /**
     * Analisa o comportamento do usuário usando algoritmos de IA
     */
    PerfilComportamentalIA analisarComportamento(Usuario usuario, DadosContextoAcesso dadosContexto);

    /**
     * Classifica se o acesso é esperado ou anômalo
     */
    PerfilComportamentalIA.ClassificacaoAcesso classificarAcesso(Usuario usuario, DadosContextoAcesso dadosContexto);

    /**
     * Calcula score de anomalia usando Isolation Forest
     */
    Double calcularScoreIsolationForest(DadosFeatures features);

    /**
     * Calcula score de anomalia usando Random Forest
     */
    Double calcularScoreRandomForest(DadosFeatures features);

    /**
     * Calcula score de anomalia usando Deep Learning
     */
    Double calcularScoreDeepLearning(DadosFeatures features);

    /**
     * Calcula score ensemble combinando múltiplos algoritmos
     */
    Double calcularScoreEnsemble(DadosFeatures features);

    /**
     * Treina os modelos com novos dados
     */
    void treinarModelos();

    /**
     * Atualiza modelo com feedback do usuário
     */
    void atualizarModeloComFeedback(Long perfilId, boolean acessoLegitimo);

    /**
     * Obtém histórico de análises do usuário
     */
    List<PerfilComportamentalIA> obterHistoricoAnalises(Usuario usuario, int limite);

    /**
     * Obtém estatísticas de anomalias
     */
    EstatisticasAnomalias obterEstatisticasAnomalias();

    /**
     * Dados de contexto do acesso
     */
    record DadosContextoAcesso(
            String enderecoIp,
            String userAgent,
            String localizacaoGeografica,
            String timezone,
            String idiomaBrowser,
            String resolucaoTela,
            Integer tentativasLogin
    ) {}

    /**
     * Features extraídas para machine learning
     */
    record DadosFeatures(
            Double horaAcesso,
            Double diaSemana,
            Double frequenciaAcessoSemanal,
            Boolean ipJaUtilizado,
            Boolean dispositivoJaUtilizado,
            Boolean localizacaoJaUtilizada,
            Double distanciaLocalizacaoHabitualKm,
            Double diferencaHorarioHabitualHoras,
            Double tempoDesdeUltimoAcessoHoras,
            Double mediaSessoesDiarias,
            Double desvioPadraoHorarios,
            Integer totalIpsDistintos,
            Integer totalDispositivosDistintos,
            Double padroesNavegacaoScore
    ) {}

    /**
     * Estatísticas de anomalias
     */
    record EstatisticasAnomalias(
            Long totalAnalises,
            Long totalAnomalias,
            Double percentualAnomalias,
            Double scoreAnomaliaMedia,
            Map<PerfilComportamentalIA.AlgoritmoIA, Double> acuraciaAlgoritmos
    ) {}
} 