package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Recebimento de uma entrega da ordem (art. 140; ADR-0214): provisório com a nota fiscal, definitivo com o aceite item a item. */
@Entity
@Table(name = "TB_RECEBIMENTO_COMPRA", indexes = @Index(name = "ix_recebimento_compra_ordem", columnList = "ordem_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecebimentoCompra implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdemFornecimento ordem;

    @Column(nullable = false, length = 60, updatable = false)
    private String notaFiscal;

    @Column(nullable = false, updatable = false)
    private LocalDate notaEmitidaEm;

    @Column(nullable = false, updatable = false)
    private LocalDate recebidoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.SituacaoRecebimentoCompra situacao;

    private LocalDate definitivoEm;

    private String atestadoPorCpf;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
