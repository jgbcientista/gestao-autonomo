package br.com.auth.service;

import br.com.auth.dominio.entidades.PadraoDigitacao;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioPadraoDigitacao;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servico responsavel pela analise de dinamica de digitacao (Keystroke Dynamics).
 * Captura padroes de digitacao do usuario e compara com baseline para deteccao de anomalias.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoDinamicaDigitacao {

    private final RepositorioPadraoDigitacao repositorioPadraoDigitacao;
    private final RepositorioUsuario repositorioUsuario;
    private final ObjectMapper objectMapper;

    private static final int MIN_AMOSTRAS_BASELINE = 5;
    private static final int AMOSTRAS_PARA_BASELINE = 10;

    /**
     * Evento de tecla capturado no frontend
     */
    public record EventoTecla(String tecla, long keyDown, long keyUp) {}

    /**
     * Captura e processa um padrao de digitacao do usuario.
     * Se o usuario ainda nao possui baseline (menos de 5 amostras), marca como baseline.
     * Se ja possui baseline, calcula score de similaridade.
     */
    @Transactional
    public PadraoDigitacao capturarPadrao(Long usuarioId, List<EventoTecla> eventos) {
        log.info("Capturando padrao de digitacao para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado: " + usuarioId));

        // Calcular hold times e flight times
        List<Long> holdTimes = calcularHoldTimes(eventos);
        List<Long> flightTimes = calcularFlightTimes(eventos);

        double mediaHold = calcularMedia(holdTimes);
        double mediaFlight = calcularMedia(flightTimes);
        double desvioHold = calcularDesvioPadrao(holdTimes, mediaHold);
        double desvioFlight = calcularDesvioPadrao(flightTimes, mediaFlight);

        // Serializar tempos entre teclas como JSON
        String temposJson = serializarTempos(holdTimes, flightTimes);

        long totalAmostras = repositorioPadraoDigitacao.countByUsuario(usuario);
        boolean ehBaseline = totalAmostras < MIN_AMOSTRAS_BASELINE;

        PadraoDigitacao padrao = PadraoDigitacao.builder()
                .usuario(usuario)
                .temposEntreTeclas(temposJson)
                .tempoMedioHold(mediaHold)
                .tempoMedioFlight(mediaFlight)
                .desvioPadraoHold(desvioHold)
                .desvioPadraoFlight(desvioFlight)
                .totalAmostras(eventos.size())
                .ehBaseline(ehBaseline)
                .build();

        // Se baseline existe, calcular similaridade
        Optional<PadraoDigitacao> baselineOpt = repositorioPadraoDigitacao.findByUsuarioAndEhBaselineTrue(usuario);
        if (baselineOpt.isPresent()) {
            double similaridade = calcularSimilaridade(
                    mediaHold, mediaFlight, desvioHold, desvioFlight,
                    baselineOpt.get()
            );
            padrao.setScoreSimilaridade(similaridade);
            log.info("Score de similaridade calculado para usuario {}: {}", usuarioId, similaridade);
        }

        PadraoDigitacao salvo = repositorioPadraoDigitacao.save(padrao);
        log.info("Padrao de digitacao salvo com ID: {} (baseline: {})", salvo.getId(), ehBaseline);
        return salvo;
    }

    /**
     * Compara eventos de digitacao atuais com o baseline do usuario.
     * Retorna score de similaridade entre 0.0 (sem correspondencia) e 1.0 (correspondencia perfeita).
     */
    public double compararComBaseline(Long usuarioId, List<EventoTecla> eventos) {
        log.info("Comparando digitacao com baseline para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado: " + usuarioId));

        Optional<PadraoDigitacao> baselineOpt = repositorioPadraoDigitacao.findByUsuarioAndEhBaselineTrue(usuario);
        if (baselineOpt.isEmpty()) {
            log.warn("Baseline nao encontrado para usuario: {}. Retornando 1.0 (sem referencia)", usuarioId);
            return 1.0;
        }

        PadraoDigitacao baseline = baselineOpt.get();

        List<Long> holdTimes = calcularHoldTimes(eventos);
        List<Long> flightTimes = calcularFlightTimes(eventos);

        double mediaHold = calcularMedia(holdTimes);
        double mediaFlight = calcularMedia(flightTimes);
        double desvioHold = calcularDesvioPadrao(holdTimes, mediaHold);
        double desvioFlight = calcularDesvioPadrao(flightTimes, mediaFlight);

        double similaridade = calcularSimilaridade(mediaHold, mediaFlight, desvioHold, desvioFlight, baseline);
        log.info("Similaridade com baseline para usuario {}: {}", usuarioId, similaridade);
        return similaridade;
    }

    /**
     * Obtem o perfil de digitacao do usuario, incluindo baseline, amostras recentes e estatisticas.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterPerfil(Long usuarioId) {
        log.info("Obtendo perfil de digitacao para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado: " + usuarioId));

        Optional<PadraoDigitacao> baseline = repositorioPadraoDigitacao.findByUsuarioAndEhBaselineTrue(usuario);
        List<PadraoDigitacao> amostrasRecentes = repositorioPadraoDigitacao.findByUsuarioOrderByCriadoEmDesc(usuario);
        long totalAmostras = repositorioPadraoDigitacao.countByUsuario(usuario);

        // Calcular estatisticas gerais
        DoubleSummaryStatistics statsHold = amostrasRecentes.stream()
                .filter(p -> p.getTempoMedioHold() != null)
                .mapToDouble(PadraoDigitacao::getTempoMedioHold)
                .summaryStatistics();

        DoubleSummaryStatistics statsFlight = amostrasRecentes.stream()
                .filter(p -> p.getTempoMedioFlight() != null)
                .mapToDouble(PadraoDigitacao::getTempoMedioFlight)
                .summaryStatistics();

        DoubleSummaryStatistics statsSimilaridade = amostrasRecentes.stream()
                .filter(p -> p.getScoreSimilaridade() != null)
                .mapToDouble(PadraoDigitacao::getScoreSimilaridade)
                .summaryStatistics();

        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("usuarioId", usuarioId);
        perfil.put("totalAmostras", totalAmostras);
        perfil.put("possuiBaseline", baseline.isPresent());
        perfil.put("baseline", baseline.orElse(null));
        perfil.put("amostrasRecentes", amostrasRecentes.stream().limit(10).toList());

        Map<String, Object> estatisticas = new LinkedHashMap<>();
        estatisticas.put("mediaHoldGeral", statsHold.getCount() > 0 ? statsHold.getAverage() : null);
        estatisticas.put("mediaFlightGeral", statsFlight.getCount() > 0 ? statsFlight.getAverage() : null);
        estatisticas.put("mediaSimilaridade", statsSimilaridade.getCount() > 0 ? statsSimilaridade.getAverage() : null);
        estatisticas.put("minSimilaridade", statsSimilaridade.getCount() > 0 ? statsSimilaridade.getMin() : null);
        estatisticas.put("maxSimilaridade", statsSimilaridade.getCount() > 0 ? statsSimilaridade.getMax() : null);
        perfil.put("estatisticas", estatisticas);

        return perfil;
    }

    /**
     * Atualiza o baseline do usuario agregando as ultimas 10 amostras.
     * Remove o baseline anterior e cria um novo com medias agregadas.
     */
    @Transactional
    public PadraoDigitacao atualizarBaseline(Long usuarioId) {
        log.info("Atualizando baseline de digitacao para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado: " + usuarioId));

        List<PadraoDigitacao> amostras = repositorioPadraoDigitacao.findByUsuarioOrderByCriadoEmDesc(usuario);

        if (amostras.isEmpty()) {
            throw new IllegalStateException("Nenhuma amostra encontrada para o usuario: " + usuarioId);
        }

        // Pegar as ultimas N amostras
        List<PadraoDigitacao> ultimasAmostras = amostras.stream()
                .limit(AMOSTRAS_PARA_BASELINE)
                .toList();

        double mediaHold = ultimasAmostras.stream()
                .filter(p -> p.getTempoMedioHold() != null)
                .mapToDouble(PadraoDigitacao::getTempoMedioHold)
                .average()
                .orElse(0.0);

        double mediaFlight = ultimasAmostras.stream()
                .filter(p -> p.getTempoMedioFlight() != null)
                .mapToDouble(PadraoDigitacao::getTempoMedioFlight)
                .average()
                .orElse(0.0);

        double desvioHold = ultimasAmostras.stream()
                .filter(p -> p.getDesvioPadraoHold() != null)
                .mapToDouble(PadraoDigitacao::getDesvioPadraoHold)
                .average()
                .orElse(0.0);

        double desvioFlight = ultimasAmostras.stream()
                .filter(p -> p.getDesvioPadraoFlight() != null)
                .mapToDouble(PadraoDigitacao::getDesvioPadraoFlight)
                .average()
                .orElse(0.0);

        // Remover baseline anterior
        Optional<PadraoDigitacao> baselineAnterior = repositorioPadraoDigitacao.findByUsuarioAndEhBaselineTrue(usuario);
        baselineAnterior.ifPresent(b -> {
            b.setEhBaseline(false);
            repositorioPadraoDigitacao.save(b);
        });

        // Criar novo baseline agregado
        PadraoDigitacao novoBaseline = PadraoDigitacao.builder()
                .usuario(usuario)
                .tempoMedioHold(mediaHold)
                .tempoMedioFlight(mediaFlight)
                .desvioPadraoHold(desvioHold)
                .desvioPadraoFlight(desvioFlight)
                .totalAmostras(ultimasAmostras.size())
                .ehBaseline(true)
                .scoreSimilaridade(1.0)
                .build();

        PadraoDigitacao salvo = repositorioPadraoDigitacao.save(novoBaseline);
        log.info("Novo baseline criado com ID: {} (baseado em {} amostras)", salvo.getId(), ultimasAmostras.size());
        return salvo;
    }

    // ==================== Metodos auxiliares ====================

    private List<Long> calcularHoldTimes(List<EventoTecla> eventos) {
        List<Long> holdTimes = new ArrayList<>();
        for (EventoTecla evento : eventos) {
            long hold = evento.keyUp() - evento.keyDown();
            if (hold > 0) {
                holdTimes.add(hold);
            }
        }
        return holdTimes;
    }

    private List<Long> calcularFlightTimes(List<EventoTecla> eventos) {
        List<Long> flightTimes = new ArrayList<>();
        for (int i = 0; i < eventos.size() - 1; i++) {
            long flight = eventos.get(i + 1).keyDown() - eventos.get(i).keyUp();
            if (flight > 0) {
                flightTimes.add(flight);
            }
        }
        return flightTimes;
    }

    private double calcularMedia(List<Long> valores) {
        if (valores.isEmpty()) {
            return 0.0;
        }
        return valores.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calcularDesvioPadrao(List<Long> valores, double media) {
        if (valores.size() < 2) {
            return 0.0;
        }
        double somaQuadrados = valores.stream()
                .mapToDouble(v -> Math.pow(v - media, 2))
                .sum();
        return Math.sqrt(somaQuadrados / (valores.size() - 1));
    }

    /**
     * Calcula similaridade usando distancia euclidiana normalizada.
     * Similaridade = 1.0 - distancia_euclidiana_normalizada(atual, baseline)
     * Resultado clamped entre 0.0 e 1.0.
     */
    private double calcularSimilaridade(double mediaHold, double mediaFlight,
                                         double desvioHold, double desvioFlight,
                                         PadraoDigitacao baseline) {
        double diffHold = mediaHold - baseline.getTempoMedioHold();
        double diffFlight = mediaFlight - baseline.getTempoMedioFlight();
        double diffDesvioHold = desvioHold - baseline.getDesvioPadraoHold();
        double diffDesvioFlight = desvioFlight - baseline.getDesvioPadraoFlight();

        // Normalizar cada dimensao pelo valor do baseline (evitar divisao por zero)
        double normHold = baseline.getTempoMedioHold() > 0 ? diffHold / baseline.getTempoMedioHold() : diffHold;
        double normFlight = baseline.getTempoMedioFlight() > 0 ? diffFlight / baseline.getTempoMedioFlight() : diffFlight;
        double normDesvioHold = baseline.getDesvioPadraoHold() > 0 ? diffDesvioHold / baseline.getDesvioPadraoHold() : diffDesvioHold;
        double normDesvioFlight = baseline.getDesvioPadraoFlight() > 0 ? diffDesvioFlight / baseline.getDesvioPadraoFlight() : diffDesvioFlight;

        double distancia = Math.sqrt(
                normHold * normHold +
                normFlight * normFlight +
                normDesvioHold * normDesvioHold +
                normDesvioFlight * normDesvioFlight
        );

        // Normalizar distancia para o intervalo [0, 1] usando fator de escala
        double distanciaNormalizada = distancia / 2.0;

        double similaridade = 1.0 - distanciaNormalizada;
        return Math.max(0.0, Math.min(1.0, similaridade));
    }

    private String serializarTempos(List<Long> holdTimes, List<Long> flightTimes) {
        try {
            Map<String, Object> tempos = new LinkedHashMap<>();
            tempos.put("holdTimes", holdTimes);
            tempos.put("flightTimes", flightTimes);
            return objectMapper.writeValueAsString(tempos);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar tempos de digitacao", e);
            return "{}";
        }
    }
}
