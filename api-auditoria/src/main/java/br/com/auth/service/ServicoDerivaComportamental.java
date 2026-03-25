package br.com.auth.service;

import br.com.auth.dominio.entidades.DerivaComportamental;
import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioDerivaComportamental;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoDerivaComportamental {

    private final RepositorioDerivaComportamental repositorioDeriva;
    private final RepositorioPerfilComportamentalIA repositorioPerfil;
    private final RepositorioUsuario repositorioUsuario;

    private static final String CLASSIFICACAO_ESTAVEL = "ESTAVEL";
    private static final String CLASSIFICACAO_EVOLUCAO = "EVOLUCAO_NATURAL";
    private static final String CLASSIFICACAO_RUPTURA = "RUPTURA_COMPORTAMENTAL";

    @Transactional
    public DerivaResponse analisarDeriva(Long usuarioId) {
        log.info("Analisando deriva comportamental para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + usuarioId));

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime seteDiasAtras = agora.minusDays(7);
        LocalDateTime trintaDiasAtras = agora.minusDays(30);

        List<PerfilComportamentalIA> perfis7d = repositorioPerfil.findByUsuarioAndDataMaiorQue(usuario, seteDiasAtras);
        List<PerfilComportamentalIA> perfis30d = repositorioPerfil.findByUsuarioAndDataMaiorQue(usuario, trintaDiasAtras);

        if (perfis30d.isEmpty()) {
            log.warn("Nenhum perfil encontrado nos ultimos 30 dias para usuario: {}", usuarioId);
            return criarRespostaSemDados(usuarioId);
        }

        // Calcular drift para cada fator
        Map<String, DriftFator> drifts = new LinkedHashMap<>();
        drifts.put("padraoHorario", calcularDrift(perfis7d, perfis30d, PerfilComportamentalIA::getPadraoHorarioScore));
        drifts.put("padraoLocalizacao", calcularDrift(perfis7d, perfis30d, PerfilComportamentalIA::getPadraoLocalizacaoScore));
        drifts.put("padraoDispositivo", calcularDrift(perfis7d, perfis30d, PerfilComportamentalIA::getPadraoDispositivoScore));
        drifts.put("frequenciaAcesso", calcularDrift(perfis7d, perfis30d, PerfilComportamentalIA::getFrequenciaAcessoScore));
        drifts.put("sequenciaNavegacao", calcularDrift(perfis7d, perfis30d, PerfilComportamentalIA::getSequenciaNavegacaoScore));

        // Score agregado de deriva
        double scoreDeriva = drifts.values().stream()
                .mapToDouble(DriftFator::drift)
                .average()
                .orElse(0.0);

        // Medias das janelas
        double media7d = perfis7d.stream()
                .filter(p -> p.getEnsembleScore() != null)
                .mapToDouble(PerfilComportamentalIA::getEnsembleScore)
                .average().orElse(0.0);

        double media30d = perfis30d.stream()
                .filter(p -> p.getEnsembleScore() != null)
                .mapToDouble(PerfilComportamentalIA::getEnsembleScore)
                .average().orElse(0.0);

        // Classificar drift
        String classificacao = classificarDrift(scoreDeriva);

        // Fatores alterados
        List<String> fatoresAlterados = drifts.entrySet().stream()
                .filter(e -> e.getValue().drift() > 0.3)
                .map(Map.Entry::getKey)
                .toList();

        // Persistir entidade
        DerivaComportamental entidade = DerivaComportamental.builder()
                .usuario(usuario)
                .dataAnalise(agora)
                .scoreDeriva(scoreDeriva)
                .janelaCurta7d(media7d)
                .janelaLonga30d(media30d)
                .classificacaoDrift(classificacao)
                .fatoresAlterados(String.join(",", fatoresAlterados))
                .detalhesJson(gerarDetalhesJson(drifts))
                .baselineAjustado(false)
                .build();

        repositorioDeriva.save(entidade);

        log.info("Deriva comportamental analisada para usuario: {} - classificacao: {}, score: {}",
                usuarioId, classificacao, scoreDeriva);

        // Construir resposta
        List<FatorDerivaDTO> fatoresDTO = drifts.entrySet().stream()
                .map(e -> new FatorDerivaDTO(e.getKey(), e.getValue().media7d(), e.getValue().media30d(),
                        e.getValue().stddev30d(), e.getValue().drift()))
                .toList();

        return new DerivaResponse(
                entidade.getId(),
                usuarioId,
                scoreDeriva,
                media7d,
                media30d,
                classificacao,
                fatoresAlterados,
                fatoresDTO,
                agora
        );
    }

    public List<DerivaHistoricoDTO> obterHistorico(Long usuarioId, int limite) {
        log.info("Obtendo historico de deriva para usuario: {} (limite: {})", usuarioId, limite);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + usuarioId));

        List<DerivaComportamental> registros = repositorioDeriva.findByUsuarioOrderByCriadoEmDesc(usuario);

        return registros.stream()
                .limit(limite)
                .map(d -> new DerivaHistoricoDTO(
                        d.getId(),
                        d.getDataAnalise(),
                        d.getScoreDeriva(),
                        d.getJanelaCurta7d(),
                        d.getJanelaLonga30d(),
                        d.getClassificacaoDrift(),
                        d.getFatoresAlterados(),
                        d.getBaselineAjustado(),
                        d.getCriadoEm()
                ))
                .toList();
    }

    @Transactional
    public AjusteBaselineDTO ajustarBaseline(Long usuarioId) {
        log.info("Ajustando baseline para usuario: {}", usuarioId);

        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + usuarioId));

        Optional<DerivaComportamental> ultimaDeriva = repositorioDeriva.findTopByUsuarioOrderByCriadoEmDesc(usuario);

        if (ultimaDeriva.isEmpty()) {
            return new AjusteBaselineDTO(usuarioId, false, "Nenhum registro de deriva encontrado", LocalDateTime.now());
        }

        DerivaComportamental deriva = ultimaDeriva.get();
        deriva.setBaselineAjustado(true);
        repositorioDeriva.save(deriva);

        log.info("Baseline ajustado com sucesso para usuario: {}", usuarioId);

        return new AjusteBaselineDTO(usuarioId, true, "Baseline ajustado com sucesso", LocalDateTime.now());
    }

    private DriftFator calcularDrift(List<PerfilComportamentalIA> perfis7d,
                                     List<PerfilComportamentalIA> perfis30d,
                                     Function<PerfilComportamentalIA, Double> extrator) {

        List<Double> valores7d = perfis7d.stream()
                .map(extrator)
                .filter(Objects::nonNull)
                .toList();

        List<Double> valores30d = perfis30d.stream()
                .map(extrator)
                .filter(Objects::nonNull)
                .toList();

        double media7d = valores7d.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double media30d = valores30d.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double stddev30d = calcularDesvioPadrao(valores30d, media30d);

        double drift = Math.abs(media7d - media30d) / Math.max(stddev30d, 0.1);

        return new DriftFator(media7d, media30d, stddev30d, drift);
    }

    private double calcularDesvioPadrao(List<Double> valores, double media) {
        if (valores.size() < 2) return 0.0;

        double somaQuadrados = valores.stream()
                .mapToDouble(v -> Math.pow(v - media, 2))
                .sum();

        return Math.sqrt(somaQuadrados / valores.size());
    }

    private String classificarDrift(double scoreDeriva) {
        if (scoreDeriva < 0.3) return CLASSIFICACAO_ESTAVEL;
        if (scoreDeriva <= 0.7) return CLASSIFICACAO_EVOLUCAO;
        return CLASSIFICACAO_RUPTURA;
    }

    private String gerarDetalhesJson(Map<String, DriftFator> drifts) {
        StringBuilder sb = new StringBuilder("{");
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, DriftFator> entry : drifts.entrySet()) {
            DriftFator df = entry.getValue();
            entries.add(String.format("\"%s\":{\"media7d\":%.4f,\"media30d\":%.4f,\"stddev30d\":%.4f,\"drift\":%.4f}",
                    entry.getKey(), df.media7d(), df.media30d(), df.stddev30d(), df.drift()));
        }
        sb.append(String.join(",", entries));
        sb.append("}");
        return sb.toString();
    }

    private DerivaResponse criarRespostaSemDados(Long usuarioId) {
        return new DerivaResponse(
                null, usuarioId, 0.0, 0.0, 0.0,
                CLASSIFICACAO_ESTAVEL, List.of(), List.of(), LocalDateTime.now()
        );
    }

    // Inner records

    private record DriftFator(double media7d, double media30d, double stddev30d, double drift) {}

    public record DerivaResponse(
            Long id,
            Long usuarioId,
            Double scoreDeriva,
            Double janelaCurta7d,
            Double janelaLonga30d,
            String classificacaoDrift,
            List<String> fatoresAlterados,
            List<FatorDerivaDTO> fatores,
            LocalDateTime dataAnalise
    ) {}

    public record FatorDerivaDTO(
            String nome,
            Double media7d,
            Double media30d,
            Double stddev30d,
            Double drift
    ) {}

    public record DerivaHistoricoDTO(
            Long id,
            LocalDateTime dataAnalise,
            Double scoreDeriva,
            Double janelaCurta7d,
            Double janelaLonga30d,
            String classificacaoDrift,
            String fatoresAlterados,
            Boolean baselineAjustado,
            LocalDateTime criadoEm
    ) {}

    public record AjusteBaselineDTO(
            Long usuarioId,
            Boolean ajustado,
            String mensagem,
            LocalDateTime timestamp
    ) {}
}
