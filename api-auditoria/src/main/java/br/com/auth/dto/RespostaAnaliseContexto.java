package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RespostaAnaliseContexto {

    @JsonProperty("id_analise")
    private String idAnalise;

    @JsonProperty("usuario_id")
    private Long usuarioId;

    @JsonProperty("pontuacao_risco_geral")
    private Double pontuacaoRiscoGeral;

    @JsonProperty("nivel_risco")
    private String nivelRisco; // BAIXO, MEDIO, ALTO, CRITICO

    @JsonProperty("decisao")
    private String decisao; // PERMITIR, NEGAR, REQUER_MFA, REQUER_VERIFICACAO_ADICIONAL

    @JsonProperty("pontuacao_confianca")
    private Double pontuacaoConfianca;

    @JsonProperty("detalhes_analise")
    private DetalhesAnalise detalhesAnalise;

    @JsonProperty("recomendacoes")
    private List<String> recomendacoes;

    @JsonProperty("hash_blockchain")
    private String hashBlockchain;

    @JsonProperty("processado_em")
    private LocalDateTime processadoEm;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalhesAnalise {
        @JsonProperty("analise_localizacao")
        private AnaliseLocalizacao analiseLocalizacao;

        @JsonProperty("analise_dispositivo")
        private AnaliseDispositivo analiseDispositivo;

        @JsonProperty("analise_temporal")
        private AnaliseTemporal analiseTemporal;

        @JsonProperty("analise_comportamental")
        private AnaliseComportamental analiseComportamental;

        @JsonProperty("analise_rede")
        private AnaliseRede analiseRede;
        
        // Métodos auxiliares para compatibilidade
        public AnaliseLocalizacao getLocationAnalysis() { return analiseLocalizacao; }
        public void setLocationAnalysis(AnaliseLocalizacao analise) { this.analiseLocalizacao = analise; }
        public AnaliseDispositivo getDeviceAnalysis() { return analiseDispositivo; }
        public void setDeviceAnalysis(AnaliseDispositivo analise) { this.analiseDispositivo = analise; }
        public AnaliseTemporal getTemporalAnalysis() { return analiseTemporal; }
        public void setTemporalAnalysis(AnaliseTemporal analise) { this.analiseTemporal = analise; }
        public AnaliseComportamental getBehavioralAnalysis() { return analiseComportamental; }
        public void setBehavioralAnalysis(AnaliseComportamental analise) { this.analiseComportamental = analise; }
        public AnaliseRede getNetworkAnalysis() { return analiseRede; }
        public void setNetworkAnalysis(AnaliseRede analise) { this.analiseRede = analise; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnaliseLocalizacao {
        @JsonProperty("pontuacao_consistencia")
        private Double pontuacaoConsistencia;

        @JsonProperty("distancia_do_usual")
        private Double distanciaDoUsual;

        @JsonProperty("localizacao_conhecida")
        private Boolean localizacaoConhecida;

        @JsonProperty("nivel_risco_pais")
        private String nivelRiscoPais;

        @JsonProperty("sinalizadores")
        private List<String> sinalizadores;
        
        // Métodos auxiliares para compatibilidade
        public Double getConsistencyScore() { return pontuacaoConsistencia; }
        public void setConsistencyScore(Double pontuacao) { this.pontuacaoConsistencia = pontuacao; }
        public Double getDistanceFromUsual() { return distanciaDoUsual; }
        public void setDistanceFromUsual(Double distancia) { this.distanciaDoUsual = distancia; }
        public Boolean getIsKnownLocation() { return localizacaoConhecida; }
        public void setIsKnownLocation(Boolean conhecida) { this.localizacaoConhecida = conhecida; }
        public String getCountryRiskLevel() { return nivelRiscoPais; }
        public void setCountryRiskLevel(String nivel) { this.nivelRiscoPais = nivel; }
        public List<String> getFlags() { return sinalizadores; }
        public void setFlags(List<String> flags) { this.sinalizadores = flags; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnaliseDispositivo {
        @JsonProperty("pontuacao_consistencia")
        private Double pontuacaoConsistencia;

        @JsonProperty("dispositivo_conhecido")
        private Boolean dispositivoConhecido;

        @JsonProperty("reputacao_dispositivo")
        private String reputacaoDispositivo;

        @JsonProperty("integridade_navegador")
        private Double integridadeNavegador;

        @JsonProperty("sinalizadores")
        private List<String> sinalizadores;
        
        // Métodos auxiliares para compatibilidade
        public Double getConsistencyScore() { return pontuacaoConsistencia; }
        public void setConsistencyScore(Double pontuacao) { this.pontuacaoConsistencia = pontuacao; }
        public Boolean getIsKnownDevice() { return dispositivoConhecido; }
        public void setIsKnownDevice(Boolean conhecido) { this.dispositivoConhecido = conhecido; }
        public String getDeviceReputation() { return reputacaoDispositivo; }
        public void setDeviceReputation(String reputacao) { this.reputacaoDispositivo = reputacao; }
        public Double getBrowserIntegrity() { return integridadeNavegador; }
        public void setBrowserIntegrity(Double integridade) { this.integridadeNavegador = integridade; }
        public List<String> getFlags() { return sinalizadores; }
        public void setFlags(List<String> flags) { this.sinalizadores = flags; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnaliseTemporal {
        @JsonProperty("pontuacao_padrao")
        private Double pontuacaoPadrao;

        @JsonProperty("horario_usual")
        private Boolean horarioUsual;

        @JsonProperty("desvio_tempo")
        private Double desvioTempo;

        @JsonProperty("anomalia_frequencia")
        private Boolean anomaliaFrequencia;

        @JsonProperty("sinalizadores")
        private List<String> sinalizadores;
        
        // Métodos auxiliares para compatibilidade
        public Double getPatternScore() { return pontuacaoPadrao; }
        public void setPatternScore(Double pontuacao) { this.pontuacaoPadrao = pontuacao; }
        public Boolean getIsUsualTime() { return horarioUsual; }
        public void setIsUsualTime(Boolean usual) { this.horarioUsual = usual; }
        public Double getTimeDeviation() { return desvioTempo; }
        public void setTimeDeviation(Double desvio) { this.desvioTempo = desvio; }
        public Boolean getFrequencyAnomaly() { return anomaliaFrequencia; }
        public void setFrequencyAnomaly(Boolean anomalia) { this.anomaliaFrequencia = anomalia; }
        public List<String> getFlags() { return sinalizadores; }
        public void setFlags(List<String> flags) { this.sinalizadores = flags; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnaliseComportamental {
        @JsonProperty("pontuacao_comportamental")
        private Double pontuacaoComportamental;

        @JsonProperty("anomalia_digitacao")
        private Boolean anomaliaDigitacao;

        @JsonProperty("padrao_interacao")
        private String padraoInteracao;

        @JsonProperty("confianca_ia")
        private Double confiancaIa;

        @JsonProperty("sinalizadores")
        private List<String> sinalizadores;
        
        // Métodos auxiliares para compatibilidade
        public Double getBehavioralScore() { return pontuacaoComportamental; }
        public void setBehavioralScore(Double pontuacao) { this.pontuacaoComportamental = pontuacao; }
        public Boolean getTypingAnomaly() { return anomaliaDigitacao; }
        public void setTypingAnomaly(Boolean anomalia) { this.anomaliaDigitacao = anomalia; }
        public String getInteractionPattern() { return padraoInteracao; }
        public void setInteractionPattern(String padrao) { this.padraoInteracao = padrao; }
        public Double getAiConfidence() { return confiancaIa; }
        public void setAiConfidence(Double confianca) { this.confiancaIa = confianca; }
        public List<String> getFlags() { return sinalizadores; }
        public void setFlags(List<String> flags) { this.sinalizadores = flags; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnaliseRede {
        @JsonProperty("pontuacao_rede")
        private Double pontuacaoRede;

        @JsonProperty("indicadores_ameaca")
        private Map<String, Boolean> indicadoresAmeaca;

        @JsonProperty("reputacao_ip")
        private String reputacaoIp;

        @JsonProperty("trafego_anomalo")
        private Boolean trafegoAnomalo;

        @JsonProperty("sinalizadores")
        private List<String> sinalizadores;
        
        // Métodos auxiliares para compatibilidade
        public Double getNetworkScore() { return pontuacaoRede; }
        public void setNetworkScore(Double pontuacao) { this.pontuacaoRede = pontuacao; }
        public Map<String, Boolean> getThreatIndicators() { return indicadoresAmeaca; }
        public void setThreatIndicators(Map<String, Boolean> indicadores) { this.indicadoresAmeaca = indicadores; }
        public String getIpReputation() { return reputacaoIp; }
        public void setIpReputation(String reputacao) { this.reputacaoIp = reputacao; }
        public Boolean getAnomalousTraffic() { return trafegoAnomalo; }
        public void setAnomalousTraffic(Boolean anomalo) { this.trafegoAnomalo = anomalo; }
        public List<String> getFlags() { return sinalizadores; }
        public void setFlags(List<String> flags) { this.sinalizadores = flags; }
    }

    // Métodos auxiliares para compatibilidade com código existente
    public String getAnalysisId() { return idAnalise; }
    public void setAnalysisId(String id) { this.idAnalise = id; }
    public Long getUserId() { return usuarioId; }
    public void setUserId(Long id) { this.usuarioId = id; }
    public Double getOverallRiskScore() { return pontuacaoRiscoGeral; }
    public void setOverallRiskScore(Double pontuacao) { this.pontuacaoRiscoGeral = pontuacao; }
    public String getRiskLevel() { return nivelRisco; }
    public void setRiskLevel(String nivel) { this.nivelRisco = nivel; }
    public String getDecision() { return decisao; }
    public void setDecision(String decisao) { this.decisao = decisao; }
    public Double getConfidenceScore() { return pontuacaoConfianca; }
    public void setConfidenceScore(Double pontuacao) { this.pontuacaoConfianca = pontuacao; }
    public DetalhesAnalise getAnalysisDetails() { return detalhesAnalise; }
    public void setAnalysisDetails(DetalhesAnalise detalhes) { this.detalhesAnalise = detalhes; }
    public List<String> getRecommendations() { return recomendacoes; }
    public void setRecommendations(List<String> recomendacoes) { this.recomendacoes = recomendacoes; }
    public String getBlockchainHash() { return hashBlockchain; }
    public void setBlockchainHash(String hash) { this.hashBlockchain = hash; }
    public LocalDateTime getProcessedAt() { return processadoEm; }
    public void setProcessedAt(LocalDateTime data) { this.processadoEm = data; }
} 