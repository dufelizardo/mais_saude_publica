package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Oferta assistencial (ADR-0179): quantas vagas de um procedimento regulado a unidade executante oferta na competência (o mês, guardado
 * no dia 1). Uma por executante, procedimento e competência. As cotas da PPI repartem a oferta.
 */
@Entity
@Table(name = "TB_OFERTA_ASSISTENCIAL", uniqueConstraints = @UniqueConstraint(name = "uk_oferta_assistencial",
        columnNames = {"unidade_executante_id", "procedimento_id", "competencia"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OfertaAssistencial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_executante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude executante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProcedimentoRegulado procedimento;

    /** O primeiro dia do mês. */
    @Column(nullable = false, updatable = false)
    private LocalDate competencia;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String atualizadaPorCpf;
}
