package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieVetor;
import com.edufelizardo.maissaudepublica.models.enuns.FaseFoco;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoLaboratorioFoco;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoFoco;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDeposito;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Foco do vetor: um depósito positivo encontrado na vistoria (ADR-0162), com a amostra para o laboratório e o tratamento. */
@Entity
@Table(name = "TB_FOCO_VETORIAL", indexes = @Index(name = "ix_foco_vetorial_situacao", columnList = "situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FocoVetorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vistoria_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VistoriaVetorial vistoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoDeposito tipoDeposito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FaseFoco fase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EspecieVetor especieSuspeita;

    @Column(nullable = false, updatable = false)
    private boolean amostraColetada;

    @Enumerated(EnumType.STRING)
    private ResultadoLaboratorioFoco resultadoLaboratorio;

    private Instant resultadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoFoco situacao;

    private Instant encerradoEm;

    @Column(length = 500)
    private String observacao;
}
