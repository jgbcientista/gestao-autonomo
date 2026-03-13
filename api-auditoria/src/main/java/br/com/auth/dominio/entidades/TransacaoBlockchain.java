package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transacoes_blockchain")
@EntityListeners(AuditingEntityListener.class)
public class TransacaoBlockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hash_transacao", nullable = false, unique = true)
    private String hashTransacao;

    @Column(name = "hash_bloco")
    private String hashBloco;

    @Column(name = "hash_bloco_anterior")
    private String hashBlocoAnterior;

    @Column(name = "numero_bloco")
    private Long numeroBloco;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "hash_dados", nullable = false)
    private String hashDados;

    @Column(name = "endereco_ip")
    private String enderecoIp;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "impressao_digital_dispositivo")
    private String impressaoDigitalDispositivo;

    @Column(name = "pontuacao_risco")
    private Double pontuacaoRisco;

    @Column(name = "decisao", nullable = false)
    private String decisao; // PERMITIDO, NEGADO, REQUER_MFA

    @Column(name = "gas_usado")
    private Long gasUsado;

    @Column(name = "preco_gas")
    private String precoGas;

    @Column(name = "taxa_transacao")
    private String taxaTransacao;

    @Column(name = "nome_rede")
    private String nomeRede;

    @Column(name = "endereco_contrato_inteligente")
    private String enderecoContratoInteligente;

    @Column(name = "status_confirmacao")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusConfirmacao statusConfirmacao = StatusConfirmacao.PENDENTE;

    @Column(name = "confirmacoes")
    @Builder.Default
    private Integer confirmacoes = 0;

    @Column(name = "verificado")
    @Builder.Default
    private Boolean verificado = false;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    public enum StatusConfirmacao {
        PENDENTE,
        CONFIRMADO,
        FALHADO,
        CANCELADO
    }

    // Métodos auxiliares para compatibilidade com código existente
    public String getTransactionHash() {
        return hashTransacao;
    }

    public void setTransactionHash(String hash) {
        this.hashTransacao = hash;
    }

    public String getBlockHash() {
        return hashBloco;
    }

    public void setBlockHash(String hash) {
        this.hashBloco = hash;
    }

    public Long getBlockNumber() {
        return numeroBloco;
    }

    public void setBlockNumber(Long numero) {
        this.numeroBloco = numero;
    }

    public String getEventType() {
        return tipoEvento;
    }

    public void setEventType(String tipo) {
        this.tipoEvento = tipo;
    }

    public Long getUserId() {
        return usuarioId;
    }

    public void setUserId(Long id) {
        this.usuarioId = id;
    }

    public String getUserEmail() {
        return usuarioEmail;
    }

    public void setUserEmail(String email) {
        this.usuarioEmail = email;
    }

    public String getDataHash() {
        return hashDados;
    }

    public void setDataHash(String hash) {
        this.hashDados = hash;
    }

    public String getIpAddress() {
        return enderecoIp;
    }

    public void setIpAddress(String ip) {
        this.enderecoIp = ip;
    }

    public String getLocation() {
        return localizacao;
    }

    public void setLocation(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getDeviceFingerprint() {
        return impressaoDigitalDispositivo;
    }

    public void setDeviceFingerprint(String impressao) {
        this.impressaoDigitalDispositivo = impressao;
    }

    public Double getRiskScore() {
        return pontuacaoRisco;
    }

    public void setRiskScore(Double pontuacao) {
        this.pontuacaoRisco = pontuacao;
    }

    public String getDecision() {
        return decisao;
    }

    public void setDecision(String decisao) {
        this.decisao = decisao;
    }

    public Long getGasUsed() {
        return gasUsado;
    }

    public void setGasUsed(Long gas) {
        this.gasUsado = gas;
    }

    public String getGasPrice() {
        return precoGas;
    }

    public void setGasPrice(String preco) {
        this.precoGas = preco;
    }

    public String getTransactionFee() {
        return taxaTransacao;
    }

    public void setTransactionFee(String taxa) {
        this.taxaTransacao = taxa;
    }

    public String getNetworkName() {
        return nomeRede;
    }

    public void setNetworkName(String nome) {
        this.nomeRede = nome;
    }

    public String getSmartContractAddress() {
        return enderecoContratoInteligente;
    }

    public void setSmartContractAddress(String endereco) {
        this.enderecoContratoInteligente = endereco;
    }

    public StatusConfirmacao getConfirmationStatus() {
        return statusConfirmacao;
    }

    public void setConfirmationStatus(StatusConfirmacao status) {
        this.statusConfirmacao = status;
    }

    public Integer getConfirmations() {
        return confirmacoes;
    }

    public void setConfirmations(Integer confirmacoes) {
        this.confirmacoes = confirmacoes;
    }

    public Boolean getVerified() {
        return verificado;
    }

    public void setVerified(Boolean verificado) {
        this.verificado = verificado;
    }

    public LocalDateTime getCreatedAt() {
        return criadoEm;
    }

    public void setCreatedAt(LocalDateTime data) {
        this.criadoEm = data;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmadoEm;
    }

    public void setConfirmedAt(LocalDateTime data) {
        this.confirmadoEm = data;
    }
} 