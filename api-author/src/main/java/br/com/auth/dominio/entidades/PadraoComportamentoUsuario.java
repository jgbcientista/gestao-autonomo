package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "padroes_comportamento_usuario")
@EntityListeners(AuditingEntityListener.class)
public class PadraoComportamentoUsuario {

    public enum StatusPerfilRisco {
        BAIXO, MEDIO, ALTO, CRITICO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "pontuacao_frequencia_login")
    private Double pontuacaoFrequenciaLogin;

    @Column(name = "pontuacao_consistencia_localizacao")
    private Double pontuacaoConsistenciaLocalizacao;

    @Column(name = "pontuacao_consistencia_dispositivo")
    private Double pontuacaoConsistenciaDispositivo;

    @Column(name = "pontuacao_padrao_horario")
    private Double pontuacaoPadraoHorario;

    @Column(name = "pontuacao_reputacao_ip")
    private Double pontuacaoReputacaoIp;

    @Column(name = "pontuacao_risco_geral")
    private Double pontuacaoRiscoGeral;

    @Column(name = "pontuacao_risco_global")
    private Double pontuacaoRiscoGlobal;

    @Column(name = "numero_logins_suspeitos")
    @Builder.Default
    private Integer numeroLoginsSuspeitos = 0;

    @Column(name = "ultimo_login_suspeito")
    private LocalDateTime ultimoLoginSuspeito;

    @Column(name = "numero_total_logins")
    @Builder.Default
    private Integer numeroTotalLogins = 0;

    @Column(name = "pontuacao_localizacao_acesso")
    private Double pontuacaoLocalizacaoAcesso;

    @Column(name = "pontuacao_dispositivo_acesso")
    private Double pontuacaoDispositivoAcesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_perfil_risco")
    @Builder.Default
    private StatusPerfilRisco statusPerfilRisco = StatusPerfilRisco.BAIXO;

    @ElementCollection
    @CollectionTable(name = "usuario_localizacoes_tipicas", joinColumns = @JoinColumn(name = "padrao_id"))
    @MapKeyColumn(name = "tipo_localizacao")
    @Column(name = "valor_localizacao")
    private Map<String, String> localizacoesTipicas;

    @ElementCollection
    @CollectionTable(name = "usuario_dispositivos_tipicos", joinColumns = @JoinColumn(name = "padrao_id"))
    @MapKeyColumn(name = "tipo_dispositivo")
    @Column(name = "valor_dispositivo")
    private Map<String, String> dispositivosTipicos;

    @ElementCollection
    @CollectionTable(name = "usuario_horarios_login", joinColumns = @JoinColumn(name = "padrao_id"))
    @Column(name = "frequencia_horario")
    private Map<Integer, Integer> frequenciaHorariosLogin;

    @Column(name = "total_logins")
    @Builder.Default
    private Long totalLogins = 0L;

    @Column(name = "contagem_atividades_suspeitas")
    @Builder.Default
    private Long contagemAtividadesSuspeitas = 0L;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // Métodos auxiliares para compatibilidade com código existente
    public Usuario getUser() {
        return usuario;
    }

    public void setUser(Usuario usuario) {
        this.usuario = usuario;
    }

    public Double getLoginFrequencyScore() {
        return pontuacaoFrequenciaLogin;
    }

    public void setLoginFrequencyScore(Double pontuacao) {
        this.pontuacaoFrequenciaLogin = pontuacao;
    }

    public Double getLocationConsistencyScore() {
        return pontuacaoConsistenciaLocalizacao;
    }

    public void setLocationConsistencyScore(Double pontuacao) {
        this.pontuacaoConsistenciaLocalizacao = pontuacao;
    }

    public Double getDeviceConsistencyScore() {
        return pontuacaoConsistenciaDispositivo;
    }

    public void setDeviceConsistencyScore(Double pontuacao) {
        this.pontuacaoConsistenciaDispositivo = pontuacao;
    }

    public Double getTimePatternScore() {
        return pontuacaoPadraoHorario;
    }

    public void setTimePatternScore(Double pontuacao) {
        this.pontuacaoPadraoHorario = pontuacao;
    }

    public Double getIpReputationScore() {
        return pontuacaoReputacaoIp;
    }

    public void setIpReputationScore(Double pontuacao) {
        this.pontuacaoReputacaoIp = pontuacao;
    }

    public Double getOverallRiskScore() {
        return pontuacaoRiscoGeral;
    }

    public void setOverallRiskScore(Double pontuacao) {
        this.pontuacaoRiscoGeral = pontuacao;
    }

    public Map<String, String> getTypicalLocations() {
        return localizacoesTipicas;
    }

    public void setTypicalLocations(Map<String, String> localizacoes) {
        this.localizacoesTipicas = localizacoes;
    }

    public Map<String, String> getTypicalDevices() {
        return dispositivosTipicos;
    }

    public void setTypicalDevices(Map<String, String> dispositivos) {
        this.dispositivosTipicos = dispositivos;
    }

    public Map<Integer, Integer> getLoginHoursFrequency() {
        return frequenciaHorariosLogin;
    }

    public void setLoginHoursFrequency(Map<Integer, Integer> frequencia) {
        this.frequenciaHorariosLogin = frequencia;
    }

    public Long getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(Long total) {
        this.totalLogins = total;
    }

    public Long getSuspiciousActivitiesCount() {
        return contagemAtividadesSuspeitas;
    }

    public void setSuspiciousActivitiesCount(Long contagem) {
        this.contagemAtividadesSuspeitas = contagem;
    }

    public LocalDateTime getCreatedAt() {
        return criadoEm;
    }

    public void setCreatedAt(LocalDateTime data) {
        this.criadoEm = data;
    }

    public LocalDateTime getUpdatedAt() {
        return atualizadoEm;
    }

    public void setUpdatedAt(LocalDateTime data) {
        this.atualizadoEm = data;
    }
} 