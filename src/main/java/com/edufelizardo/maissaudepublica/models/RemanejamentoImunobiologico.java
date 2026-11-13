package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoRemanejamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRemanejamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Remanejamento de doses de um lote para a sala de vacina de outra unidade (ADR-0206): a origem envia, o destino recebe (com a temperatura de chegada) ou recusa; as doses ficam em trânsito, fora dos dois estoques. */
@Entity
@Table(name = "TB_REMANEJAMENTO_IMUNOBIOLOGICO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RemanejamentoImunobiologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LoteImunobiologico loteOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidadeDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_destino_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LoteImunobiologico loteDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camara_destino_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CamaraRefrigeracao camaraDestino;

    @Column(nullable = false, updatable = false)
    private int doses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoRemanejamento motivo;

    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoRemanejamento situacao;

    @Column(nullable = false, updatable = false)
    private Instant enviadoEm;

    @Column(updatable = false)
    private String enviadoPorCpf;

    private Instant concluidoEm;

    private String concluidoPorCpf;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperaturaChegada;

    @Column(length = 500)
    private String motivoRecusa;
}
