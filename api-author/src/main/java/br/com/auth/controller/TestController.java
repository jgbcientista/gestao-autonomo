package br.com.auth.controller;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.ServicoAnaliseComportamentalIA;
import br.com.auth.service.ServicoIsolationForest;
import br.com.auth.service.ServicoRandomForest;
import br.com.auth.service.ServicoDeepLearning;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final ServicoAnaliseComportamentalIA servicoIA;
    private final ServicoIsolationForest servicoIsolationForest;
    private final ServicoRandomForest servicoRandomForest;
    private final ServicoDeepLearning servicoDeepLearning;
    private final RepositorioUsuario repositorioUsuario;

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK - Aplicação funcionando!");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Status: ATIVO");
    }

    @PostMapping("/ia/treinar")
    public ResponseEntity<Map<String, Object>> treinarModelosIA() {
        log.info("Iniciando treinamento dos modelos de IA via endpoint de teste");
        
        try {
            // Treinar cada modelo individualmente
            servicoIsolationForest.treinarModelo();
            servicoRandomForest.treinarModelo();
            servicoDeepLearning.treinarModelo();
            
            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "mensagem", "Todos os modelos de IA foram treinados com sucesso",
                "modelos", Map.of(
                    "isolation_forest", "treinado",
                    "random_forest", "treinado", 
                    "deep_learning", "treinado"
                ),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Erro no treinamento dos modelos de IA", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "erro",
                "mensagem", "Erro no treinamento: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    @PostMapping("/ia/testar-score")
    public ResponseEntity<Map<String, Object>> testarScoreIA() {
        log.info("Testando cálculo de scores dos algoritmos de IA");
        
        try {
            // Criar dados de teste simulados
            IServicoAnaliseComportamentalIA.DadosFeatures featuresNormais = 
                new IServicoAnaliseComportamentalIA.DadosFeatures(
                    9.0,    // horaAcesso - horário comercial
                    2.0,    // diaSemana - terça-feira
                    5.0,    // frequenciaAcessoSemanal - normal
                    true,   // ipJaUtilizado
                    true,   // dispositivoJaUtilizado
                    true,   // localizacaoJaUtilizada
                    5.0,    // distanciaLocalizacaoHabitualKm - pequena
                    1.0,    // diferencaHorarioHabitualHoras - pequena
                    2.0,    // tempoDesdeUltimoAcessoHoras - recente
                    3.0,    // mediaSessoesDiarias - normal
                    1.5,    // desvioPadraoHorarios - baixo
                    2,      // totalIpsDistintos - poucos
                    1,      // totalDispositivosDistintos - poucos
                    0.6     // padroesNavegacaoScore - consistente
                );

            IServicoAnaliseComportamentalIA.DadosFeatures featuresAnomalas = 
                new IServicoAnaliseComportamentalIA.DadosFeatures(
                    3.0,    // horaAcesso - madrugada
                    7.0,    // diaSemana - domingo
                    0.5,    // frequenciaAcessoSemanal - muito baixa
                    false,  // ipJaUtilizado - novo IP
                    false,  // dispositivoJaUtilizado - novo dispositivo
                    false,  // localizacaoJaUtilizada - nova localização
                    500.0,  // distanciaLocalizacaoHabitualKm - muito longe
                    8.0,    // diferencaHorarioHabitualHoras - muito diferente
                    120.0,  // tempoDesdeUltimoAcessoHoras - muito tempo
                    0.5,    // mediaSessoesDiarias - muito baixa
                    8.0,    // desvioPadraoHorarios - muito alto
                    10,     // totalIpsDistintos - muitos
                    5,      // totalDispositivosDistintos - muitos
                    0.1     // padroesNavegacaoScore - inconsistente
                );

            // Calcular scores para comportamento normal
            Double scoreIFNormal = servicoIsolationForest.calcularScore(featuresNormais);
            Double scoreRFNormal = servicoRandomForest.calcularScore(featuresNormais);
            Double scoreDLNormal = servicoDeepLearning.calcularScore(featuresNormais);

            // Calcular scores para comportamento anômalo
            Double scoreIFAnomalo = servicoIsolationForest.calcularScore(featuresAnomalas);
            Double scoreRFAnomalo = servicoRandomForest.calcularScore(featuresAnomalas);
            Double scoreDLAnomalo = servicoDeepLearning.calcularScore(featuresAnomalas);

            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "comportamento_normal", Map.of(
                    "isolation_forest", scoreIFNormal,
                    "random_forest", scoreRFNormal,
                    "deep_learning", scoreDLNormal,
                    "ensemble", (scoreIFNormal * 0.4) + (scoreRFNormal * 0.3) + (scoreDLNormal * 0.3)
                ),
                "comportamento_anomalo", Map.of(
                    "isolation_forest", scoreIFAnomalo,
                    "random_forest", scoreRFAnomalo,
                    "deep_learning", scoreDLAnomalo,
                    "ensemble", (scoreIFAnomalo * 0.4) + (scoreRFAnomalo * 0.3) + (scoreDLAnomalo * 0.3)
                ),
                "interpretacao", Map.of(
                    "normal_esperado", "Scores baixos (< 0.5) indicam comportamento normal",
                    "anomalo_esperado", "Scores altos (> 0.5) indicam comportamento anômalo",
                    "threshold_anomalia", 0.7,
                    "threshold_suspeito", 0.5
                ),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Erro no teste de scores de IA", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "erro",
                "mensagem", "Erro no teste: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    @PostMapping("/ia/simular-analise/{usuarioId}")
    public ResponseEntity<Map<String, Object>> simularAnaliseComportamental(@PathVariable Long usuarioId) {
        log.info("Simulando análise comportamental para usuário: {}", usuarioId);
        
        try {
            Optional<Usuario> usuarioOpt = repositorioUsuario.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // Simular dados de contexto de acesso suspeito
            IServicoAnaliseComportamentalIA.DadosContextoAcesso dadosContexto = 
                new IServicoAnaliseComportamentalIA.DadosContextoAcesso(
                    "192.168.1.100",  // IP diferente
                    "Mozilla/5.0 (Unknown Device)",  // User agent suspeito
                    "São Paulo, Brasil",  // Localização
                    "America/Sao_Paulo",  // Timezone
                    "pt-BR",  // Idioma
                    "1920x1080",  // Resolução
                    1  // Tentativas de login
                );

            PerfilComportamentalIA perfil = servicoIA.analisarComportamento(usuario, dadosContexto);
            
            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "nome", usuario.getName()
                ),
                "analise", Map.of(
                    "score_anomalia", perfil.getScoreAnomalia(),
                    "classificacao", perfil.getClassificacaoAcesso().toString(),
                    "algoritmo", perfil.getAlgoritmoUtilizado().toString(),
                    "confianca", perfil.getConfiancaPredicao()
                ),
                "scores_individuais", Map.of(
                    "isolation_forest", perfil.getIsolationForestScore(),
                    "random_forest", perfil.getRandomForestScore(),
                    "deep_learning", perfil.getDeepLearningScore(),
                    "ensemble", perfil.getEnsembleScore()
                ),
                "contexto", Map.of(
                    "ip", dadosContexto.enderecoIp(),
                    "user_agent", dadosContexto.userAgent(),
                    "localizacao", dadosContexto.localizacaoGeografica()
                ),
                "tempo_processamento_ms", perfil.getTempoProcessamentoMs(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Erro na simulação de análise comportamental", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "erro",
                "mensagem", "Erro na simulação: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    @GetMapping("/ia/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticasIA() {
        try {
            IServicoAnaliseComportamentalIA.EstatisticasAnomalias estatisticas = 
                servicoIA.obterEstatisticasAnomalias();
            
            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "estatisticas", Map.of(
                    "total_analises", estatisticas.totalAnalises(),
                    "total_anomalias", estatisticas.totalAnomalias(),
                    "percentual_anomalias", estatisticas.percentualAnomalias(),
                    "score_media", estatisticas.scoreAnomaliaMedia(),
                    "acuracia_algoritmos", estatisticas.acuraciaAlgoritmos()
                ),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas de IA", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "erro",
                "mensagem", "Erro ao obter estatísticas: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug(HttpServletRequest request) {
        System.out.println("🔍 DEBUG ENDPOINT CHAMADO!");
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("method", request.getMethod());
        debug.put("requestURI", request.getRequestURI());
        debug.put("contextPath", request.getContextPath());
        debug.put("servletPath", request.getServletPath());
        debug.put("pathInfo", request.getPathInfo());
        debug.put("queryString", request.getQueryString());
        debug.put("remoteAddr", request.getRemoteAddr());
        debug.put("remoteHost", request.getRemoteHost());
        debug.put("serverName", request.getServerName());
        debug.put("serverPort", request.getServerPort());
        debug.put("scheme", request.getScheme());
        debug.put("protocol", request.getProtocol());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> 
            headers.put(name, request.getHeader(name))
        );
        debug.put("headers", headers);
        
        System.out.println("🎯 DEBUG INFO: " + debug);
        
        return ResponseEntity.ok(debug);
    }

    @GetMapping("/simple")
    public ResponseEntity<String> simple() {
        System.out.println("🔍 SIMPLE ENDPOINT CHAMADO!");
        return ResponseEntity.ok("SUCCESS - ENDPOINT FUNCIONANDO!");
    }

    @GetMapping("/health-simple")
    public ResponseEntity<String> healthSimple() {
        System.out.println("🔍 HEALTH SIMPLE ENDPOINT CHAMADO!");
        return ResponseEntity.ok("OK");
    }
} 