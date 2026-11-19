package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ordem de fornecimento contra a ata ou o contrato (ADR-0214): reserva o saldo na emissão e fixa o prazo de entrega. */
@Entity
@Table(name = "TB_ORDEM_FORNECIMENTO", indexes = @Index(name = "ix_ordem_fornecimento_instrumento", columnList = "instrumento_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrdemFornecimento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentoContratual instrumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    /** O número do empenho, em texto; o empenho em si é do Financeiro (domínio 17). */
    @Column(length = 60)
    private String empenho;

    @Column(nullable = false, updatable = false)
    private LocalDate emitidaEm;

    @Column(nullable = false, updatable = false)
    private int prazoDias;

    @Column(nullable = false, updatable = false)
    private LocalDate prazoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento situacao;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
