package br.com.auth.controller;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.ServicoAnaliseComportamentalIA;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Análise Comportamental IA", description = "APIs para análise comportamental usando Inteligência Artificial")
public class AnaliseComportamentalIAController {

    private final ServicoAnaliseComportamentalIA servicoIA;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPerfilComportamentalIA repositorioPerfil;

    @PostMapping("/analisar/{usuarioId}")
    @Operation(summary = "Analisar comportamento do usuário",
               description = "Executa análise comportamental completa usando algoritmos de IA")
    @Transactional
    public ResponseEntity<PerfilComportamentalIA> analisarComportamento(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId,
            @RequestBody DadosContextoRequest contexto,
            HttpServletRequest request) {
        
        log.info("Iniciando análise comportamental para usuário: {}", usuarioId);
        
        try {
            Optional<Usuario> usuarioOpt = repositorioUsuario.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // Extrair dados de contexto da requisição
            IServicoAnaliseComportamentalIA.DadosContextoAcesso dadosContexto = 
                extrairDadosContexto(contexto, request);

            PerfilComportamentalIA perfil = servicoIA.analisarComportamento(usuario, dadosContexto);
            
            return ResponseEntity.ok(perfil);
            
        } catch (Exception e) {
            log.error("Erro na análise comportamental para usuário: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/classificar/{usuarioId}")
    @Operation(summary = "Classificar acesso", 
               description = "Classifica se o acesso é esperado ou anômalo")
    public ResponseEntity<ClassificacaoResponse> classificarAcesso(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId,
            @RequestBody DadosContextoRequest contexto,
            HttpServletRequest request) {
        
        try {
            Optional<Usuario> usuarioOpt = repositorioUsuario.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            IServicoAnaliseComportamentalIA.DadosContextoAcesso dadosContexto = 
                extrairDadosContexto(contexto, request);

            PerfilComportamentalIA.ClassificacaoAcesso classificacao = 
                servicoIA.classificarAcesso(usuario, dadosContexto);
            
            ClassificacaoResponse response = new ClassificacaoResponse(
                classificacao,
                obterDescricaoClassificacao(classificacao),
                obterNivelRisco(classificacao)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro na classificação de acesso para usuário: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/treinar-modelos")
    @Operation(summary = "Treinar modelos de IA", 
               description = "Executa treinamento dos modelos de machine learning")
    public ResponseEntity<Map<String, String>> treinarModelos() {
        
        log.info("Iniciando treinamento dos modelos de IA");
        
        try {
            servicoIA.treinarModelos();
            
            Map<String, String> response = Map.of(
                "status", "sucesso",
                "mensagem", "Modelos treinados com sucesso",
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no treinamento dos modelos", e);
            
            Map<String, String> response = Map.of(
                "status", "erro",
                "mensagem", "Erro no treinamento: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/feedback/{perfilId}")
    @Operation(summary = "Enviar feedback", 
               description = "Fornece feedback sobre a classificação para melhorar os modelos")
    public ResponseEntity<Map<String, String>> enviarFeedback(
            @Parameter(description = "ID do perfil comportamental") @PathVariable Long perfilId,
            @RequestBody FeedbackRequest feedback) {
        
        try {
            servicoIA.atualizarModeloComFeedback(perfilId, feedback.acessoLegitimo);
            
            Map<String, String> response = Map.of(
                "status", "sucesso",
                "mensagem", "Feedback registrado com sucesso"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao processar feedback para perfil: {}", perfilId, e);
            
            Map<String, String> response = Map.of(
                "status", "erro",
                "mensagem", "Erro ao processar feedback: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/historico/{usuarioId}")
    @Operation(summary = "Obter histórico de análises",
               description = "Retorna histórico de análises comportamentais do usuário")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PerfilComportamentalIA>> obterHistorico(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId,
            @Parameter(description = "Limite de registros") @RequestParam(defaultValue = "10") int limite) {
        
        try {
            Optional<Usuario> usuarioOpt = repositorioUsuario.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            List<PerfilComportamentalIA> historico = servicoIA.obterHistoricoAnalises(usuario, limite);
            
            return ResponseEntity.ok(historico);
            
        } catch (Exception e) {
            log.error("Erro ao obter histórico para usuário: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas de anomalias", 
               description = "Retorna estatísticas gerais sobre detecção de anomalias")
    public ResponseEntity<IServicoAnaliseComportamentalIA.EstatisticasAnomalias> obterEstatisticas() {
        
        try {
            IServicoAnaliseComportamentalIA.EstatisticasAnomalias estatisticas = 
                servicoIA.obterEstatisticasAnomalias();
            
            return ResponseEntity.ok(estatisticas);
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/calcular-score")
    @Operation(summary = "Calcular score de anomalia", 
               description = "Calcula score de anomalia usando algoritmos específicos")
    public ResponseEntity<ScoreResponse> calcularScore(
            @RequestBody CalcularScoreRequest request) {
        
        try {
            IServicoAnaliseComportamentalIA.DadosFeatures features = criarDadosFeatures(request);
            
            Double scoreIF = servicoIA.calcularScoreIsolationForest(features);
            Double scoreRF = servicoIA.calcularScoreRandomForest(features);
            Double scoreDL = servicoIA.calcularScoreDeepLearning(features);
            Double scoreEnsemble = servicoIA.calcularScoreEnsemble(features);
            
            ScoreResponse response = new ScoreResponse(
                scoreIF, scoreRF, scoreDL, scoreEnsemble,
                Map.of(
                    "isolation_forest", scoreIF,
                    "random_forest", scoreRF,
                    "deep_learning", scoreDL,
                    "ensemble", scoreEnsemble
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao calcular scores", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Métodos auxiliares
    
    private IServicoAnaliseComportamentalIA.DadosContextoAcesso extrairDadosContexto(
            DadosContextoRequest contexto, HttpServletRequest request) {
        
        String enderecoIp = (contexto.enderecoIp != null && !contexto.enderecoIp.isBlank())
                           ? contexto.enderecoIp : obterEnderecoIpReal(request);
        String userAgent = (contexto.userAgent != null && !contexto.userAgent.isBlank())
                          ? contexto.userAgent : request.getHeader("User-Agent");
        
        return new IServicoAnaliseComportamentalIA.DadosContextoAcesso(
            enderecoIp,
            userAgent,
            contexto.localizacaoGeografica,
            contexto.timezone,
            contexto.idiomaBrowser,
            contexto.resolucaoTela,
            contexto.tentativasLogin
        );
    }

    private String obterEnderecoIpReal(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String obterDescricaoClassificacao(PerfilComportamentalIA.ClassificacaoAcesso classificacao) {
        return switch (classificacao) {
            case ESPERADO -> "Acesso dentro dos padrões normais do usuário";
            case SUSPEITO -> "Acesso com algumas características incomuns";
            case ANOMALO -> "Acesso com padrão significativamente diferente";
            case ALTAMENTE_SUSPEITO -> "Acesso com alto risco de ser fraudulento";
        };
    }

    private String obterNivelRisco(PerfilComportamentalIA.ClassificacaoAcesso classificacao) {
        return switch (classificacao) {
            case ESPERADO -> "BAIXO";
            case SUSPEITO -> "MEDIO";
            case ANOMALO -> "ALTO";
            case ALTAMENTE_SUSPEITO -> "CRITICO";
        };
    }

    private IServicoAnaliseComportamentalIA.DadosFeatures criarDadosFeatures(CalcularScoreRequest request) {
        return new IServicoAnaliseComportamentalIA.DadosFeatures(
            request.horaAcesso,
            request.diaSemana,
            request.frequenciaAcessoSemanal,
            request.ipJaUtilizado,
            request.dispositivoJaUtilizado,
            request.localizacaoJaUtilizada,
            request.distanciaLocalizacaoHabitualKm,
            request.diferencaHorarioHabitualHoras,
            request.tempoDesdeUltimoAcessoHoras,
            request.mediaSessoesDiarias,
            request.desvioPadraoHorarios,
            request.totalIpsDistintos,
            request.totalDispositivosDistintos,
            request.padroesNavegacaoScore
        );
    }

    // DTOs

    public record DadosContextoRequest(
        String enderecoIp,
        String userAgent,
        String localizacaoGeografica,
        String timezone,
        String idiomaBrowser,
        String resolucaoTela,
        Integer tentativasLogin
    ) {}

    public record ClassificacaoResponse(
        PerfilComportamentalIA.ClassificacaoAcesso classificacao,
        String descricao,
        String nivelRisco
    ) {}

    public record FeedbackRequest(
        boolean acessoLegitimo,
        String comentario
    ) {}

    public record ScoreResponse(
        Double isolationForest,
        Double randomForest,
        Double deepLearning,
        Double ensemble,
        Map<String, Double> detalhes
    ) {}

    public record CalcularScoreRequest(
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

    // --- Novos endpoints de comparação e métricas ---

    @GetMapping("/comparacao-modelos/{usuarioId}")
    @Operation(summary = "Comparação entre modelos de IA",
               description = "Retorna dados de comparação entre os modelos para todas as análises de um usuário")
    @Transactional(readOnly = true)
    public ResponseEntity<?> compararModelos(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId) {

        log.info("Obtendo comparação de modelos para usuário: {}", usuarioId);

        try {
            Optional<Usuario> usuarioOpt = repositorioUsuario.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            List<PerfilComportamentalIA> perfis = repositorioPerfil.findByUsuarioOrderByCriadoEmDesc(usuario);

            List<Map<String, Object>> analises = new ArrayList<>();
            int totalDesacordos = 0;

            for (PerfilComportamentalIA perfil : perfis) {
                Map<String, Object> analise = new HashMap<>();
                analise.put("id", perfil.getId());
                analise.put("timestamp", perfil.getCriadoEm());
                analise.put("isolationForestScore", perfil.getIsolationForestScore());
                analise.put("randomForestScore", perfil.getRandomForestScore());
                analise.put("deepLearningScore", perfil.getDeepLearningScore());
                analise.put("ensembleScore", perfil.getEnsembleScore());
                analise.put("classificacaoAcesso", perfil.getClassificacaoAcesso());

                // Classificar cada modelo individualmente
                String classifIF = classificarPorScore(perfil.getIsolationForestScore());
                String classifRF = classificarPorScore(perfil.getRandomForestScore());
                String classifDL = classificarPorScore(perfil.getDeepLearningScore());

                analise.put("classificacaoIsolationForest", classifIF);
                analise.put("classificacaoRandomForest", classifRF);
                analise.put("classificacaoDeepLearning", classifDL);

                // Verificar se há desacordo entre os modelos
                boolean desacordo = !(classifIF.equals(classifRF) && classifRF.equals(classifDL));
                analise.put("desacordo", desacordo);

                if (desacordo) {
                    totalDesacordos++;
                }

                analises.add(analise);
            }

            int total = perfis.size();
            double percentualAcordo = total > 0 ? ((double) (total - totalDesacordos) / total) * 100 : 100.0;

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("usuarioId", usuarioId);
            resultado.put("totalAnalises", total);
            resultado.put("totalDesacordos", totalDesacordos);
            resultado.put("percentualAcordo", Math.round(percentualAcordo * 100.0) / 100.0);
            resultado.put("analises", analises);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Erro ao comparar modelos para usuário: {}", usuarioId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao comparar modelos: " + e.getMessage()));
        }
    }

    @GetMapping("/metricas-modelos")
    @Operation(summary = "Métricas dos modelos de IA",
               description = "Retorna precisão, recall e F1-score para cada modelo comparado ao ensemble")
    @Transactional(readOnly = true)
    public ResponseEntity<?> obterMetricasModelos() {

        log.info("Calculando métricas dos modelos de IA");

        try {
            List<PerfilComportamentalIA> todosPerfis = repositorioPerfil.findAll();

            if (todosPerfis.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "mensagem", "Nenhum perfil encontrado para calcular métricas",
                    "totalRegistros", 0
                ));
            }

            Map<String, Object> metricasIF = calcularMetricasModelo(todosPerfis, "isolationForest");
            Map<String, Object> metricasRF = calcularMetricasModelo(todosPerfis, "randomForest");
            Map<String, Object> metricasDL = calcularMetricasModelo(todosPerfis, "deepLearning");

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("totalRegistros", todosPerfis.size());
            resultado.put("isolationForest", metricasIF);
            resultado.put("randomForest", metricasRF);
            resultado.put("deepLearning", metricasDL);
            resultado.put("calculadoEm", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Erro ao calcular métricas dos modelos", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao calcular métricas: " + e.getMessage()));
        }
    }

    private String classificarPorScore(Double score) {
        if (score == null) return "ESPERADO";
        if (score <= 0.3) return "ESPERADO";
        if (score <= 0.7) return "SUSPEITO";
        return "ANOMALO";
    }

    private Map<String, Object> calcularMetricasModelo(List<PerfilComportamentalIA> perfis, String modelo) {
        // Classificações possíveis: ESPERADO, SUSPEITO, ANOMALO
        String[] classes = {"ESPERADO", "SUSPEITO", "ANOMALO"};

        int totalCorretos = 0;
        Map<String, Integer> tp = new HashMap<>();
        Map<String, Integer> fp = new HashMap<>();
        Map<String, Integer> fn = new HashMap<>();

        for (String c : classes) {
            tp.put(c, 0);
            fp.put(c, 0);
            fn.put(c, 0);
        }

        for (PerfilComportamentalIA perfil : perfis) {
            Double score;
            switch (modelo) {
                case "isolationForest" -> score = perfil.getIsolationForestScore();
                case "randomForest" -> score = perfil.getRandomForestScore();
                case "deepLearning" -> score = perfil.getDeepLearningScore();
                default -> score = null;
            }

            String classifModelo = classificarPorScore(score);
            String classifEnsemble = classificarPorScore(perfil.getEnsembleScore());

            if (classifModelo.equals(classifEnsemble)) {
                totalCorretos++;
            }

            for (String c : classes) {
                boolean modeloPrediz = classifModelo.equals(c);
                boolean ensembleDiz = classifEnsemble.equals(c);

                if (modeloPrediz && ensembleDiz) {
                    tp.merge(c, 1, Integer::sum);
                } else if (modeloPrediz && !ensembleDiz) {
                    fp.merge(c, 1, Integer::sum);
                } else if (!modeloPrediz && ensembleDiz) {
                    fn.merge(c, 1, Integer::sum);
                }
            }
        }

        // Calcular métricas macro-average
        double precisaoTotal = 0, recallTotal = 0, f1Total = 0;
        int classesComDados = 0;

        Map<String, Map<String, Object>> metricasPorClasse = new HashMap<>();

        for (String c : classes) {
            int tpVal = tp.get(c);
            int fpVal = fp.get(c);
            int fnVal = fn.get(c);

            double precisao = (tpVal + fpVal) > 0 ? (double) tpVal / (tpVal + fpVal) : 0.0;
            double recall = (tpVal + fnVal) > 0 ? (double) tpVal / (tpVal + fnVal) : 0.0;
            double f1 = (precisao + recall) > 0 ? 2 * (precisao * recall) / (precisao + recall) : 0.0;

            Map<String, Object> metricaClasse = new HashMap<>();
            metricaClasse.put("precisao", Math.round(precisao * 10000.0) / 10000.0);
            metricaClasse.put("recall", Math.round(recall * 10000.0) / 10000.0);
            metricaClasse.put("f1Score", Math.round(f1 * 10000.0) / 10000.0);
            metricaClasse.put("truePositives", tpVal);
            metricaClasse.put("falsePositives", fpVal);
            metricaClasse.put("falseNegatives", fnVal);

            metricasPorClasse.put(c, metricaClasse);

            if (tpVal + fpVal + fnVal > 0) {
                precisaoTotal += precisao;
                recallTotal += recall;
                f1Total += f1;
                classesComDados++;
            }
        }

        double acuracia = perfis.size() > 0 ? (double) totalCorretos / perfis.size() : 0.0;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("acuracia", Math.round(acuracia * 10000.0) / 10000.0);
        resultado.put("precisaoMedia", classesComDados > 0 ? Math.round((precisaoTotal / classesComDados) * 10000.0) / 10000.0 : 0.0);
        resultado.put("recallMedio", classesComDados > 0 ? Math.round((recallTotal / classesComDados) * 10000.0) / 10000.0 : 0.0);
        resultado.put("f1ScoreMedio", classesComDados > 0 ? Math.round((f1Total / classesComDados) * 10000.0) / 10000.0 : 0.0);
        resultado.put("metricasPorClasse", metricasPorClasse);

        return resultado;
    }
} 