package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deriva_comportamental")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DerivaComportamental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_analise", nullable = false)
    private LocalDateTime dataAnalise;

    @Column(name = "score_deriva", nullable = false)
    private Double scoreDeriva;

    @Column(name = "janela_curta_7d")
    private Double janelaCurta7d;

    @Column(name = "janela_longa_30d")
    private Double janelaLonga30d;

    @Column(name = "classificacao_drift", nullable = false)
    private String classificacaoDrift;

    @Column(name = "fatores_alterados", columnDefinition = "TEXT")
    private String fatoresAlterados;

    @Column(name = "detalhes_json", columnDefinition = "TEXT")
    private String detalhesJson;

    @Builder.Default
    @Column(name = "baseline_ajustado")
    private Boolean baselineAjustado = false;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
