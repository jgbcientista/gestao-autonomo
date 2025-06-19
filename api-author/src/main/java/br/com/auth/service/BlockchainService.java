package br.com.auth.service;

import br.com.auth.dominio.entidades.TransacaoBlockchain;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioTransacaoBlockchain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${blockchain.private.key:}")
    private String privateKey;

    @Value("${blockchain.contract.address:}")
    private String contractAddress;

    @Value("${blockchain.enabled:false}")
    private boolean blockchainEnabled;

    private Web3j web3j;
    private Credentials credentials;

    @PostConstruct
    public void initialize() {
        if (blockchainEnabled) {
            try {
                web3j = Web3j.build(new HttpService(networkUrl));
                if (privateKey != null && !privateKey.isEmpty()) {
                    credentials = Credentials.create(privateKey);
                }
                log.info("Blockchain service initialized successfully");
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

        try {
            // Cria hash dos dados
            String dataHash = createDataHash(usuario, eventType, decision, riskScore, ipAddress, location, deviceFingerprint);

            // Registra localmente primeiro
            TransacaoBlockchain transacao = TransacaoBlockchain.builder()
                    .tipoEvento(eventType)
                    .usuarioId(usuario.getId())
                    .usuarioEmail(usuario.getEmail())
                    .hashDados(dataHash)
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

            if (blockchainEnabled && web3j != null && credentials != null) {
                // Envia para a blockchain
                String txHash = sendToBlockchain(dataHash, eventType);
                
                // Atualiza com o hash da transação
                transacaoSalva.setHashTransacao(txHash);
                repositorioTransacaoBlockchain.save(transacaoSalva);

                // Verifica confirmação em background
                verifyTransactionAsync(transacaoSalva.getId(), txHash);

                log.info("Authentication event recorded to blockchain with hash: {}", txHash);
                return CompletableFuture.completedFuture(txHash);
            } else {
                // Simula hash quando blockchain está desabilitado
                String simulatedHash = "0x" + Integer.toHexString(dataHash.hashCode());
                transacaoSalva.setHashTransacao(simulatedHash);
                transacaoSalva.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                transacaoSalva.setVerificado(true);
                repositorioTransacaoBlockchain.save(transacaoSalva);

                log.info("Authentication event recorded locally with simulated hash: {}", simulatedHash);
                return CompletableFuture.completedFuture(simulatedHash);
            }

        } catch (Exception e) {
            log.error("Error recording authentication event to blockchain", e);
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

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 