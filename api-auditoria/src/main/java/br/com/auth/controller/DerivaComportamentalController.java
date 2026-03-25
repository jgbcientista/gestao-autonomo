package br.com.auth.controller;

import br.com.auth.service.ServicoDerivaComportamental;
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
@RequestMapping("/api/v1/ia/deriva")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deriva Comportamental", description = "APIs para deteccao de deriva comportamental dos usuarios")
public class DerivaComportamentalController {

    private final ServicoDerivaComportamental servicoDeriva;

    @GetMapping("/analisar/{usuarioId}")
    @Operation(summary = "Analisar deriva comportamental",
               description = "Executa analise de drift comparando janelas de 7 e 30 dias")
    public ResponseEntity<ServicoDerivaComportamental.DerivaResponse> analisarDeriva(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {

        log.info("Analisando deriva comportamental para usuario: {}", usuarioId);

        try {
            ServicoDerivaComportamental.DerivaResponse resultado = servicoDeriva.analisarDeriva(usuarioId);
            return ResponseEntity.ok(resultado);

        } catch (RuntimeException e) {
            log.error("Erro ao analisar deriva para usuario: {}", usuarioId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro inesperado ao analisar deriva para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/historico/{usuarioId}")
    @Operation(summary = "Obter historico de deriva",
               description = "Retorna timeline de analises de deriva comportamental do usuario")
    public ResponseEntity<List<ServicoDerivaComportamental.DerivaHistoricoDTO>> obterHistorico(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId,
            @Parameter(description = "Limite de registros") @RequestParam(defaultValue = "10") int limite) {

        log.info("Obtendo historico de deriva para usuario: {}", usuarioId);

        try {
            List<ServicoDerivaComportamental.DerivaHistoricoDTO> historico = servicoDeriva.obterHistorico(usuarioId, limite);
            return ResponseEntity.ok(historico);

        } catch (RuntimeException e) {
            log.error("Erro ao obter historico de deriva para usuario: {}", usuarioId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro inesperado ao obter historico de deriva para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ajustar-baseline/{usuarioId}")
    @Operation(summary = "Ajustar baseline comportamental",
               description = "Forca ajuste do baseline comportamental do usuario")
    public ResponseEntity<ServicoDerivaComportamental.AjusteBaselineDTO> ajustarBaseline(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {

        log.info("Ajustando baseline para usuario: {}", usuarioId);

        try {
            ServicoDerivaComportamental.AjusteBaselineDTO resultado = servicoDeriva.ajustarBaseline(usuarioId);
            return ResponseEntity.ok(resultado);

        } catch (RuntimeException e) {
            log.error("Erro ao ajustar baseline para usuario: {}", usuarioId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro inesperado ao ajustar baseline para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
