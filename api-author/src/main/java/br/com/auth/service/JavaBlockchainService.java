package br.com.auth.service;

import br.com.auth.dominio.entidades.TransacaoBlockchain;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioTransacaoBlockchain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação Blockchain nativa em Java
 * Simula uma blockchain funcional sem dependências externas
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "blockchain.native.enabled", havingValue = "true")
public class JavaBlockchainService {

    private final RepositorioTransacaoBlockchain repositorioTransacaoBlockchain;
    
    // Simulação da blockchain em memória
    private final Map<String, Block> blockchain = new ConcurrentHashMap<>();
    private final Map<String, String> transactionPool = new ConcurrentHashMap<>();
    private volatile String lastBlockHash = "0";
    private volatile long blockNumber = 0;

    /**
     * Representação de um bloco na blockchain
     */
    public static class Block {
        public final String hash;
        public final String previousHash;
        public final long blockNumber;
        public final LocalDateTime timestamp;
        public final List<Transaction> transactions;
        public final int nonce;

        public Block(String hash, String previousHash, long blockNumber, 
                    LocalDateTime timestamp, List<Transaction> transactions, int nonce) {
            this.hash = hash;
            this.previousHash = previousHash;
            this.blockNumber = blockNumber;
            this.timestamp = timestamp;
            this.transactions = transactions;
            this.nonce = nonce;
        }
    }

    /**
     * Representação de uma transação
     */
    public static class Transaction {
        public final String txHash;
        public final String userId;
        public final String userEmail;
        public final String eventType;
        public final String decision;
        public final double riskScore;
        public final String ipAddress;
        public final String location;
        public final String dataHash;
        public final LocalDateTime timestamp;

        public Transaction(String txHash, String userId, String userEmail, String eventType,
                          String decision, double riskScore, String ipAddress, String location,
                          String dataHash, LocalDateTime timestamp) {
            this.txHash = txHash;
            this.userId = userId;
            this.userEmail = userEmail;
            this.eventType = eventType;
            this.decision = decision;
            this.riskScore = riskScore;
            this.ipAddress = ipAddress;
            this.location = location;
            this.dataHash = dataHash;
            this.timestamp = timestamp;
        }
    }

    /**
     * Registrar evento de autenticação na blockchain Java nativa
     */
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

        log.info("🔗 JAVA BLOCKCHAIN: Registrando evento de autenticação");
        log.info("Usuario: {}, Evento: {}, Decisao: {}, Risco: {}", 
                usuario.getEmail(), eventType, decision, riskScore);

        try {
            // Criar hash dos dados
            String dataHash = createSHA256Hash(
                usuario.getId() + "|" + usuario.getEmail() + "|" + eventType + "|" + 
                decision + "|" + riskScore + "|" + ipAddress + "|" + location + "|" + 
                deviceFingerprint + "|" + System.currentTimeMillis()
            );

            // Gerar hash único da transação
            String txHash = "tx_" + createSHA256Hash(dataHash + System.nanoTime());

            // Criar transação
            Transaction transaction = new Transaction(
                txHash, usuario.getId().toString(), usuario.getEmail(), eventType,
                decision, riskScore, ipAddress, location, dataHash, LocalDateTime.now()
            );

            // Adicionar à pool de transações
            transactionPool.put(txHash, dataHash);

            // Registrar no banco de dados
            TransacaoBlockchain transacaoBlockchain = TransacaoBlockchain.builder()
                    .hashTransacao(txHash)
                    .tipoEvento(eventType)
                    .usuarioId(usuario.getId())
                    .usuarioEmail(usuario.getEmail())
                    .hashDados(dataHash)
                    .enderecoIp(ipAddress)
                    .localizacao(location)
                    .impressaoDigitalDispositivo(deviceFingerprint)
                    .pontuacaoRisco(riskScore)
                    .decisao(decision)
                    .nomeRede("JavaBlockchain")
                    .statusConfirmacao(TransacaoBlockchain.StatusConfirmacao.PENDENTE)
                    .verificado(false)
                    .build();

            TransacaoBlockchain transacaoSalva = repositorioTransacaoBlockchain.save(transacaoBlockchain);
            log.info("🔗 Transação salva no BD com ID: {}", transacaoSalva.getId());

            // Minerar bloco em background se houver transações suficientes
            if (transactionPool.size() >= 3) { // Minera bloco a cada 3 transações
                CompletableFuture.runAsync(this::mineBlock);
            }

            // Simular confirmação em 2 segundos
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    confirmTransaction(txHash);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            log.info("🔗 JAVA BLOCKCHAIN: Transação registrada com hash: {}", txHash);
            return CompletableFuture.completedFuture(txHash);

        } catch (Exception e) {
            log.error("🔗 ERRO no JavaBlockchain: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Minerar um novo bloco
     */
    private void mineBlock() {
        log.info("🔗 JAVA BLOCKCHAIN: Iniciando mineração de bloco...");

        try {
            // Coletar transações da pool
            List<Transaction> transactions = new ArrayList<>();
            List<String> txHashes = new ArrayList<>(transactionPool.keySet());
            
            for (String txHash : txHashes) {
                // Buscar transação no banco
                repositorioTransacaoBlockchain.findByHashTransacao(txHash).ifPresent(tx -> {
                    Transaction transaction = new Transaction(
                        tx.getHashTransacao(),
                        tx.getUsuarioId().toString(),
                        tx.getUsuarioEmail(),
                        tx.getTipoEvento(),
                        tx.getDecisao(),
                        tx.getPontuacaoRisco(),
                        tx.getEnderecoIp(),
                        tx.getLocalizacao(),
                        tx.getHashDados(),
                        tx.getCriadoEm()
                    );
                    transactions.add(transaction);
                    transactionPool.remove(txHash);
                });
            }

            if (transactions.isEmpty()) {
                return;
            }

            // Proof of Work simples (encontrar hash com zeros à esquerda)
            int nonce = 0;
            String blockData = lastBlockHash + blockNumber + transactions.toString();
            String blockHash;

            do {
                nonce++;
                blockHash = createSHA256Hash(blockData + nonce);
            } while (!blockHash.startsWith("00")); // Dificuldade baixa para desenvolvimento

            // Criar novo bloco
            Block newBlock = new Block(
                blockHash,
                lastBlockHash,
                ++blockNumber,
                LocalDateTime.now(),
                transactions,
                nonce
            );

            // Adicionar à blockchain
            blockchain.put(blockHash, newBlock);
            lastBlockHash = blockHash;

            // Atualizar transações no banco como confirmadas
            final String finalBlockHash = blockHash;
            final long finalBlockNumber = blockNumber;
            for (Transaction tx : transactions) {
                repositorioTransacaoBlockchain.findByHashTransacao(tx.txHash).ifPresent(dbTx -> {
                    dbTx.setHashBloco(finalBlockHash);
                    dbTx.setNumeroBloco(finalBlockNumber);
                    dbTx.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                    dbTx.setVerificado(true);
                    dbTx.setConfirmadoEm(LocalDateTime.now());
                    repositorioTransacaoBlockchain.save(dbTx);
                });
            }

            log.info("🔗 JAVA BLOCKCHAIN: Bloco minerado! Hash: {}, Número: {}, Transações: {}", 
                    blockHash, blockNumber, transactions.size());

        } catch (Exception e) {
            log.error("🔗 Erro na mineração: {}", e.getMessage(), e);
        }
    }

    /**
     * Confirmar transação individual
     */
    private void confirmTransaction(String txHash) {
        repositorioTransacaoBlockchain.findByHashTransacao(txHash).ifPresent(tx -> {
            if (tx.getStatusConfirmacao() == TransacaoBlockchain.StatusConfirmacao.PENDENTE) {
                tx.setStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao.CONFIRMADO);
                tx.setVerificado(true);
                tx.setConfirmadoEm(LocalDateTime.now());
                repositorioTransacaoBlockchain.save(tx);
                log.info("🔗 Transação confirmada: {}", txHash);
            }
        });
    }

    /**
     * Verificar integridade da blockchain
     */
    public boolean verifyBlockchainIntegrity() {
        String previousHash = "0";
        
        for (long i = 1; i <= blockNumber; i++) {
            final long currentBlockNumber = i;
            Block block = blockchain.values().stream()
                    .filter(b -> b.blockNumber == currentBlockNumber)
                    .findFirst()
                    .orElse(null);
                    
            if (block == null) {
                return false;
            }
            
            if (!block.previousHash.equals(previousHash)) {
                return false;
            }
            
            // Verificar hash do bloco
            String calculatedHash = createSHA256Hash(
                block.previousHash + block.blockNumber + block.transactions.toString() + block.nonce
            );
            
            if (!block.hash.equals(calculatedHash)) {
                return false;
            }
            
            previousHash = block.hash;
        }
        
        return true;
    }

    /**
     * Obter estatísticas da blockchain
     */
    public Map<String, Object> getBlockchainStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBlocks", blockchain.size());
        stats.put("lastBlockHash", lastBlockHash);
        stats.put("currentBlockNumber", blockNumber);
        stats.put("pendingTransactions", transactionPool.size());
        stats.put("isValid", verifyBlockchainIntegrity());
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }

    /**
     * Obter bloco por hash
     */
    public Optional<Block> getBlock(String blockHash) {
        return Optional.ofNullable(blockchain.get(blockHash));
    }

    /**
     * Obter todos os blocos
     */
    public List<Block> getAllBlocks() {
        return blockchain.values().stream()
                .sorted((a, b) -> Long.compare(a.blockNumber, b.blockNumber))
                .toList();
    }

    /**
     * Criar hash SHA256
     */
    private String createSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }

    /**
     * Criar HMAC para verificação adicional
     */
    private String createHMAC(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar HMAC", e);
        }
    }
} 