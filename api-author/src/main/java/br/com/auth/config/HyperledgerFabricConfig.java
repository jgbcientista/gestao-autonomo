package br.com.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuração para conexão com Hyperledger Fabric
 * Suporta modo simulação funcional
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

    @Value("${blockchain.hyperledger.tls-enabled:true}")
    private boolean tlsEnabled;

    @Value("${blockchain.hyperledger.simulation-mode:true}")
    private boolean simulationMode;

    private boolean isConnected = false;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Inicializando configuração Hyperledger Fabric...");
            log.info("Canal: {}, Chaincode: {}, Organização: {}", channelName, chaincodeName, organizationMspId);
            log.info("Modo simulação: {}", simulationMode);
            
            initializeSimulationMode();
            
            log.info("Configuração Hyperledger Fabric estabelecida com sucesso (Modo: Simulação Funcional)");
        } catch (Exception e) {
            log.error("Falha ao inicializar configuração Hyperledger Fabric", e);
            throw new RuntimeException("Falha na inicialização do Hyperledger Fabric", e);
        }
    }

    private void initializeSimulationMode() throws Exception {
        log.info("Inicializando modo simulação funcional do Hyperledger Fabric");
        
        // Simula conexão estabelecida
        isConnected = true;
        simulationMode = true;
        
        log.info("Modo simulação do Hyperledger Fabric inicializado com sucesso");
        log.info("Rede simulada: Canal={}, Chaincode={}", channelName, chaincodeName);
    }

    // Getters para acesso às configurações
    public String getChannelName() {
        return channelName;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public String getOrganizationMspId() {
        return organizationMspId;
    }

    public String getPeerEndpoint() {
        return peerEndpoint;
    }

    public String getCaUrl() {
        return caUrl;
    }

    public String getOrdererEndpoint() {
        return ordererEndpoint;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserSecret() {
        return userSecret;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminSecret() {
        return adminSecret;
    }

    public String getConnectionProfilePath() {
        return connectionProfilePath;
    }

    public String getWalletPath() {
        return walletPath;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
} 