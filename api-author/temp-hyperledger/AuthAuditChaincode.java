package br.com.auth.chaincode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Chaincode Java para Auditoria de Autenticação
 * Substitui o chaincode JavaScript por implementação Java nativa
 */
@Contract(
    name = "AuthAuditContract",
    info = @Info(
        title = "Authentication Audit Contract",
        description = "Chaincode para auditoria de eventos de autenticação",
        version = "1.0.0",
        license = @License(
            name = "Apache 2.0 License",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        ),
        contact = @Contact(
            email = "admin@sistema.com",
            name = "Sistema de Autenticação"
        )
    )
)
@Default
public class AuthAuditChaincode implements ContractInterface {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Evento de Autenticação armazenado no blockchain
     */
    public static class AuthEvent {
        public String userId;
        public String userEmail;
        public String eventType;
        public String decision;
        public double riskScore;
        public String ipAddress;
        public String location;
        public String dataHash;
        public String timestamp;
        public int confirmations;
        public String blockNumber;
        public String txId;

        public AuthEvent() {}

        public AuthEvent(String userId, String userEmail, String eventType, String decision, 
                        double riskScore, String ipAddress, String location, String dataHash, 
                        String timestamp, String txId) {
            this.userId = userId;
            this.userEmail = userEmail;
            this.eventType = eventType;
            this.decision = decision;
            this.riskScore = riskScore;
            this.ipAddress = ipAddress;
            this.location = location;
            this.dataHash = dataHash;
            this.timestamp = timestamp;
            this.confirmations = 0;
            this.blockNumber = txId;
            this.txId = txId;
        }
    }

    /**
     * Inicializar o ledger
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        // Criar evento inicial do sistema
        AuthEvent initialEvent = new AuthEvent(
            "system",
            "system@auth.com", 
            "SYSTEM_INIT",
            "ALLOWED",
            0.0,
            "127.0.0.1",
            "System",
            "init_hash",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "INIT_EVENT"
        );
        initialEvent.confirmations = 1;

        try {
            String eventJson = objectMapper.writeValueAsString(initialEvent);
            stub.putStringState("INIT_EVENT", eventJson);
            
            // Emitir evento
            stub.setEvent("SystemInitialized", eventJson.getBytes());
            
            System.out.println("Sistema inicializado com sucesso");
        } catch (Exception e) {
            throw new ChaincodeException("Erro ao inicializar ledger: " + e.getMessage());
        }
    }

    /**
     * Registrar evento de autenticação
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerAuthEvent(final Context ctx, final String userId, final String userEmail,
                                   final String eventType, final String decision, final String riskScore,
                                   final String ipAddress, final String location, final String dataHash,
                                   final String timestamp) {
        
        ChaincodeStub stub = ctx.getStub();
        String txId = stub.getTxId();

        System.out.println("Registrando evento de autenticação para usuário: " + userEmail);

        try {
            AuthEvent authEvent = new AuthEvent(
                userId, userEmail, eventType, decision,
                Double.parseDouble(riskScore), ipAddress, location,
                dataHash, timestamp, txId
            );

            String eventJson = objectMapper.writeValueAsString(authEvent);
            stub.putStringState(txId, eventJson);

            // Emitir evento para notificação
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("txId", txId);
            eventData.put("userId", userId);
            eventData.put("eventType", eventType);
            eventData.put("timestamp", timestamp);

            String eventDataJson = objectMapper.writeValueAsString(eventData);
            stub.setEvent("AuthEventRegistered", eventDataJson.getBytes());

            System.out.println("Evento registrado com sucesso - TxID: " + txId);
            return txId;

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao registrar evento: " + e.getMessage());
        }
    }

    /**
     * Consultar evento específico
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAuthEvent(final Context ctx, final String txId) {
        ChaincodeStub stub = ctx.getStub();

        System.out.println("Consultando evento: " + txId);

        String eventJson = stub.getStringState(txId);
        if (eventJson == null || eventJson.isEmpty()) {
            throw new ChaincodeException("Evento " + txId + " não encontrado");
        }

        return eventJson;
    }

    /**
     * Consultar histórico de um usuário
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryUserHistory(final Context ctx, final String userId) {
        ChaincodeStub stub = ctx.getStub();

        System.out.println("Consultando histórico do usuário: " + userId);

        try {
            // Consulta rica usando CouchDB
            String queryString = String.format(
                "{\"selector\":{\"userId\":\"%s\"},\"sort\":[{\"timestamp\":\"desc\"}]}",
                userId
            );

            QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
            List<AuthEvent> userEvents = new ArrayList<>();

            for (KeyValue result : results) {
                AuthEvent event = objectMapper.readValue(result.getStringValue(), AuthEvent.class);
                userEvents.add(event);
            }

            return objectMapper.writeValueAsString(userEvents);

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao consultar histórico: " + e.getMessage());
        }
    }

    /**
     * Consultar transações por período
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryTransactionsByPeriod(final Context ctx, final String startTime, final String endTime) {
        ChaincodeStub stub = ctx.getStub();

        System.out.println("Consultando transações entre " + startTime + " e " + endTime);

        try {
            String queryString = String.format(
                "{\"selector\":{\"timestamp\":{\"$gte\":\"%s\",\"$lte\":\"%s\"}},\"sort\":[{\"timestamp\":\"desc\"}]}",
                startTime, endTime
            );

            QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
            List<AuthEvent> events = new ArrayList<>();

            for (KeyValue result : results) {
                AuthEvent event = objectMapper.readValue(result.getStringValue(), AuthEvent.class);
                events.add(event);
            }

            return objectMapper.writeValueAsString(events);

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao consultar por período: " + e.getMessage());
        }
    }

    /**
     * Consultar transações de alto risco
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryHighRiskTransactions(final Context ctx, final String minRiskScore) {
        ChaincodeStub stub = ctx.getStub();

        System.out.println("Consultando transações de alto risco (>" + minRiskScore + ")");

        try {
            String queryString = String.format(
                "{\"selector\":{\"riskScore\":{\"$gte\":%s}},\"sort\":[{\"riskScore\":\"desc\"},{\"timestamp\":\"desc\"}]}",
                minRiskScore
            );

            QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
            List<AuthEvent> events = new ArrayList<>();

            for (KeyValue result : results) {
                AuthEvent event = objectMapper.readValue(result.getStringValue(), AuthEvent.class);
                events.add(event);
            }

            return objectMapper.writeValueAsString(events);

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao consultar alto risco: " + e.getMessage());
        }
    }

    /**
     * Verificar se uma transação existe
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean verifyTransaction(final Context ctx, final String txId) {
        ChaincodeStub stub = ctx.getStub();

        System.out.println("Verificando transação: " + txId);

        String eventJson = stub.getStringState(txId);
        return eventJson != null && !eventJson.isEmpty();
    }

    /**
     * Registrar evento personalizado
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String registerCustomEvent(final Context ctx, final String eventType, 
                                     final String eventData, final String userId, final String timestamp) {
        ChaincodeStub stub = ctx.getStub();
        String txId = stub.getTxId();

        System.out.println("Registrando evento personalizado: " + eventType);

        try {
            Map<String, Object> customEvent = new HashMap<>();
            customEvent.put("eventType", eventType);
            customEvent.put("eventData", eventData);
            customEvent.put("userId", userId);
            customEvent.put("timestamp", timestamp);
            customEvent.put("txId", txId);

            String eventJson = objectMapper.writeValueAsString(customEvent);
            stub.putStringState(txId, eventJson);

            // Emitir evento
            Map<String, Object> eventNotification = new HashMap<>();
            eventNotification.put("txId", txId);
            eventNotification.put("eventType", eventType);
            eventNotification.put("userId", userId);
            eventNotification.put("timestamp", timestamp);

            String notificationJson = objectMapper.writeValueAsString(eventNotification);
            stub.setEvent("CustomEventRegistered", notificationJson.getBytes());

            return txId;

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao registrar evento personalizado: " + e.getMessage());
        }
    }

    /**
     * Ping para teste de conectividade
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ping(final Context ctx) {
        System.out.println("Ping recebido");
        return "pong";
    }

    /**
     * Obter informações do chaincode
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getInfo(final Context ctx) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("name", "AuthAuditContract");
            info.put("version", "1.0.0");
            info.put("description", "Chaincode Java para auditoria de eventos de autenticação");
            info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            info.put("language", "Java");

            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            throw new ChaincodeException("Erro ao obter info: " + e.getMessage());
        }
    }

    /**
     * Obter estatísticas gerais
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getStatistics(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        try {
            // Consultar todas as transações
            String queryString = "{\"selector\":{}}";
            QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
            
            int totalTransactions = 0;
            int highRiskTransactions = 0;
            Map<String, Integer> eventTypeCount = new HashMap<>();

            for (KeyValue result : results) {
                try {
                    AuthEvent event = objectMapper.readValue(result.getStringValue(), AuthEvent.class);
                    totalTransactions++;
                    
                    if (event.riskScore > 0.7) {
                        highRiskTransactions++;
                    }
                    
                    eventTypeCount.merge(event.eventType, 1, Integer::sum);
                    
                } catch (Exception e) {
                    // Ignorar eventos que não são AuthEvent
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTransactions", totalTransactions);
            stats.put("highRiskTransactions", highRiskTransactions);
            stats.put("eventTypeDistribution", eventTypeCount);
            stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return objectMapper.writeValueAsString(stats);

        } catch (Exception e) {
            throw new ChaincodeException("Erro ao obter estatísticas: " + e.getMessage());
        }
    }
} 