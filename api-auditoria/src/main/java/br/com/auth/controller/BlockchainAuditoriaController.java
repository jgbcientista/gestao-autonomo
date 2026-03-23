package br.com.auth.controller;

import br.com.auth.dominio.entidades.TransacaoBlockchain;
import br.com.auth.infraestrutura.repositorios.RepositorioTransacaoBlockchain;
import br.com.auth.service.BlockchainService;
import br.com.auth.service.HyperledgerFabricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para auditoria e consulta de transações blockchain
 * Fornece endpoints para verificação de integridade e relatórios de auditoria
 */
@Slf4j
@RestController
@RequestMapping("/blockchain/auditoria")
@RequiredArgsConstructor
@Tag(name = "Blockchain Auditoria", description = "API para auditoria e consulta de transações blockchain")
public class BlockchainAuditoriaController {

    private final BlockchainService blockchainService;
    private final RepositorioTransacaoBlockchain repositorioTransacaoBlockchain;
    private final org.springframework.beans.factory.ObjectProvider<HyperledgerFabricService> hyperledgerFabricServiceProvider;

    @Operation(summary = "Listar todas as transações",
               description = "Retorna todas as transações registradas na blockchain, ordenadas da mais recente para a mais antiga")
    @GetMapping("/transacoes")
    public ResponseEntity<List<TransacaoBlockchain>> listarTodasTransacoes(
            @Parameter(description = "Limite de registros (padrão: 100)")
            @RequestParam(defaultValue = "100") int limite) {

        log.debug("Listando todas as transações (limite: {})", limite);

        List<TransacaoBlockchain> transacoes = repositorioTransacaoBlockchain
                .findAll(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "criadoEm"))
                .stream()
                .limit(limite)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Buscar transação por hash",
               description = "Consulta uma transação específica pelo seu hash")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transação encontrada"),
        @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/transacao/{hash}")
    public ResponseEntity<TransacaoBlockchain> getTransactionByHash(
            @Parameter(description = "Hash da transação blockchain")
            @PathVariable String hash) {
        
        log.debug("Buscando transação por hash: {}", hash);
        
        Optional<TransacaoBlockchain> transacao = blockchainService.getTransactionByHash(hash);
        
        if (transacao.isPresent()) {
            return ResponseEntity.ok(transacao.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Buscar transações por usuário", 
               description = "Consulta todas as transações de um usuário específico")
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<List<TransacaoBlockchain>> getUserTransactions(
            @Parameter(description = "ID do usuário")
            @PathVariable Long usuarioId) {
        
        log.debug("Buscando transações para usuário: {}", usuarioId);
        
        List<TransacaoBlockchain> transacoes = blockchainService.getUserTransactions(usuarioId);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Buscar transações por período", 
               description = "Consulta transações em um período específico para auditoria")
    @GetMapping("/periodo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<List<TransacaoBlockchain>> buscarTransacoesPorPeriodo(
            @Parameter(description = "Data de início (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data de fim (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        
        log.debug("Buscando transações entre {} e {}", inicio, fim);

        List<TransacaoBlockchain> transacoes = repositorioTransacaoBlockchain.findByCriadoEmBetween(inicio, fim);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Buscar transações de alto risco", 
               description = "Consulta transações com score de risco acima do limite especificado")
    @GetMapping("/alto-risco")
    public ResponseEntity<List<TransacaoBlockchain>> buscarTransacoesAltoRisco(
            @Parameter(description = "Limite mínimo de score de risco (0.0 a 1.0)")
            @RequestParam(defaultValue = "0.7") Double limiteRisco) {
        
        log.debug("Buscando transações com risco >= {}", limiteRisco);
        
        List<TransacaoBlockchain> transacoes = blockchainService.getHighRiskTransactions(limiteRisco);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Transações suspeitas por usuário", 
               description = "Consulta transações suspeitas de um usuário específico")
    @GetMapping("/usuario/{usuarioId}/suspeitas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<List<TransacaoBlockchain>> getUserHighRiskTransactions(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0.7") Double limiteRisco) {
        
        log.debug("Buscando transações suspeitas para usuário {} com risco >= {}", usuarioId, limiteRisco);

        List<TransacaoBlockchain> transacoes = blockchainService.getHighRiskTransactionsByUser(usuarioId, limiteRisco);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Verificar integridade de transação", 
               description = "Verifica se uma transação mantém sua integridade através do hash dos dados")
    @GetMapping("/integridade/{hash}")
    public ResponseEntity<Map<String, Object>> verificarIntegridade(
            @PathVariable String hash) {
        
        log.debug("Verificando integridade da transação: {}", hash);
        
        Optional<TransacaoBlockchain> transacaoOpt = blockchainService.getTransactionByHash(hash);
        
        if (transacaoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TransacaoBlockchain transacao = transacaoOpt.get();

        // Verificação real: recalcular hash e verificar encadeamento
        boolean integridadeHash = blockchainService.verificarIntegridadeReal(transacao);
        boolean verificadoDb = Boolean.TRUE.equals(transacao.getVerificado());
        boolean integridadeOk = integridadeHash && verificadoDb;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("hash", hash);
        resultado.put("integridadeOk", integridadeOk);
        resultado.put("integridadeHash", integridadeHash);
        resultado.put("verificadoDb", verificadoDb);
        resultado.put("hashBlocoAnterior", transacao.getHashBlocoAnterior());
        resultado.put("hashDados", transacao.getHashDados());
        resultado.put("verificadoEm", LocalDateTime.now());
        resultado.put("transacao", transacao);

        if (!integridadeOk) {
            String alerta = !integridadeHash
                ? "INTEGRIDADE COMPROMETIDA - Hash recalculado não corresponde ao armazenado"
                : "TRANSAÇÃO NÃO VERIFICADA - Aguardando confirmação na blockchain";
            resultado.put("alerta", alerta);
            log.warn("Integridade comprometida detectada para transação: {} (hash={}, db={})",
                hash, integridadeHash, verificadoDb);
        }

        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Status de confirmação blockchain", 
               description = "Verifica o status de confirmação de uma transação na rede blockchain")
    @GetMapping("/confirmacao/{hash}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<Map<String, Object>> verificarConfirmacao(
            @PathVariable String hash) {
        
        log.debug("Verificando confirmação da transação: {}", hash);
        
        Optional<TransacaoBlockchain> transacaoOpt = blockchainService.getTransactionByHash(hash);
        
        if (transacaoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TransacaoBlockchain transacao = transacaoOpt.get();
        
        Map<String, Object> status = new HashMap<>();
        status.put("hash", hash);
        status.put("statusConfirmacao", transacao.getStatusConfirmacao());
        status.put("verificado", transacao.getVerificado());
        status.put("confirmacoes", transacao.getConfirmacoes());
        status.put("criadoEm", transacao.getCriadoEm());
        status.put("confirmadoEm", transacao.getConfirmadoEm());
        status.put("numeroBloco", transacao.getNumeroBloco());
        status.put("hashBloco", transacao.getHashBloco());
        
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Relatório de auditoria por usuário", 
               description = "Gera relatório completo de auditoria para um usuário")
    @GetMapping("/relatorio/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<Map<String, Object>> gerarRelatorioUsuario(
            @PathVariable Long usuarioId) {
        
        log.debug("Gerando relatório de auditoria para usuário: {}", usuarioId);
        
        List<TransacaoBlockchain> transacoes = blockchainService.getUserTransactions(usuarioId);
        // Double mediaRisco = blockchainService.calcularMediaRiscoPorUsuario(usuarioId);
        Double mediaRisco = 0.0; // Método temporariamente desabilitado
        
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("usuarioId", usuarioId);
        relatorio.put("totalTransacoes", transacoes.size());
        relatorio.put("mediaRisco", mediaRisco != null ? mediaRisco : 0.0);
        relatorio.put("transacoes", transacoes);
        
        // Estatísticas por tipo de evento
        Map<String, Long> estatisticasTipo = transacoes.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                TransacaoBlockchain::getTipoEvento,
                java.util.stream.Collectors.counting()
            ));
        relatorio.put("estatisticasPorTipo", estatisticasTipo);
        
        // Estatísticas por decisão
        Map<String, Long> estatisticasDecisao = transacoes.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                TransacaoBlockchain::getDecisao,
                java.util.stream.Collectors.counting()
            ));
        relatorio.put("estatisticasPorDecisao", estatisticasDecisao);
        
        // Contagem de transações de alto risco
        long transacoesAltoRisco = transacoes.stream()
            .mapToDouble(t -> t.getPontuacaoRisco() != null ? t.getPontuacaoRisco() : 0.0)
            .filter(risco -> risco > 0.7)
            .count();
        relatorio.put("transacoesAltoRisco", transacoesAltoRisco);
        
        relatorio.put("geradoEm", LocalDateTime.now());
        
        return ResponseEntity.ok(relatorio);
    }

    @Operation(summary = "Estatísticas gerais da blockchain", 
               description = "Fornece estatísticas gerais sobre todas as transações na blockchain")
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticasGerais() {

        log.debug("Obtendo estatísticas gerais da blockchain");

        Map<String, Object> estatisticas = new HashMap<>();

        // Total de transações
        long totalTransacoes = repositorioTransacaoBlockchain.count();
        estatisticas.put("totalTransacoes", totalTransacoes);

        // Transações confirmadas
        List<TransacaoBlockchain> confirmadas = repositorioTransacaoBlockchain
                .findByStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
        estatisticas.put("transacoesConfirmadas", confirmadas.size());

        // Transações pendentes
        List<TransacaoBlockchain> pendentes = repositorioTransacaoBlockchain
                .findByStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.PENDENTE);
        estatisticas.put("transacoesPendentes", pendentes.size());

        // Total de transações não verificadas
        List<TransacaoBlockchain> naoVerificadas = blockchainService.getUnverifiedTransactions();
        estatisticas.put("transacoesNaoVerificadas", naoVerificadas.size());

        // Transações de alto risco
        List<TransacaoBlockchain> altoRisco = blockchainService.getHighRiskTransactions(0.7);
        estatisticas.put("transacoesAltoRisco", altoRisco.size());

        // Estatísticas das últimas 24 horas
        LocalDateTime ultimasVinteQuatroHoras = LocalDateTime.now().minusHours(24);
        List<TransacaoBlockchain> recentesTransacoes = repositorioTransacaoBlockchain
                .findByCriadoEmBetween(ultimasVinteQuatroHoras, LocalDateTime.now());
        estatisticas.put("transacoesUltimas24h", recentesTransacoes.size());

        // Integridade da cadeia (verificação real)
        try {
            Map<String, Object> integridadeCadeia = blockchainService.verificarIntegridadeCadeia();
            estatisticas.put("integridadeCadeia", integridadeCadeia);
            estatisticas.put("integridadeValida", Boolean.TRUE.equals(integridadeCadeia.get("integridadeOk")));
        } catch (Exception e) {
            log.error("Erro ao verificar integridade da cadeia: {}", e.getMessage());
            estatisticas.put("integridadeValida", naoVerificadas.isEmpty());
        }

        estatisticas.put("consultadoEm", LocalDateTime.now());

        return ResponseEntity.ok(estatisticas);
    }

    @Operation(summary = "Verificar transações não confirmadas", 
               description = "Lista todas as transações que ainda não foram confirmadas na blockchain")
    @GetMapping("/nao-confirmadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransacaoBlockchain>> buscarTransacoesNaoConfirmadas() {
        
        log.debug("Buscando transações não confirmadas");
        
        List<TransacaoBlockchain> naoConfirmadas = blockchainService.getUnverifiedTransactions();
        return ResponseEntity.ok(naoConfirmadas);
    }

    @Operation(summary = "Obter informações da rede Hyperledger Fabric",
               description = "Retorna informações sobre a conectividade e status da rede Hyperledger Fabric")
    @GetMapping("/hyperledger/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obterInfoHyperledger() {
        try {
            log.debug("Obtendo informações da rede Hyperledger Fabric");

            HyperledgerFabricService hyperledgerService = hyperledgerFabricServiceProvider.getIfAvailable();
            if (hyperledgerService != null) {
                Map<String, Object> info = hyperledgerService.getNetworkInfo();
                return ResponseEntity.ok(info);
            } else {
                Map<String, Object> info = new HashMap<>();
                info.put("status", "INDISPONIVEL");
                info.put("hyperledger", "Serviço não habilitado (blockchain.enabled=false)");
                info.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(info);
            }
        } catch (Exception e) {
            log.error("Erro ao obter informações do Hyperledger", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao obter informações do Hyperledger",
                               "detalhes", e.getMessage()));
        }
    }

    @Operation(summary = "Testar conectividade com Hyperledger Fabric",
               description = "Realiza um teste de conectividade com a rede Hyperledger Fabric")
    @PostMapping("/hyperledger/test-connectivity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testarConectividadeHyperledger() {
        try {
            log.debug("Testando conectividade com Hyperledger Fabric");
            HyperledgerFabricService hyperledgerService = hyperledgerFabricServiceProvider.getIfAvailable();
            boolean conectado = false;
            if (hyperledgerService != null) {
                try {
                    conectado = hyperledgerService.testConnectivity().get();
                } catch (Exception ex) {
                    log.warn("Falha no teste de conectividade Hyperledger: {}", ex.getMessage());
                }
            }
            return ResponseEntity.ok(Map.of(
                "conectado", conectado,
                "timestamp", LocalDateTime.now(),
                "rede", "Hyperledger Fabric",
                "status", conectado ? "ONLINE" : "OFFLINE"
            ));
        } catch (Exception e) {
            log.error("Erro ao testar conectividade do Hyperledger", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao testar conectividade", 
                               "detalhes", e.getMessage(),
                               "conectado", false));
        }
    }
} 
