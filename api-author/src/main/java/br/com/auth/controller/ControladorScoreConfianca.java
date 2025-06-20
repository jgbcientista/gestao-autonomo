package br.com.auth.controller;

import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.ServicoScoreConfianca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciar scores de confiança dos usuários
 */
@RestController
@RequestMapping("/api/trust-score")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Score de Confiança", description = "APIs para gerenciar scores de confiança dos usuários")
public class ControladorScoreConfianca {

    private final ServicoScoreConfianca servicoScoreConfianca;
    private final RepositorioScoreConfianca repositorioScoreConfianca;
    private final RepositorioUsuario repositorioUsuario;

    @GetMapping("/usuario/{email}")
    @Operation(summary = "Obtém o score de confiança de um usuário", description = "Retorna o score atual de confiança do usuário")
    public ResponseEntity<Map<String, Object>> obterScoreUsuario(
            @Parameter(description = "Email do usuário") @PathVariable String email) {
        
        try {
            Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("usuario", usuario.getEmail());
            response.put("scoreAtual", score.getScoreAtual());
            response.put("nivelConfianca", score.getNivelConfianca());
            response.put("scoreBase", score.getScoreBase());
            response.put("fatorAjuste", score.getFatorAjuste());
            response.put("emObservacao", score.getEmObservacao());
            response.put("ultimaAtualizacao", score.getUltimaAtualizacao());
            response.put("motivoAlteracao", score.getMotivoAlteracao());
            
            // Estatísticas
            Map<String, Integer> estatisticas = new HashMap<>();
            estatisticas.put("totalLoginsSucesso", score.getTotalLoginsSucesso());
            estatisticas.put("totalLoginsSuspeitos", score.getTotalLoginsSuspeitos());
            estatisticas.put("totalBloqueios", score.getTotalBloqueios());
            estatisticas.put("totalMfaExigido", score.getTotalMfaExigido());
            response.put("estatisticas", estatisticas);

            // Status de decisões
            Map<String, Boolean> decisoes = new HashMap<>();
            decisoes.put("confiavel", score.isConfiavel());
            decisoes.put("requerMfa", score.requerMfa());
            decisoes.put("deveBloquear", score.deveBloquear());
            response.put("decisoes", decisoes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao obter score do usuário: {}", email, e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao obter score: " + e.getMessage()));
        }
    }

    @PostMapping("/ajustar/{email}")
    @Operation(summary = "Ajusta manualmente o score de um usuário", description = "Permite ajuste manual do score para casos especiais")
    public ResponseEntity<Map<String, Object>> ajustarScore(
            @Parameter(description = "Email do usuário") @PathVariable String email,
            @Parameter(description = "Valor do ajuste (-1.0 a 1.0)") @RequestParam double ajuste,
            @Parameter(description = "Motivo do ajuste") @RequestParam String motivo) {
        
        try {
            Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            ScoreConfianca scoreAtualizado = servicoScoreConfianca.ajustarScore(usuario, ajuste, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("usuario", usuario.getEmail());
            response.put("scoreAnterior", scoreAtualizado.getScoreBase());
            response.put("scoreAtual", scoreAtualizado.getScoreAtual());
            response.put("ajusteAplicado", ajuste);
            response.put("motivo", motivo);
            response.put("nivelConfianca", scoreAtualizado.getNivelConfianca());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao ajustar score do usuário: {}", email, e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao ajustar score: " + e.getMessage()));
        }
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obtém estatísticas gerais dos scores", description = "Retorna estatísticas globais dos scores de confiança")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        
        try {
            Map<String, Object> estatisticas = new HashMap<>();

            // Score médio geral
            Double scoreMedio = repositorioScoreConfianca.obterScoreMedioGeral();
            estatisticas.put("scoreMedioGeral", scoreMedio != null ? scoreMedio : 0.0);

            // Distribuição por níveis
            List<Object[]> distribuicao = repositorioScoreConfianca.obterEstatisticasPorNivel();
            Map<String, Long> distribuicaoNiveis = new HashMap<>();
            for (Object[] item : distribuicao) {
                distribuicaoNiveis.put(item[0].toString(), (Long) item[1]);
            }
            estatisticas.put("distribuicaoNiveis", distribuicaoNiveis);

            // Usuários confiáveis
            List<ScoreConfianca> usuariosConfiaveis = repositorioScoreConfianca.findUsuariosConfiaveis();
            estatisticas.put("totalUsuariosConfiaveis", usuariosConfiaveis.size());

            // Usuários com score baixo
            List<ScoreConfianca> scoresBaixos = repositorioScoreConfianca.findByScoreAbaixoDe(0.3);
            estatisticas.put("usuariosScoreBaixo", scoresBaixos.size());

            // Usuários em observação
            List<ScoreConfianca> emObservacao = repositorioScoreConfianca.findUsuariosEmObservacao(
                java.time.LocalDateTime.now());
            estatisticas.put("usuariosEmObservacao", emObservacao.size());

            return ResponseEntity.ok(estatisticas);

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas", e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao obter estatísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/nivel/{nivel}")
    @Operation(summary = "Lista usuários por nível de confiança", description = "Retorna usuários com o nível de confiança especificado")
    public ResponseEntity<Map<String, Object>> listarPorNivel(
            @Parameter(description = "Nível de confiança") @PathVariable String nivel) {
        
        try {
            ScoreConfianca.NivelConfianca nivelEnum = ScoreConfianca.NivelConfianca.valueOf(nivel.toUpperCase());
            List<ScoreConfianca> scores = repositorioScoreConfianca.findByNivelConfianca(nivelEnum);

            Map<String, Object> response = new HashMap<>();
            response.put("nivel", nivel.toUpperCase());
            response.put("total", scores.size());
            
            List<Map<String, Object>> usuarios = scores.stream()
                .map(score -> {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("email", score.getUsuario().getEmail());
                    usuario.put("scoreAtual", score.getScoreAtual());
                    usuario.put("ultimaAtualizacao", score.getUltimaAtualizacao());
                    usuario.put("emObservacao", score.getEmObservacao());
                    return usuario;
                })
                .toList();
            
            response.put("usuarios", usuarios);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Nível inválido. Use: MUITO_BAIXO, BAIXO, MEDIO, ALTO, MUITO_ALTO"));
        } catch (Exception e) {
            log.error("Erro ao listar usuários por nível: {}", nivel, e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao listar usuários: " + e.getMessage()));
        }
    }

    @PostMapping("/limpar-observacoes")
    @Operation(summary = "Remove observações expiradas", description = "Remove a flag de observação de usuários com período expirado")
    public ResponseEntity<Map<String, Object>> limparObservacoesExpiradas() {
        
        try {
            int removidos = servicoScoreConfianca.limparObservacoesExpiradas();

            Map<String, Object> response = new HashMap<>();
            response.put("observacoesRemovidas", removidos);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao limpar observações expiradas", e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao limpar observações: " + e.getMessage()));
        }
    }

    @GetMapping("/decisao/{email}")
    @Operation(summary = "Simula decisão de autenticação", description = "Retorna qual seria a decisão de autenticação baseada no score atual")
    public ResponseEntity<Map<String, Object>> simularDecisao(
            @Parameter(description = "Email do usuário") @PathVariable String email) {
        
        try {
            Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("usuario", usuario.getEmail());
            response.put("scoreAtual", score.getScoreAtual());
            response.put("nivelConfianca", score.getNivelConfianca());
            
            // Simular decisões baseadas apenas no score (sem análise de IA atual)
            String decisao;
            if (score.deveBloquear()) {
                decisao = "BLOQUEAR";
            } else if (score.requerMfa()) {
                decisao = "EXIGIR_MFA";
            } else if (score.isConfiavel()) {
                decisao = "PERMITIR";
            } else {
                decisao = "PERMITIR_COM_MONITORAMENTO";
            }
            
            response.put("decisaoSimulada", decisao);
            response.put("observacoes", Map.of(
                "confiavel", score.isConfiavel(),
                "requerMfa", score.requerMfa(),
                "deveBloquear", score.deveBloquear(),
                "emObservacao", score.getEmObservacao()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao simular decisão para usuário: {}", email, e);
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Erro ao simular decisão: " + e.getMessage()));
        }
    }
} 