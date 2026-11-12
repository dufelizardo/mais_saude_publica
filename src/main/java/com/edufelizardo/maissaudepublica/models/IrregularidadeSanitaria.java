package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIrregularidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Irregularidade encontrada na inspeção (ADR-0199), com o prazo para corrigir (a intimação). Corrigida só na inspeção de retorno; autuada no processo (V3b). */
@Entity
@Table(name = "TB_IRREGULARIDADE_SANITARIA", indexes = @Index(name = "ix_irregularidade_sanitaria_estabelecimento", columnList = "estabelecimento_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IrregularidadeSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Column(length = 40, updatable = false)
    private String itemCodigo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(length = 500)
    private String enquadramento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeIrregularidade gravidade;

    private LocalDate prazoCorrecao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoIrregularidade situacao;

    private LocalDate corrigidaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_correcao_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecaoCorrecao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
