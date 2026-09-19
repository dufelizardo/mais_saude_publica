package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Avaliação de desempenho de um {@link Profissional} num {@link CicloAvaliacao} (ver
 * docs/rh/MODELO-RH.md, seção 11) — última fase do roadmap de RH.
 */
@Entity
@Table(name = "TB_AVALIACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Avaliacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_avaliacao_id", referencedColumnName = "uuid", nullable = false)
    private CicloAvaliacao ciclo;

    @NotBlank
    private String avaliador;

    private BigDecimal nota;

    private String observacao;

    public Avaliacao(Profissional profissional, CicloAvaliacao ciclo, String avaliador, BigDecimal nota, String observacao) {
        this.profissional = profissional;
        this.ciclo = ciclo;
        this.avaliador = avaliador;
        this.nota = nota;
        this.observacao = observacao;
    }
}
