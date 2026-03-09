package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dados_treinamento_ia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DadosTreinamentoIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Features para ML
    @Column(name = "hora_acesso")
    private Integer horaAcesso;

    @Column(name = "dia_semana")
    private Integer diaSemana;

    @Column(name = "duracao_sessao_minutos")
    private Integer duracaoSessaoMinutos;

    @Column(name = "numero_paginas_visitadas")
    private Integer numeroPaginasVisitadas;

    @Column(name = "tempo_entre_cliques_ms")
    private Double tempoEntreCliquesMs;

    @Column(name = "velocidade_digitacao_cpm")
    private Double velocidadeDigitacaoCpm;

    @Column(name = "frequencia_acesso_semanal")
    private Double frequenciaAcessoSemanal;

    @Column(name = "ip_ja_utilizado")
    private Boolean ipJaUtilizado;

    @Column(name = "dispositivo_ja_utilizado")
    private Boolean dispositivoJaUtilizado;

    @Column(name = "localizacao_ja_utilizada")
    private Boolean localizacaoJaUtilizada;

    @Column(name = "distancia_localizacao_habitual_km")
    private Double distanciaLocalizacaoHabitualKm;

    @Column(name = "diferenca_horario_habitual_horas")
    private Double diferencaHorarioHabitualHoras;

    @Column(name = "numero_tentativas_login")
    private Integer numeroTentativasLogin;

    @Column(name = "tempo_desde_ultimo_acesso_horas")
    private Double tempoDesdeUltimoAcessoHoras;

    @Column(name = "padroes_navegacao_score")
    private Double padroesNavegacaoScore;

    // Features de contexto
    @Column(name = "endereco_ip")
    private String enderecoIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "resolucao_tela")
    private String resolucaoTela;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "idioma_browser")
    private String idiomaBrowser;

    // Label para treinamento supervisionado
    @Enumerated(EnumType.STRING)
    @Column(name = "label_comportamento", nullable = false)
    private LabelComportamento labelComportamento;

    @Column(name = "validado_por_especialista")
    private Boolean validadoPorEspecialista;

    @Column(name = "feedback_usuario")
    private String feedbackUsuario;

    // Metadados
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "usado_para_treinamento")
    private Boolean usadoParaTreinamento;

    @Column(name = "versao_dataset")
    private String versaoDataset;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        usadoParaTreinamento = false;
        validadoPorEspecialista = false;
    }

    public enum LabelComportamento {
        NORMAL,
        ANOMALO,
        FRAUDULENTO,
        SUSPEITO
    }
} 