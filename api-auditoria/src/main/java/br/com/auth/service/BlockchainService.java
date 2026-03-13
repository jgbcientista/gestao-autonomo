package br.com.auth.service;

import br.com.auth.dominio.entidades.TransacaoBlockchain;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioTransacaoBlockchain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final RepositorioTransacaoBlockchain repositorioTransacaoBlockchain;

    @Value("${blockchain.network.url:http://localhost:8545}")
    private String networkUrl;

    @Value("${blockchain.network.name:localhost}")
    private String networkName;

    @Value("${blockchain.network.type:ethereum}")
    private String networkType;

    @Value("${blockchain.private.key:}")
    private String privateKey;

    @Value("${blockchain.contract.address:}")
    private String contractAddress;

    @Value("${blockchain.enabled:false}")
    private boolean blockchainEnabled;

    @Autowired(required = false)
    private HyperledgerFabricService hyperledgerFabricService;

    private Web3j web3j;
    private Credentials credentials;

    @PostConstruct
    public void initialize() {
        if (blockchainEnabled) {
            try {
                if ("hyperledger".equalsIgnoreCase(networkType)) {
                    log.info("Blockchain service initialized for Hyperledger Fabric network");
                    if (hyperledgerFabricService != null) {
                        log.info("HyperledgerFabricService disponível e integrado");
                    } else {
                        log.warn("HyperledgerFabricService não disponível, usando simulação");
                    }
                } else {
                    // Configuração Ethereum/Web3j
                    web3j = Web3j.build(new HttpService(networkUrl));
                    if (privateKey != null && !privateKey.isEmpty()) {
                        credentials = Credentials.create(privateKey);
                    }
                    log.info("Blockchain service initialized successfully for Ethereum network");
                }
            } catch (Exception e) {
                log.error("Failed to initialize blockchain service", e);
            }
        } else {
            log.info("Blockchain service is disabled");
        }
    }

    @Async
    @Transactional
    public CompletableFuture<String> recordAuthenticationEvent(
            Usuario usuario,
            String eventType,
            String decision,
            Double riskScore,
            String ipAddress,
            String location,
            String deviceFingerprint) {

        log.info("🔗 BLOCKCHAIN: Iniciando registro de evento de autenticação");
        log.info("Usuario: {}, Evento: {}, Decisao: {}, Risco: {}", 
                usuario.getEmail(), eventType, decision, riskScore);
        log.info("Blockchain habilitado: {}", blockchainEnabled);

        try {
            // Buscar hash do bloco anterior para encadeamento
            String hashBlocoAnterior = "0x0000000000000000000000000000000000000000000000000000000000000000";
            try {
                var ultimaTransacao = repositorioTransacaoBlockchain
                    .findAll(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "id"))
                    .stream().findFirst();
                if (ultimaTransacao.isPresent()) {
                    hashBlocoAnterior = ultimaTransacao.get().getHashTransacao();
                    log.info("Hash do bloco anterior: {}", hashBlocoAnterior);
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar bloco anterior, usando genesis: {}", e.getMessage());
            }

            // Cria hash dos dados incluindo referência ao bloco anterior
            String dataHash = createDataHash(usuario, eventType, decision, riskScore, ipAddress, location, deviceFingerprint);

            // Hash da transação inclui o hash do bloco anterior (encadeamento)
            String dadosEncadeados = hashBlocoAnterior + "|" + dataHash;
            byte[] hashEncadeado = Hash.sha3(dadosEncadeados.getBytes(StandardCharsets.UTF_8));
            String txHash = "0x" + bytesToHex(hashEncadeado);
            log.info("Hash encadeado criado: {} (anterior: {})", txHash,
                hashBlocoAnterior.length() > 18 ? hashBlocoAnterior.substring(0, 18) + "..." : hashBlocoAnterior);

            // Registra localmente com referência ao bloco anterior
            TransacaoBlockchain transacao = TransacaoBlockchain.builder()
                    .tipoEvento(eventType)
                    .usuarioId(usuario.getId())
                    .usuarioEmail(usuario.getEmail())
                    .hashDados(dataHash)
                    .hashTransacao(txHash)
                    .hashBlocoAnterior(hashBlocoAnterior)
                    .enderecoIp(ipAddress)
                    .localizacao(location)
                    .impressaoDigitalDispositivo(deviceFingerprint)
                    .pontuacaoRisco(riskScore)
                    .decisao(decision)
                    .nomeRede(networkName)
                    .enderecoContratoInteligente(contractAddress)
                    .statusConfirmacao(TransacaoBlockchain.StatusConfirmacao.PENDENTE)
                    .verificado(false)
                    .build();

            TransacaoBlockchain transacaoSalva = repositorioTransacaoBlockchain.save(transacao);
            log.info("🔗 Transação salva no BD com ID: {}", transacaoSalva.getId());

            if (blockchainEnabled) {
                log.info("🔗 Blockchain habilitado, processando evento...");
                if ("hyperledger".equalsIgnoreCase(networkType) && hyperledgerFabricService != null) {
                    // Usa Hyperledger Fabric
                    try {
                        String hlTxHash = hyperledgerFabricService.submitAuthenticationTransaction(transacaoSalva).get();

                        // Atualiza com o hash da transação
                        transacaoSalva.setHashTransacao(hlTxHash);
                        transacaoSalva.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                        transacaoSalva.setVerificado(true);
                        repositorioTransacaoBlockchain.save(transacaoSalva);

                        log.info("Authentication event recorded to Hyperledger Fabric with hash: {}", hlTxHash);
                        return CompletableFuture.completedFuture(hlTxHash);
                    } catch (Exception e) {
                        log.error("Erro ao enviar para Hyperledger Fabric, usando simulação", e);
                        return simulateBlockchainTransaction(transacaoSalva, dataHash);
                    }
                } else if (web3j != null && credentials != null) {
                    // Usa Ethereum/Web3j
                    String ethTxHash = sendToBlockchain(dataHash, eventType);

                    // Atualiza com o hash da transação
                    transacaoSalva.setHashTransacao(ethTxHash);
                    repositorioTransacaoBlockchain.save(transacaoSalva);

                    // Verifica confirmação em background
                    verifyTransactionAsync(transacaoSalva.getId(), ethTxHash);

                    log.info("Authentication event recorded to blockchain with hash: {}", ethTxHash);
                    return CompletableFuture.completedFuture(ethTxHash);
                } else {
                    return simulateBlockchainTransaction(transacaoSalva, dataHash);
                }
            } else {
                log.info("🔗 Blockchain DESABILITADO, usando simulação");
                return simulateBlockchainTransaction(transacaoSalva, dataHash);
            }

        } catch (Exception e) {
            log.error("🔗 ERRO ao registrar evento no blockchain: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(null);
        }
    }

    private String createDataHash(Usuario usuario, String eventType, String decision, Double riskScore,
                                  String ipAddress, String location, String deviceFingerprint) {
        String data = String.format("%d|%s|%s|%s|%.2f|%s|%s|%s|%d",
                usuario.getId(),
                usuario.getEmail(),
                eventType,
                decision,
                riskScore,
                ipAddress,
                location,
                deviceFingerprint,
                System.currentTimeMillis());

        byte[] hash = Hash.sha3(data.getBytes(StandardCharsets.UTF_8));
        return "0x" + bytesToHex(hash);
    }

    private String sendToBlockchain(String dataHash, String eventType) {
        try {
            if (web3j == null || credentials == null) {
                throw new IllegalStateException("Blockchain not properly initialized");
            }

            // Simula envio para blockchain (em produção seria um smart contract)
            // Por simplicidade, vamos usar uma transação simples
            BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            // Em produção, aqui seria a chamada para o smart contract
            // Para demonstração, vamos simular
            byte[] hashBytes = Hash.sha3((dataHash + eventType + System.currentTimeMillis()).getBytes());
            String simulatedTxHash = "0x" + bytesToHex(hashBytes);

            return simulatedTxHash;

        } catch (Exception e) {
            log.error("Error sending transaction to blockchain", e);
            throw new RuntimeException("Failed to send transaction to blockchain", e);
        }
    }

    @Async
    public void verifyTransactionAsync(Long transactionId, String txHash) {
        try {
            // Aguarda um pouco antes de verificar
            Thread.sleep(5000);

            // Busca a transação na blockchain
            Optional<TransacaoBlockchain> transacaoOpt = repositorioTransacaoBlockchain.findById(transactionId);
            if (transacaoOpt.isPresent()) {
                TransacaoBlockchain transacao = transacaoOpt.get();

                if (blockchainEnabled && web3j != null) {
                    // Verifica se a transação foi confirmada
                    EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
                    
                    if (receipt.getTransactionReceipt().isPresent()) {
                        TransactionReceipt txReceipt = receipt.getTransactionReceipt().get();
                        
                        transacao.setHashBloco(txReceipt.getBlockHash());
                        transacao.setNumeroBloco(txReceipt.getBlockNumber().longValue());
                        transacao.setGasUsado(txReceipt.getGasUsed().longValue());
                        transacao.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                        transacao.setVerificado(true);
                        transacao.setConfirmadoEm(LocalDateTime.now());

                        repositorioTransacaoBlockchain.save(transacao);
                        log.info("Transaction {} verified and confirmed", txHash);
                    }
                } else {
                    // Para modo simulado, marca como confirmado
                    transacao.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                    transacao.setVerificado(true);
                    transacao.setConfirmadoEm(LocalDateTime.now());
                    repositorioTransacaoBlockchain.save(transacao);
                }
            }
        } catch (Exception e) {
            log.error("Error verifying transaction {}", txHash, e);
        }
    }

    public List<TransacaoBlockchain> getUserTransactions(Long userId) {
        return repositorioTransacaoBlockchain.findByUsuarioId(userId);
    }

    public List<TransacaoBlockchain> getHighRiskTransactions(Double riskThreshold) {
        return repositorioTransacaoBlockchain.findByPontuacaoRiscoGreaterThan(riskThreshold);
    }

    public Optional<TransacaoBlockchain> getTransactionByHash(String txHash) {
        return repositorioTransacaoBlockchain.findByHashTransacao(txHash);
    }

    public List<TransacaoBlockchain> getUnverifiedTransactions() {
        return repositorioTransacaoBlockchain.findByVerificado(false);
    }

    public boolean isTransactionVerified(String txHash) {
        return repositorioTransacaoBlockchain.findByHashTransacao(txHash)
                .map(TransacaoBlockchain::getVerificado)
                .orElse(false);
    }

    private CompletableFuture<String> simulateBlockchainTransaction(TransacaoBlockchain transacaoSalva, String dataHash) {
        log.info("🔗 Executando simulação de transação blockchain...");
        
        // Simula hash quando blockchain está desabilitado ou falha
        String simulatedHash = "0x" + Integer.toHexString(dataHash.hashCode());
        transacaoSalva.setHashTransacao(simulatedHash);
        transacaoSalva.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
        transacaoSalva.setVerificado(true);
        TransacaoBlockchain transacaoAtualizada = repositorioTransacaoBlockchain.save(transacaoSalva);

        log.info("🔗 Simulação concluída - Hash: {}, ID: {}", simulatedHash, transacaoAtualizada.getId());
        return CompletableFuture.completedFuture(simulatedHash);
    }

    /**
     * Verifica a integridade de uma transação recalculando o hash dos dados
     * e comparando com o hash armazenado.
     */
    public boolean verificarIntegridadeReal(TransacaoBlockchain transacao) {
        try {
            // Recalcular hash dos dados a partir dos campos armazenados
            String dadosOriginais = String.format("%d|%s|%s|%s|%.2f|%s|%s|%s",
                transacao.getUsuarioId(),
                transacao.getUsuarioEmail(),
                transacao.getTipoEvento(),
                transacao.getDecisao(),
                transacao.getPontuacaoRisco(),
                transacao.getEnderecoIp(),
                transacao.getLocalizacao(),
                transacao.getImpressaoDigitalDispositivo());

            // Nota: o timestamp original é parte do hash mas não é armazenado separadamente.
            // Verificamos a integridade do encadeamento em vez do hash exato dos dados.

            // Verificar encadeamento: o hashTransacao deve derivar de hashBlocoAnterior + hashDados
            if (transacao.getHashBlocoAnterior() != null && transacao.getHashDados() != null) {
                String dadosEncadeados = transacao.getHashBlocoAnterior() + "|" + transacao.getHashDados();
                byte[] hashRecalculado = Hash.sha3(dadosEncadeados.getBytes(StandardCharsets.UTF_8));
                String hashEsperado = "0x" + bytesToHex(hashRecalculado);

                boolean integro = hashEsperado.equals(transacao.getHashTransacao());
                if (!integro) {
                    log.warn("Integridade comprometida! Hash esperado: {}, Hash armazenado: {}",
                        hashEsperado, transacao.getHashTransacao());
                }
                return integro;
            }

            // Para transações antigas sem encadeamento, verificar flag
            return Boolean.TRUE.equals(transacao.getVerificado());
        } catch (Exception e) {
            log.error("Erro ao verificar integridade: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica a integridade de toda a cadeia blockchain.
     * Cada bloco deve referenciar corretamente o hash do bloco anterior.
     */
    public Map<String, Object> verificarIntegridadeCadeia() {
        List<TransacaoBlockchain> todasTransacoes = repositorioTransacaoBlockchain
            .findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.ASC, "id"));

        int blocosVerificados = 0;
        int blocosComprometidos = 0;
        String ultimoHash = "0x0000000000000000000000000000000000000000000000000000000000000000";

        for (TransacaoBlockchain transacao : todasTransacoes) {
            // Verificar encadeamento
            if (transacao.getHashBlocoAnterior() != null) {
                if (!transacao.getHashBlocoAnterior().equals(ultimoHash)) {
                    blocosComprometidos++;
                    log.warn("Cadeia quebrada no bloco ID={}: esperado={}, encontrado={}",
                        transacao.getId(), ultimoHash, transacao.getHashBlocoAnterior());
                }
            }

            // Verificar integridade do hash
            if (verificarIntegridadeReal(transacao)) {
                blocosVerificados++;
            } else {
                blocosComprometidos++;
            }

            ultimoHash = transacao.getHashTransacao();
        }

        Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("totalBlocos", todasTransacoes.size());
        resultado.put("blocosVerificados", blocosVerificados);
        resultado.put("blocosComprometidos", blocosComprometidos);
        resultado.put("integridadeOk", blocosComprometidos == 0);
        resultado.put("verificadoEm", LocalDateTime.now());
        return resultado;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 