package br.com.auth.controller;

import br.com.auth.service.ServicoExplicabilidadeIA;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ia/explicabilidade")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Explicabilidade IA", description = "APIs para explicabilidade das decisoes de Inteligencia Artificial")
public class ExplicabilidadeIAController {

    private final ServicoExplicabilidadeIA servicoExplicabilidade;

    @GetMapping("/{usuarioId}")
    @Operation(summary = "Gerar explicacao da analise de IA",
               description = "Retorna breakdown completo dos fatores que contribuiram para a decisao da IA")
    public ResponseEntity<ExplicabilidadeResponseDTO> gerarExplicacao(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {

        log.info("Gerando explicacao de IA para usuario: {}", usuarioId);

        try {
            ServicoExplicabilidadeIA.ExplicabilidadeResponse resultado = servicoExplicabilidade.gerarExplicacao(usuarioId);

            List<FatorContribuicaoDTO> fatoresDTO = resultado.fatores().stream()
                    .map(f -> new FatorContribuicaoDTO(
                            f.nome(), f.scoreAtual(), f.peso(), f.contribuicao(), f.nivelRisco(), f.descricao()))
                    .toList();

            ExplicabilidadeResponseDTO response = new ExplicabilidadeResponseDTO(
                    resultado.usuarioId(),
                    resultado.classificacao(),
                    resultado.scoreEnsemble(),
                    resultado.decisaoRecomendada(),
                    resultado.motivoDecisao(),
                    fatoresDTO,
                    resultado.comparacaoHabitual(),
                    resultado.timestamp()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erro ao gerar explicacao para usuario: {}", usuarioId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar explicacao para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{usuarioId}/comparacao-habitual")
    @Operation(summary = "Comparar com perfil habitual",
               description = "Retorna comparacao do perfil atual com a media historica dos ultimos 30 dias")
    public ResponseEntity<ComparacaoHabitualResponseDTO> compararComPerfilHabitual(
            @Parameter(description = "ID do usuario") @PathVariable Long usuarioId) {

        log.info("Comparando perfil habitual para usuario: {}", usuarioId);

        try {
            Map<String, double[]> comparacao = servicoExplicabilidade.compararComPerfilHabitual(usuarioId);

            ComparacaoHabitualResponseDTO response = new ComparacaoHabitualResponseDTO(
                    usuarioId,
                    comparacao,
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erro ao comparar perfil habitual para usuario: {}", usuarioId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro inesperado ao comparar perfil habitual para usuario: {}", usuarioId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTOs

    public record ExplicabilidadeResponseDTO(
            Long usuarioId,
            String classificacao,
            Double scoreEnsemble,
            String decisaoRecomendada,
            String motivoDecisao,
            List<FatorContribuicaoDTO> fatores,
            Map<String, double[]> comparacaoHabitual,
            LocalDateTime timestamp
    ) {}

    public record FatorContribuicaoDTO(
            String nome,
            Double scoreAtual,
            Double peso,
            Double contribuicao,
            String nivelRisco,
            String descricao
    ) {}

    public record ComparacaoHabitualResponseDTO(
            Long usuarioId,
            Map<String, double[]> comparacao,
            LocalDateTime timestamp
    ) {}
}
