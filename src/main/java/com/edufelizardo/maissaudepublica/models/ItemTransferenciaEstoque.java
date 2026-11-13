package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaEstoque;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Item da transferência (ADR-0189): o lote de origem, o enviado, o recebido e a divergência, com o lote que ele virou no destino. */
@Entity
@Table(name = "TB_ITEM_TRANSFERENCIA_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemTransferenciaEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private TransferenciaEstoque transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private MaterialEstoque material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LoteMaterial loteOrigem;

    @Column(length = 60, updatable = false)
    private String numeroLote;

    @Column(updatable = false)
    private LocalDate validade;

    @Column(precision = 12, scale = 4, updatable = false)
    private BigDecimal custoUnitario;

    @Column(nullable = false, updatable = false)
    private Integer quantidadeEnviada;

    private Integer quantidadeRecebida;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MotivoDivergenciaEstoque motivoDivergencia;

    @Column(length = 500)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_destino_id", referencedColumnName = "uuid")
    private LoteMaterial loteDestino;
}
