package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que armazena o score de confiança do usuário
 * baseado no histórico de comportamento e análises de IA
 */
@Entity
@Table(name = "scores_confianca")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreConfianca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Score atual de confiança do usuário (0.0 a 1.0)
     * 0.0 = Sem confiança, 1.0 = Confiança total
     */
    @Column(name = "score_atual", nullable = false)
    private Double scoreAtual;

    /**
     * Score base calculado a partir do histórico
     */
    @Column(name = "score_base")
    private Double scoreBase;

    /**
     * Fator de ajuste baseado em comportamentos recentes
     */
    @Column(name = "fator_ajuste")
    private Double fatorAjuste;

    /**
     * Número total de logins bem-sucedidos
     */
    @Column(name = "total_logins_sucesso")
    private Integer totalLoginsSucesso;

    /**
     * Número de logins suspeitos (mas permitidos)
     */
    @Column(name = "total_logins_suspeitos")
    private Integer totalLoginsSuspeitos;

    /**
     * Número de tentativas bloqueadas
     */
    @Column(name = "total_bloqueios")
    private Integer totalBloqueios;

    /**
     * Número de vezes que MFA foi exigido
     */
    @Column(name = "total_mfa_exigido")
    private Integer totalMfaExigido;

    /**
     * Última vez que o score foi atualizado
     */
    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    /**
     * Nível de confiança baseado no score
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_confianca")
    private NivelConfianca nivelConfianca;

    /**
     * Indica se o usuário está temporariamente em observação
     */
    @Column(name = "em_observacao")
    private Boolean emObservacao;

    /**
     * Data até quando o usuário ficará em observação
     */
    @Column(name = "observacao_ate")
    private LocalDateTime observacaoAte;

    /**
     * Motivo da última alteração no score
     */
    @Column(name = "motivo_alteracao")
    private String motivoAlteracao;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /**
     * Enum para níveis de confiança
     */
    public enum NivelConfianca {
        MUITO_BAIXO,    // 0.0 - 0.2
        BAIXO,          // 0.2 - 0.4
        MEDIO,          // 0.4 - 0.6
        ALTO,           // 0.6 - 0.8
        MUITO_ALTO      // 0.8 - 1.0
    }

    /**
     * Calcula o nível de confiança baseado no score
     */
    public void calcularNivelConfianca() {
        if (scoreAtual == null) {
            this.nivelConfianca = NivelConfianca.BAIXO;
            return;
        }

        if (scoreAtual >= 0.8) {
            this.nivelConfianca = NivelConfianca.MUITO_ALTO;
        } else if (scoreAtual >= 0.6) {
            this.nivelConfianca = NivelConfianca.ALTO;
        } else if (scoreAtual >= 0.4) {
            this.nivelConfianca = NivelConfianca.MEDIO;
        } else if (scoreAtual >= 0.2) {
            this.nivelConfianca = NivelConfianca.BAIXO;
        } else {
            this.nivelConfianca = NivelConfianca.MUITO_BAIXO;
        }
    }

    /**
     * Verifica se o usuário é confiável para login automático
     */
    public boolean isConfiavel() {
        return scoreAtual != null && scoreAtual >= 0.7 && !Boolean.TRUE.equals(emObservacao);
    }

    /**
     * Verifica se requer MFA baseado no score
     */
    public boolean requerMfa() {
        return scoreAtual == null || scoreAtual < 0.5 || Boolean.TRUE.equals(emObservacao);
    }

    /**
     * Verifica se deve bloquear o acesso
     */
    public boolean deveBloquear() {
        return scoreAtual != null && scoreAtual < 0.2;
    }

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        if (ultimaAtualizacao == null) {
            ultimaAtualizacao = LocalDateTime.now();
        }
        calcularNivelConfianca();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
        ultimaAtualizacao = LocalDateTime.now();
        calcularNivelConfianca();
    }
} 