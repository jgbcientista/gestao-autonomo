package br.com.auth.controller;

import br.com.auth.service.GeracaoDadosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/geracao-dados")
@RequiredArgsConstructor
@Tag(name = "Geracao de Dados", description = "Endpoints para gerar registros de acesso reais a partir de diferentes paises")
public class GeracaoDadosController {

    private final GeracaoDadosService geracaoDadosService;

    @GetMapping("/paises")
    @Operation(summary = "Listar paises disponiveis", description = "Retorna a lista de paises com IPs reais disponiveis para geracao de dados")
    public ResponseEntity<List<Map<String, String>>> listarPaises() {
        return ResponseEntity.ok(geracaoDadosService.listarPaisesDisponiveis());
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios", description = "Retorna a lista de usuarios cadastrados")
    public ResponseEntity<List<Map<String, String>>> listarUsuarios() {
        return ResponseEntity.ok(geracaoDadosService.listarUsuarios());
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar registros de acesso",
            description = "Gera registros de login reais passando pelo fluxo completo: Log + IA + Blockchain + Trust Score")
    public ResponseEntity<Map<String, Object>> gerarRegistros(@RequestBody RequisicaoGeracaoDados requisicao) {
        try {
            log.info("Requisicao de geracao de dados: usuario={}, paises={}, registrosPorPais={}",
                    requisicao.email, requisicao.paises, requisicao.registrosPorPais);

            int registrosPorPais = requisicao.registrosPorPais > 0 ? requisicao.registrosPorPais : 5;
            if (registrosPorPais > 5) registrosPorPais = 5;

            String tipoRegistro = requisicao.tipoRegistro != null ? requisicao.tipoRegistro : "MISTO";

            Map<String, Object> resultado = geracaoDadosService.gerarRegistros(
                    requisicao.email, requisicao.paises, registrosPorPais, tipoRegistro);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao gerar registros: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", e.getMessage(),
                    "totalGerados", 0
            ));
        }
    }

    public static class RequisicaoGeracaoDados {
        public String email;
        public List<String> paises;
        public int registrosPorPais = 5;
        public String tipoRegistro = "MISTO"; // NORMAL, SUSPEITO, MISTO
    }
}
