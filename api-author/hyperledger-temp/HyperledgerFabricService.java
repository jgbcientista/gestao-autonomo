package br.com.auth.service;

import br.com.auth.config.HyperledgerFabricConfig;
import br.com.auth.dominio.entidades.TransacaoBlockchain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Serviço para operações específicas com Hyperledger Fabric
 * Gerencia transações e consultas na rede blockchain
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true")
@ConditionalOnProperty(name = "blockchain.network.type", havingValue = "hyperledger")
public class HyperledgerFabricService {

    private final HyperledgerFabricConfig fabricConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Envia uma transação de autenticação para a blockchain Hyperledger Fabric
     */
    public CompletableFuture<String> submitAuthenticationTransaction(TransacaoBlockchain transacao) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Enviando transação de autenticação para Hyperledger Fabric - Usuário: {}", 
                    transacao.getUsuarioEmail());

                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                // Preparar dados da transação
                String transactionData = prepareTransactionData(transacao);
                String functionName = "registerAuthEvent";

                // Submeter transação
                Transaction transaction = contract.createTransaction(functionName);
                byte[] result = transaction.submit(
                    transacao.getUsuarioId().toString(),
                    transacao.getUsuarioEmail(),
                    transacao.getTipoEvento(),
                    transacao.getDecisao(),
                    transacao.getPontuacaoRisco().toString(),
                    transacao.getEnderecoIp(),
                    transacao.getLocalizacao(),
                    transacao.getHashDados(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );

                String transactionId = transaction.getTransactionId();
                log.info("Transação submetida para Hyperledger Fabric - TxID: {}", transactionId);

                return transactionId;

            } catch (Exception e) {
                log.error("Erro ao submeter transação para Hyperledger Fabric", e);
                throw new RuntimeException("Falha ao submeter transação para Hyperledger Fabric", e);
            }
        });
    }

    /**
     * Consulta uma transação específica na blockchain
     */
    public CompletableFuture<String> queryTransaction(String transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                byte[] result = contract.evaluateTransaction("queryAuthEvent", transactionId);
                String response = new String(result, StandardCharsets.UTF_8);
                
                log.debug("Consulta de transação {} retornou: {}", transactionId, response);
                return response;

            } catch (Exception e) {
                log.error("Erro ao consultar transação {} no Hyperledger Fabric", transactionId, e);
                throw new RuntimeException("Falha ao consultar transação", e);
            }
        });
    }

    /**
     * Consulta o histórico de transações de um usuário
     */
    public CompletableFuture<String> queryUserHistory(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                byte[] result = contract.evaluateTransaction("queryUserHistory", userId.toString());
                String response = new String(result, StandardCharsets.UTF_8);
                
                log.debug("Histórico do usuário {} retornou {} registros", userId, response);
                return response;

            } catch (Exception e) {
                log.error("Erro ao consultar histórico do usuário {} no Hyperledger Fabric", userId, e);
                throw new RuntimeException("Falha ao consultar histórico do usuário", e);
            }
        });
    }

    /**
     * Consulta transações por período
     */
    public CompletableFuture<String> queryTransactionsByPeriod(LocalDateTime inicio, LocalDateTime fim) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                String inicioStr = inicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String fimStr = fim.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                byte[] result = contract.evaluateTransaction("queryTransactionsByPeriod", inicioStr, fimStr);
                String response = new String(result, StandardCharsets.UTF_8);
                
                log.debug("Consulta por período {} - {} retornou: {}", inicioStr, fimStr, response);
                return response;

            } catch (Exception e) {
                log.error("Erro ao consultar transações por período no Hyperledger Fabric", e);
                throw new RuntimeException("Falha ao consultar transações por período", e);
            }
        });
    }

    /**
     * Consulta transações por score de risco
     */
    public CompletableFuture<String> queryHighRiskTransactions(Double minRiskScore) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                byte[] result = contract.evaluateTransaction("queryHighRiskTransactions", minRiskScore.toString());
                String response = new String(result, StandardCharsets.UTF_8);
                
                log.debug("Consulta de transações de alto risco (>{}) retornou: {}", minRiskScore, response);
                return response;

            } catch (Exception e) {
                log.error("Erro ao consultar transações de alto risco no Hyperledger Fabric", e);
                throw new RuntimeException("Falha ao consultar transações de alto risco", e);
            }
        });
    }

    /**
     * Verifica o status de uma transação na rede
     */
    public CompletableFuture<Boolean> verifyTransactionStatus(String transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    return false;
                }

                byte[] result = contract.evaluateTransaction("verifyTransaction", transactionId);
                String response = new String(result, StandardCharsets.UTF_8);
                
                return "true".equalsIgnoreCase(response.trim());

            } catch (Exception e) {
                log.error("Erro ao verificar status da transação {} no Hyperledger Fabric", transactionId, e);
                return false;
            }
        });
    }

    /**
     * Submete uma transação com timeout personalizado
     */
    public CompletableFuture<String> submitTransactionWithTimeout(TransacaoBlockchain transacao, long timeout, TimeUnit timeUnit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                Transaction transaction = contract.createTransaction("registerAuthEvent");
                
                // Configurar timeout
                CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return transaction.submit(
                            transacao.getUsuarioId().toString(),
                            transacao.getUsuarioEmail(),
                            transacao.getTipoEvento(),
                            transacao.getDecisao(),
                            transacao.getPontuacaoRisco().toString(),
                            transacao.getEnderecoIp(),
                            transacao.getLocalizacao(),
                            transacao.getHashDados(),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Erro na submissão", e);
                    }
                });

                byte[] result = future.get(timeout, timeUnit);
                String transactionId = transaction.getTransactionId();
                
                log.info("Transação submetida com timeout - TxID: {}", transactionId);
                return transactionId;

            } catch (Exception e) {
                log.error("Erro ao submeter transação com timeout", e);
                throw new RuntimeException("Falha ao submeter transação com timeout", e);
            }
        });
    }

    /**
     * Registra um evento customizado na blockchain
     */
    public CompletableFuture<String> registerCustomEvent(String eventType, String eventData, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    throw new IllegalStateException("Contrato Fabric não está disponível");
                }

                Transaction transaction = contract.createTransaction("registerCustomEvent");
                byte[] result = transaction.submit(eventType, eventData, userId, 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                String transactionId = transaction.getTransactionId();
                log.info("Evento customizado registrado - Tipo: {}, TxID: {}", eventType, transactionId);

                return transactionId;

            } catch (Exception e) {
                log.error("Erro ao registrar evento customizado", e);
                throw new RuntimeException("Falha ao registrar evento customizado", e);
            }
        });
    }

    /**
     * Obtém informações sobre a rede Hyperledger
     */
    public Map<String, Object> getNetworkInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            info.put("channelName", fabricConfig.getChannelName());
            info.put("chaincodeName", fabricConfig.getChaincodeName());
            info.put("organizationMspId", fabricConfig.getOrganizationMspId());
            info.put("status", "CONNECTED");
            info.put("networkType", "HYPERLEDGER_FABRIC");
            
            Gateway gateway = fabricConfig.getGateway();
            if (gateway != null) {
                info.put("gatewayStatus", "ACTIVE");
            } else {
                info.put("gatewayStatus", "INACTIVE");
            }
            
            info.put("timestamp", LocalDateTime.now());
            
            return info;
            
        } catch (Exception e) {
            log.error("Erro ao obter informações da rede", e);
            return Map.of(
                "erro", "Erro ao obter informações da rede",
                "status", "ERROR",
                "detalhes", e.getMessage()
            );
        }
    }

    /**
     * Testa conectividade com a rede Hyperledger Fabric
     */
    public CompletableFuture<Boolean> testConnectivity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Contract contract = fabricConfig.getContract();
                if (contract == null) {
                    return false;
                }

                // Testar com função ping do chaincode
                byte[] result = contract.evaluateTransaction("ping");
                String response = new String(result, StandardCharsets.UTF_8);
                
                log.debug("Teste de conectividade retornou: {}", response);
                return "pong".equalsIgnoreCase(response.trim());

            } catch (Exception e) {
                log.error("Erro no teste de conectividade", e);
                return false;
            }
        });
    }

    private String prepareTransactionData(TransacaoBlockchain transacao) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("usuarioId", transacao.getUsuarioId());
            data.put("usuarioEmail", transacao.getUsuarioEmail());
            data.put("tipoEvento", transacao.getTipoEvento());
            data.put("decisao", transacao.getDecisao());
            data.put("pontuacaoRisco", transacao.getPontuacaoRisco());
            data.put("enderecoIp", transacao.getEnderecoIp());
            data.put("localizacao", transacao.getLocalizacao());
            data.put("hashDados", transacao.getHashDados());
            data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Erro ao preparar dados da transação", e);
            throw new RuntimeException("Falha ao preparar dados da transação", e);
        }
    }

    /**
     * Limpa recursos quando o serviço é destruído
     */
    public void cleanup() {
        log.info("Limpando recursos do HyperledgerFabricService");
        // Recursos são gerenciados pelo HyperledgerFabricConfig
    }
} 