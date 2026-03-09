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
@Table(name = "log_auditoria")
@EntityListeners(AuditingEntityListener.class)
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "endereco_ip", nullable = false)
    private String enderecoIp;

    @Column(name = "agente_usuario")
    private String agenteUsuario;

    @Column
    private String localizacao;

    @Column(name = "info_dispositivo")
    private String infoDispositivo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private boolean sucesso;

    @Column(name = "motivo_falha")
    private String motivoFalha;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // Métodos auxiliares para compatibilidade com código existente
    public Usuario getUser() {
        return usuario;
    }

    public void setUser(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEventType() {
        return tipoEvento;
    }

    public void setEventType(String tipo) {
        this.tipoEvento = tipo;
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

    public Boolean getSuccess() {
        return sucesso;
    }

    public void setSuccess(Boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getFailureReason() {
        return motivoFalha;
    }

    public void setFailureReason(String motivo) {
        this.motivoFalha = motivo;
    }

    public String getUserAgent() {
        return agenteUsuario;
    }

    public void setUserAgent(String agente) {
        this.agenteUsuario = agente;
    }

    public String getDeviceInfo() {
        return infoDispositivo;
    }

    public void setDeviceInfo(String info) {
        this.infoDispositivo = info;
    }
} 