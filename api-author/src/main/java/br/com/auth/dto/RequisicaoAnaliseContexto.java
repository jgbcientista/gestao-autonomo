package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequisicaoAnaliseContexto {

    @JsonProperty("usuario_id")
    private Long usuarioId;

    @JsonProperty("endereco_ip")
    private String enderecoIp;

    @JsonProperty("agente_usuario")
    private String agenteUsuario;

    @JsonProperty("localizacao")
    private String localizacao;

    @JsonProperty("impressao_digital_dispositivo")
    private String impressaoDigitalDispositivo;

    @JsonProperty("dados_geolocalizacao")
    private DadosGeolocalizacao dadosGeolocalizacao;

    @JsonProperty("info_rede")
    private InfoRede infoRede;

    @JsonProperty("dados_comportamentais")
    private DadosComportamentais dadosComportamentais;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DadosGeolocalizacao {
        private Double latitude;
        private Double longitude;
        private String pais;
        private String regiao;
        private String cidade;
        private String fusoHorario;
        private String provedor;
        
        // Métodos auxiliares para compatibilidade
        public String getCountry() { return pais; }
        public void setCountry(String pais) { this.pais = pais; }
        public String getRegion() { return regiao; }
        public void setRegion(String regiao) { this.regiao = regiao; }
        public String getCity() { return cidade; }
        public void setCity(String cidade) { this.cidade = cidade; }
        public String getTimezone() { return fusoHorario; }
        public void setTimezone(String fuso) { this.fusoHorario = fuso; }
        public String getIsp() { return provedor; }
        public void setIsp(String provedor) { this.provedor = provedor; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoRede {
        @JsonProperty("endereco_ip")
        private String enderecoIp;
        
        @JsonProperty("agente_usuario")
        private String agenteUsuario;
        
        @JsonProperty("tipo_conexao")
        private String tipoConexao;
        
        @JsonProperty("vpn_detectado")
        private Boolean vpnDetectado;
        
        @JsonProperty("proxy_detectado")
        private Boolean proxyDetectado;
        
        @JsonProperty("tor_detectado")
        private Boolean torDetectado;
        
        @JsonProperty("reputacao_ip")
        private String reputacaoIp;
        
        // Métodos auxiliares para compatibilidade
        public String getIpAddress() { return enderecoIp; }
        public void setIpAddress(String ip) { this.enderecoIp = ip; }
        public String getUserAgent() { return agenteUsuario; }
        public void setUserAgent(String agente) { this.agenteUsuario = agente; }
        public String getConnectionType() { return tipoConexao; }
        public void setConnectionType(String tipo) { this.tipoConexao = tipo; }
        public Boolean getVpnDetected() { return vpnDetectado; }
        public void setVpnDetected(Boolean detectado) { this.vpnDetectado = detectado; }
        public Boolean getProxyDetected() { return proxyDetectado; }
        public void setProxyDetected(Boolean detectado) { this.proxyDetectado = detectado; }
        public Boolean getTorDetected() { return torDetectado; }
        public void setTorDetected(Boolean detectado) { this.torDetectado = detectado; }
        public String getIpReputation() { return reputacaoIp; }
        public void setIpReputation(String reputacao) { this.reputacaoIp = reputacao; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DadosComportamentais {
        @JsonProperty("padroes_digitacao")
        private Map<String, Object> padroesDigitacao;
        
        @JsonProperty("movimentos_mouse")
        private Map<String, Object> movimentosMouse;
        
        @JsonProperty("resolucao_tela")
        private String resolucaoTela;
        
        @JsonProperty("plugins_navegador")
        private String[] pluginsNavegador;
        
        @JsonProperty("duracao_sessao")
        private Long duracaoSessao;
        
        @JsonProperty("interacoes_pagina")
        private Integer interacoesPagina;
        
        // Métodos auxiliares para compatibilidade
        public Map<String, Object> getTypingPatterns() { return padroesDigitacao; }
        public void setTypingPatterns(Map<String, Object> padroes) { this.padroesDigitacao = padroes; }
        public Map<String, Object> getMouseMovements() { return movimentosMouse; }
        public void setMouseMovements(Map<String, Object> movimentos) { this.movimentosMouse = movimentos; }
        public String getScreenResolution() { return resolucaoTela; }
        public void setScreenResolution(String resolucao) { this.resolucaoTela = resolucao; }
        public String[] getBrowserPlugins() { return pluginsNavegador; }
        public void setBrowserPlugins(String[] plugins) { this.pluginsNavegador = plugins; }
        public Long getSessionDuration() { return duracaoSessao; }
        public void setSessionDuration(Long duracao) { this.duracaoSessao = duracao; }
        public Integer getPageInteractions() { return interacoesPagina; }
        public void setPageInteractions(Integer interacoes) { this.interacoesPagina = interacoes; }
    }

    // Métodos auxiliares para compatibilidade com código existente
    public Long getUserId() { return usuarioId; }
    public void setUserId(Long id) { this.usuarioId = id; }
    public String getIpAddress() { return enderecoIp; }
    public void setIpAddress(String ip) { this.enderecoIp = ip; }
    public String getUserAgent() { return agenteUsuario; }
    public void setUserAgent(String agente) { this.agenteUsuario = agente; }
    public String getLocation() { return localizacao; }
    public void setLocation(String localizacao) { this.localizacao = localizacao; }
    public String getDeviceFingerprint() { return impressaoDigitalDispositivo; }
    public void setDeviceFingerprint(String impressao) { this.impressaoDigitalDispositivo = impressao; }
    public DadosGeolocalizacao getGeolocation() { return dadosGeolocalizacao; }
    public void setGeolocation(DadosGeolocalizacao dados) { this.dadosGeolocalizacao = dados; }
    public InfoRede getNetworkInfo() { return infoRede; }
    public void setNetworkInfo(InfoRede info) { this.infoRede = info; }
    public DadosComportamentais getBehavioralData() { return dadosComportamentais; }
    public void setBehavioralData(DadosComportamentais dados) { this.dadosComportamentais = dados; }
} 