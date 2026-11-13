package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaSanitaria;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Medida sanitária (ADR-0200): a interdição cautelar de até 90 dias, a interdição da penalidade e a apreensão de produto; encerrada pela desinterdição ou pela destinação. */
@Entity
@Table(name = "TB_MEDIDA_SANITARIA", indexes = @Index(name = "ix_medida_sanitaria_estabelecimento", columnList = "estabelecimento_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProcessoSanitario processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMedidaSanitaria tipo;

    @Column(nullable = false)
    private boolean total;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(length = 200)
    private String produto;

    @Column(length = 60)
    private String lote;

    @Column(length = 60)
    private String quantidade;

    @Column(length = 200)
    private String localGuarda;

    @Column(nullable = false)
    private LocalDate inicio;

    private LocalDate fimPrevisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMedidaSanitaria situacao;

    private LocalDate encerradaEm;

    @Column(length = 1000)
    private String motivoEncerramento;

    private String encerradaPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
