package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Uma capacidade assistencial na unidade, com início e fim (ADR-0117) — a habilitação de um CAPS III, por exemplo, tem
 * data. Sem fim, está vigente.
 */
@Entity
@Table(name = "TB_UNIDADE_CAPACIDADE_ASSISTENCIAL", indexes = {
        @Index(name = "ix_unidade_capacidade_assistencial", columnList = "unidade_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeCapacidadeAssistencial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacidade_assistencial_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private CapacidadeAssistencial capacidade;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    /** Ex.: a portaria de habilitação, a quantidade de leitos. */
    @Column(length = 300)
    private String observacao;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
