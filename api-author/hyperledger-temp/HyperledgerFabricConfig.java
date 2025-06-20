package br.com.auth.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Set;

/**
 * Configuração para conexão com Hyperledger Fabric
 * Gerencia credenciais, certificados e gateway de conexão
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "blockchain.enabled", havingValue = "true")
public class HyperledgerFabricConfig {

    @Value("${blockchain.hyperledger.channel:mychannel}")
    private String channelName;

    @Value("${blockchain.hyperledger.chaincode:auth-audit}")
    private String chaincodeName;

    @Value("${blockchain.hyperledger.organization:Org1MSP}")
    private String organizationMspId;

    @Value("${blockchain.hyperledger.peer:peer0.org1.example.com:7051}")
    private String peerEndpoint;

    @Value("${blockchain.hyperledger.ca-url:https://ca.org1.example.com:7054}")
    private String caUrl;

    @Value("${blockchain.hyperledger.orderer:orderer.example.com:7050}")
    private String ordererEndpoint;

    @Value("${blockchain.hyperledger.user:appUser}")
    private String userName;

    @Value("${blockchain.hyperledger.user-secret:appUserSecret}")
    private String userSecret;

    @Value("${blockchain.hyperledger.admin-user:admin}")
    private String adminUser;

    @Value("${blockchain.hyperledger.admin-secret:adminpw}")
    private String adminSecret;

    @Value("${blockchain.hyperledger.connection-profile-path:src/main/resources/connection-profile.yaml}")
    private String connectionProfilePath;

    @Value("${blockchain.hyperledger.wallet-path:wallet}")
    private String walletPath;

    @Value("${blockchain.hyperledger.certificate-path:}")
    private String certificatePath;

    @Value("${blockchain.hyperledger.private-key-path:}")
    private String privateKeyPath;

    @Value("${blockchain.hyperledger.tls-enabled:true}")
    private boolean tlsEnabled;

    private Gateway gateway;
    private Wallet wallet;
    private Network network;
    private Contract contract;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Inicializando conexão com Hyperledger Fabric...");
            log.info("Canal: {}, Chaincode: {}, Organização: {}", channelName, chaincodeName, organizationMspId);
            
            setupWallet();
            setupGateway();
            setupNetwork();
            
            log.info("Conexão com Hyperledger Fabric estabelecida com sucesso");
        } catch (Exception e) {
            log.error("Falha ao inicializar conexão com Hyperledger Fabric", e);
            throw new RuntimeException("Falha na inicialização do Hyperledger Fabric", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (gateway != null) {
            gateway.close();
            log.info("Conexão com Hyperledger Fabric fechada");
        }
    }

    private void setupWallet() throws IOException {
        Path walletPathObj = Paths.get(walletPath);
        wallet = Wallets.newFileSystemWallet(walletPathObj);
        
        // Verificar se o usuário já existe na wallet
        if (!wallet.get(userName).isPresent()) {
            log.info("Usuário {} não encontrado na wallet. Criando identidade...", userName);
            enrollUser();
        } else {
            log.info("Usuário {} encontrado na wallet", userName);
        }
    }

    private void enrollUser() throws IOException {
        try {
            // Criar identidade de usuário
            X509Identity userIdentity = createUserIdentity();
            wallet.put(userName, userIdentity);
            log.info("Identidade do usuário {} criada e adicionada à wallet", userName);
        } catch (Exception e) {
            log.error("Erro ao criar identidade do usuário", e);
            throw new RuntimeException("Falha ao criar identidade do usuário", e);
        }
    }

    private X509Identity createUserIdentity() {
        try {
            // Se certificado e chave privada foram fornecidos via arquivo
            if (!certificatePath.isEmpty() && !privateKeyPath.isEmpty()) {
                return loadIdentityFromFiles();
            } else {
                // Usar dados de exemplo para demonstração
                return createExampleIdentity();
            }
        } catch (Exception e) {
            log.error("Erro ao criar identidade", e);
            throw new RuntimeException("Falha ao criar identidade", e);
        }
    }

    private X509Identity loadIdentityFromFiles() throws Exception {
        // TODO: Implementar carregamento de certificado e chave privada de arquivos
        // Este método carregaria os certificados reais de arquivos
        log.warn("Carregamento de certificados de arquivos não implementado ainda");
        return createExampleIdentity();
    }

    private X509Identity createExampleIdentity() {
        // Identidade de exemplo para desenvolvimento
        // Em produção, usar certificados reais
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIICKzCCAc+gAwIBAgIRAJ1wQRyAGW6RSbK9gEKYpfcwCgYIKoZIzj0EAwIwczEL\n" +
                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                "cmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\n" +
                "Lm9yZzEuZXhhbXBsZS5jb20wHhcNMjMwMTAxMDAwMDAwWhcNMzMwMTAxMDAwMDAw\n" +
                "WjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\n" +
                "U2FuIEZyYW5jaXNjbzEOMAwGA1UECxMFYWRtaW4xHjAcBgNVBAMTFUFkbWluQG9y\n" +
                "ZzEuZXhhbXBsZS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATKyJQDRcjQ\n" +
                "pXs1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnop\n" +
                "qrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghi\n" +
                "jklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz123456789\n" +
                "-----END CERTIFICATE-----\n";

        String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgabcdefghijklmnop\n" +
                "qrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghi\n" +
                "jklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz123456789\n" +
                "-----END PRIVATE KEY-----\n";

        return Identities.newX509Identity(organizationMspId, cert, privateKey);
    }

    private void setupGateway() throws IOException {
        Path networkConfigPath = Paths.get(connectionProfilePath);
        
        Gateway.Builder builder = Gateway.createBuilder();
        
        if (networkConfigPath.toFile().exists()) {
            builder.networkConfig(networkConfigPath);
        } else {
            log.warn("Arquivo de perfil de conexão não encontrado em: {}. Usando configuração padrão.", connectionProfilePath);
            builder = createDefaultGatewayBuilder();
        }
        
        gateway = builder
                .identity(wallet, userName)
                .discovery(true)
                .connect();
                
        log.info("Gateway do Hyperledger Fabric conectado com sucesso");
    }

    private Gateway.Builder createDefaultGatewayBuilder() {
        // Configuração padrão quando não há arquivo de perfil de conexão
        return Gateway.createBuilder()
                .discovery(false); // Desabilitar descoberta se não há perfil de rede
    }

    private void setupNetwork() {
        try {
            network = gateway.getNetwork(channelName);
            contract = network.getContract(chaincodeName);
            log.info("Rede {} e contrato {} conectados com sucesso", channelName, chaincodeName);
        } catch (Exception e) {
            log.error("Erro ao conectar com a rede {}", channelName, e);
            throw new RuntimeException("Falha ao conectar com a rede", e);
        }
    }

    @Bean
    @ConditionalOnProperty(value = "blockchain.network.type", havingValue = "hyperledger")
    public HyperledgerFabricService hyperledgerFabricService() {
        return new HyperledgerFabricService(this);
    }

    // Getters para acesso aos componentes Fabric
    public Gateway getGateway() {
        return gateway;
    }

    public Network getNetwork() {
        return network;
    }

    public Contract getContract() {
        return contract;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public String getOrganizationMspId() {
        return organizationMspId;
    }

    /**
     * Classe interna para configurações de identidade
     */
    @Data
    public static class FabricIdentity implements User {
        private String name;
        private Set<String> roles;
        private String account;
        private String affiliation;
        private Enrollment enrollment;
        private String mspId;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<String> getRoles() {
            return roles;
        }

        @Override
        public String getAccount() {
            return account;
        }

        @Override
        public String getAffiliation() {
            return affiliation;
        }

        @Override
        public Enrollment getEnrollment() {
            return enrollment;
        }

        @Override
        public String getMspId() {
            return mspId;
        }
    }
} 