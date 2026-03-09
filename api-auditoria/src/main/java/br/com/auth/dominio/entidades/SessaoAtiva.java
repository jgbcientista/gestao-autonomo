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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessoes_ativas")
@EntityListeners(AuditingEntityListener.class)
public class SessaoAtiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_jwt", nullable = false)
    private String tokenJwt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "browser_info", length = 200)
    private String browserInfo;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ATIVA";

    @Column(name = "trust_score")
    private Double trustScore;

    @CreatedDate
    @Column(name = "login_time", nullable = false, updatable = false)
    private LocalDateTime loginTime;

    @LastModifiedDate
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "session_duration")
    private Double sessionDuration;

    @Column(name = "risk_level", length = 20)
    @Builder.Default
    private String riskLevel = "BAIXO";

    @PreUpdate
    protected void onUpdate() {
        if (loginTime != null && lastActivity != null) {
            double hours = java.time.Duration.between(loginTime, lastActivity).toMinutes() / 60.0;
            this.sessionDuration = Math.round(hours * 10.0) / 10.0;
        }
    }
} 