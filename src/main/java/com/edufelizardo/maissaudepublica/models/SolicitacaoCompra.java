package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Solicitação de compra, o documento de formalização da demanda (Lei 14.133/2021, art. 12, VII; ADR-0212). */
@Entity
@Table(name = "TB_SOLICITACAO_COMPRA", indexes = @Index(name = "ix_solicitacao_compra_unidade", columnList = "unidade_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoCompra implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @Column(nullable = false, length = 2000)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.PrioridadeCompra prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra situacao;

    /** O motivo da devolução, da rejeição ou do cancelamento. */
    @Column(length = 1000)
    private String motivo;

    private Instant enviadaEm;

    private Instant analisadaEm;

    private String analisadaPorCpf;

    private Instant pesquisaConcluidaEm;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
