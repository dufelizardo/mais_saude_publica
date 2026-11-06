package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lote de imunobiológico da sala de vacina (ADR-0148): o saldo em doses é o corrente; o livro de movimentação guarda o
 * histórico. Sob suspeita (excursão de temperatura), não se aplica até a avaliação da coordenação.
 */
@Entity
@Table(name = "TB_LOTE_IMUNOBIOLOGICO", uniqueConstraints = @UniqueConstraint(name = "uk_lote_imuno_unidade_numero", columnNames = {"unidade_id", "imunobiologico_id", "numero_lote"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoteImunobiologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid", nullable = false)
    private Imunobiologico imunobiologico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @Column(nullable = false, length = 40)
    private String numeroLote;

    @Column(length = 100)
    private String fabricante;

    @Column(nullable = false)
    private LocalDate validade;

    /** Saldo corrente em doses. */
    @Column(nullable = false)
    private int doses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camara_id", referencedColumnName = "uuid")
    private CamaraRefrigeracao camara;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoLoteImunobiologico situacao;

    private Instant sobSuspeitaDesde;

    @Column(length = 500)
    private String parecer;

    private Instant avaliadoEm;

    private String avaliadoPorCpf;
}
