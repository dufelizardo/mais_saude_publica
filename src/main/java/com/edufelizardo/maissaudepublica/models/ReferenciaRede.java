package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A referência estrutural da rede (GR2, ADR-0197): para onde a origem (unidade ou esfera) encaminha um procedimento regulado, com vigência.
 * Trocar a executante encerra a vigente na véspera do início da nova; o histórico fica.
 */
@Entity
@Table(name = "TB_REFERENCIA_REDE", indexes = @Index(name = "ix_referencia_rede_procedimento", columnList = "procedimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReferenciaRede implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProcedimentoRegulado procedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executante_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude executante;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    /** O último dia de vigência; nulo enquanto aberta. */
    private LocalDate fim;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;

    private Instant encerradaEm;

    private String encerradaPorCpf;

    public boolean vigenteEm(LocalDate dia) {
        return !inicio.isAfter(dia) && (fim == null || !fim.isBefore(dia));
    }
}
