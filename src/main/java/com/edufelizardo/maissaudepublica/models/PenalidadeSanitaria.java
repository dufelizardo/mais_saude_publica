package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoPenalidadeSanitaria;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Penalidade da decisão do processo sanitário (art. 2º da Lei 6.437/1977; ADR-0200). Revogada no recurso provido; vale só no encerramento. */
@Entity
@Table(name = "TB_PENALIDADE_SANITARIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PenalidadeSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProcessoSanitario processo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPenalidadeSanitaria tipo;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorMulta;

    private LocalDate suspensaoAte;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean revogada;
}
