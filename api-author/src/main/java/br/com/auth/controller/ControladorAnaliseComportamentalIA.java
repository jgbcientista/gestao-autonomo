package br.com.auth.controller;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Análise Comportamental IA", description = "APIs para análise comportamental usando Inteligência Artificial")
public class ControladorAnaliseComportamentalIA {

    private final ServicoAnaliseComportamentalIA servicoIA;
    private final RepositorioUsuario repositorioUsuario;

    @PostMapping("/analisar/{usuarioId}")
    @Operation(summary = "Analisar comportamento do usuário", 
               description = "Executa análise comportamental completa usando algoritmos de IA")
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
        
        String enderecoIp = contexto.enderecoIp != null ? contexto.enderecoIp : 
                           obterEnderecoIpReal(request);
        String userAgent = contexto.userAgent != null ? contexto.userAgent : 
                          request.getHeader("User-Agent");
        
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
} 