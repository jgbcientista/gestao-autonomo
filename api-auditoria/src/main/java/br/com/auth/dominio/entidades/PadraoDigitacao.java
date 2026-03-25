package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "padrao_digitacao")
public class PadraoDigitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tempos_entre_teclas", columnDefinition = "TEXT")
    private String temposEntreTeclas;

    @Column(name = "tempo_medio_hold")
    private Double tempoMedioHold;

    @Column(name = "tempo_medio_flight")
    private Double tempoMedioFlight;

    @Column(name = "desvio_padrao_hold")
    private Double desvioPadraoHold;

    @Column(name = "desvio_padrao_flight")
    private Double desvioPadraoFlight;

    @Column(name = "total_amostras")
    private Integer totalAmostras;

    @Builder.Default
    @Column(name = "eh_baseline")
    private Boolean ehBaseline = false;

    @Column(name = "score_similaridade")
    private Double scoreSimilaridade;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
