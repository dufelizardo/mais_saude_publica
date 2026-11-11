package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemEntradaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Lançamento do livro do estoque (ADR-0177): a variação do saldo do lote e o saldo depois dela. Só inclusão. */
@Entity
@Table(name = "TB_MOVIMENTACAO_ESTOQUE", indexes = @Index(name = "ix_movimentacao_estoque_lote", columnList = "lote_id, registrado_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentacaoEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LoteMaterial lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private TipoMovimentacaoEstoque tipo;

    /** Positiva na entrada, negativa na saída; no ajuste, a diferença contada. */
    @Column(nullable = false, updatable = false)
    private Integer quantidade;

    @Column(nullable = false, updatable = false)
    private Integer saldoApos;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, updatable = false)
    private OrigemEntradaEstoque origem;

    /** Número da nota fiscal ou do termo de doação. */
    @Column(length = 60, updatable = false)
    private String documento;

    /** Em texto até o domínio de Compras (#14). */
    @Column(length = 150, updatable = false)
    private String fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, updatable = false)
    private MotivoPerdaEstoque motivoPerda;

    @Column(length = 500, updatable = false)
    private String justificativa;

    /** A transferência que originou a saída, a entrada ou o estorno (ADR-0189). */
    @Column(updatable = false)
    private UUID transferenciaId;

    /** O consumo extraordinário de uma emergência (ES3, ADR-0191). */
    @Column(name = "emergencia_id")
    private UUID emergenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
