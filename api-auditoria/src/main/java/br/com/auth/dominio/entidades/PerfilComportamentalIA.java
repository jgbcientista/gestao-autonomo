package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "perfil_comportamental_ia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilComportamentalIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "ip_acesso", nullable = false)
    private String ipAcesso;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(name = "data_hora_acesso", nullable = false)
    private LocalDateTime dataHoraAcesso;

    @Column(name = "horario_acesso")
    private LocalDateTime horarioAcesso;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_dia")
    private Integer horaDia;

    @Column(name = "media_sessoes_diarias")
    private Double mediaSessoesDiarias;

    @Column(name = "desvio_padrao_horarios")
    private Double desvioPadraoHorarios;

    @Column(name = "localizacao_geografica")
    private String localizacaoGeografica;

    @Column(name = "dispositivo_usual")
    private String dispositivoUsual;

    @Column(name = "padrao_horario")
    private String padraoHorario;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_acesso", nullable = false)
    private ClassificacaoAcesso classificacaoAcesso;

    @Column(name = "score_anomalia", nullable = false)
    private Double scoreAnomalia;

    @Enumerated(EnumType.STRING)
    @Column(name = "algoritmo_utilizado", nullable = false)
    private AlgoritmoIA algoritmoUtilizado;

    @Column(name = "confianca_predicao", nullable = false)
    private Double confiancaPredicao;

    // Características comportamentais
    @Column(name = "padrao_horario_score")
    private Double padraoHorarioScore;

    @Column(name = "padrao_localizacao_score")
    private Double padraoLocalizacaoScore;

    @Column(name = "padrao_dispositivo_score")
    private Double padraoDispositivoScore;

    @Column(name = "frequencia_acesso_score")
    private Double frequenciaAcessoScore;

    @Column(name = "sequencia_navegacao_score")
    private Double sequenciaNavegacaoScore;

    // Dados de contexto
    @Column(name = "endereco_ip")
    private String enderecoIp;

    @Column(name = "total_ips_distintos")
    private Integer totalIpsDistintos;

    @Column(name = "total_dispositivos_distintos")
    private Integer totalDispositivosDistintos;

    // Resultados dos algoritmos
    @Column(name = "isolation_forest_score")
    private Double isolationForestScore;

    @Column(name = "random_forest_score")
    private Double randomForestScore;

    @Column(name = "deep_learning_score")
    private Double deepLearningScore;

    @Column(name = "ensemble_score")
    private Double ensembleScore;

    @Column(name = "keystroke_similarity_score")
    private Double keystrokeSimilarityScore;

    // Metadados
    @Column(name = "versao_modelo")
    private String versaoModelo;

    @Column(name = "tempo_processamento_ms")
    private Long tempoProcessamentoMs;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public enum ClassificacaoAcesso {
        ESPERADO,
        ANOMALO,
        SUSPEITO,
        ALTAMENTE_SUSPEITO
    }

    public enum AlgoritmoIA {
        ISOLATION_FOREST,
        RANDOM_FOREST,
        DEEP_LEARNING,
        ENSEMBLE,
        NAIVE_BAYES,
        SVM
    }

    // Getters e setters específicos para manter compatibilidade
    public void setHorarioAcesso(LocalDateTime horario) {
        this.horarioAcesso = horario;
        this.dataHoraAcesso = horario; // Mantém os dois campos sincronizados
    }

    public LocalDateTime getHorarioAcesso() {
        return this.horarioAcesso != null ? this.horarioAcesso : this.dataHoraAcesso;
    }
} 