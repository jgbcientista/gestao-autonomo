package br.com.auth.controller;

import br.com.auth.dominio.entidades.PadraoDigitacao;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.ServicoDinamicaDigitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/keystroke")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dinamica de Digitacao", description = "APIs para captura e analise de padroes de digitacao (Keystroke Dynamics)")
public class DinamicaDigitacaoController {

    private final ServicoDinamicaDigitacao servicoDinamicaDigitacao;
    private final RepositorioUsuario repositorioUsuario;

    @PostMapping("/capturar")
    @Operation(summary = "Capturar padrao de digitacao",
               description = "Recebe eventos de tecla e processa o padrao de digitacao do usuario")
    public ResponseEntity<?> capturarPadrao(@RequestBody CapturarPadraoRequest request) {
        log.info("Recebendo captura de padrao de digitacao para: {}", request.email() != null ? request.email() : request.usuarioId());

        try {
            // Resolver usuarioId a partir do email se necessario
            Long usuarioId = request.usuarioId();
            if (usuarioId == null && request.email() != null) {
                Usuario usuario = repositorioUsuario.findByEmail(request.email()).orElse(null);
                if (usuario == null) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Usuario nao encontrado"));
                }
                usuarioId = usuario.getId();
            }

            List<ServicoDinamicaDigitacao.EventoTecla> eventos = request.eventos().stream()
                    .map(e -> new ServicoDinamicaDigitacao.EventoTecla(e.tecla(), e.keyDown(), e.keyUp()))
                    .toList();

            PadraoDigitacao padrao = servicoDinamicaDigitacao.capturarPadrao(usuarioId, eventos);

            return ResponseEntity.ok(Map.of(
                    "id", padrao.getId(),
                    "tempoMedioHold", padrao.getTempoMedioHold(),
                    "tempoMedioFlight", padrao.getTempoMedioFlight(),
                    "desvioPadraoHold", padrao.getDesvioPadraoHold(),
                    "desvioPadraoFlight", padrao.getDesvioPadraoFlight(),
                    "ehBaseline", padrao.getEhBaseline(),
                    "scoreSimilaridade", padrao.getScoreSimilaridade() != null ? padrao.getScoreSimilaridade() : "N/A",
                    "criadoEm", padrao.getCriadoEm() != null ? padrao.getCriadoEm().toString() : ""
            ));

        } catch (Exception e) {
            log.error("Erro ao capturar padrao de digitacao para usuario: {}", request.usuarioId(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao capturar padrao: " + e.getMessage()));
        }
    }

    @GetMapping("/perfil/{usuarioId}")
    @Operation(summary = "Obter perfil de digitacao",
               description = "Retorna o perfil de digitacao do usuario com baseline, amostras recentes e estatisticas")
    public ResponseEntity<?> obterPerfil(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {
        log.info("Obtendo perfil de digitacao para usuario: {}", usuarioId);

        try {
            Map<String, Object> perfil = servicoDinamicaDigitacao.obterPerfil(usuarioId);
            return ResponseEntity.ok(perfil);

        } catch (Exception e) {
            log.error("Erro ao obter perfil de digitacao para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao obter perfil: " + e.getMessage()));
        }
    }

    @PostMapping("/atualizar-baseline/{usuarioId}")
    @Operation(summary = "Atualizar baseline de digitacao",
               description = "Recalcula o baseline do usuario agregando as ultimas amostras")
    public ResponseEntity<?> atualizarBaseline(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {
        log.info("Atualizando baseline de digitacao para usuario: {}", usuarioId);

        try {
            PadraoDigitacao novoBaseline = servicoDinamicaDigitacao.atualizarBaseline(usuarioId);

            return ResponseEntity.ok(Map.of(
                    "id", novoBaseline.getId(),
                    "tempoMedioHold", novoBaseline.getTempoMedioHold(),
                    "tempoMedioFlight", novoBaseline.getTempoMedioFlight(),
                    "desvioPadraoHold", novoBaseline.getDesvioPadraoHold(),
                    "desvioPadraoFlight", novoBaseline.getDesvioPadraoFlight(),
                    "totalAmostras", novoBaseline.getTotalAmostras(),
                    "mensagem", "Baseline atualizado com sucesso"
            ));

        } catch (Exception e) {
            log.error("Erro ao atualizar baseline para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao atualizar baseline: " + e.getMessage()));
        }
    }

    @GetMapping("/similaridade/{usuarioId}")
    @Operation(summary = "Obter ultimo score de similaridade",
               description = "Retorna o score de similaridade mais recente do usuario")
    public ResponseEntity<?> obterSimilaridade(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {
        log.info("Obtendo similaridade de digitacao para usuario: {}", usuarioId);

        try {
            Map<String, Object> perfil = servicoDinamicaDigitacao.obterPerfil(usuarioId);

            @SuppressWarnings("unchecked")
            List<PadraoDigitacao> amostras = (List<PadraoDigitacao>) perfil.get("amostrasRecentes");

            Double ultimaSimilaridade = null;
            if (amostras != null && !amostras.isEmpty()) {
                ultimaSimilaridade = amostras.get(0).getScoreSimilaridade();
            }

            return ResponseEntity.ok(Map.of(
                    "usuarioId", usuarioId,
                    "scoreSimilaridade", ultimaSimilaridade != null ? ultimaSimilaridade : "N/A",
                    "possuiBaseline", perfil.get("possuiBaseline"),
                    "totalAmostras", perfil.get("totalAmostras")
            ));

        } catch (Exception e) {
            log.error("Erro ao obter similaridade para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao obter similaridade: " + e.getMessage()));
        }
    }

    // DTOs

    public record EventoTeclaDTO(
            String tecla,
            long keyDown,
            long keyUp
    ) {}

    public record CapturarPadraoRequest(
            Long usuarioId,
            String email,
            List<EventoTeclaDTO> eventos
    ) {}
}
