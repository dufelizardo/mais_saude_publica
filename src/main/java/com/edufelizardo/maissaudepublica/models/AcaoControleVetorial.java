package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoControleVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoControleVetorial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Ação de controle vetorial (ADR-0162): bloqueio de transmissão, nebulização, mutirão, educativa. Ligada às microáreas e, opcionalmente, ao caso notificado ou ao surto que a motivou. */
@Entity
@Table(name = "TB_ACAO_CONTROLE_VETORIAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoControleVetorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcaoControleVetorial tipo;

    @Column(length = 1000)
    private String descricao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_ACAO_CONTROLE_VETORIAL_MICROAREA", joinColumns = @JoinColumn(name = "acao_id"), inverseJoinColumns = @JoinColumn(name = "microarea_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Microarea> microareas = new HashSet<>();

    @Column(nullable = false)
    private LocalDate dataPrevista;

    private LocalDate dataRealizada;

    @Column(nullable = false, length = 200)
    private String responsavel;

    private Integer imoveisTrabalhados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Notificacao notificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surto_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Surto surto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAcaoControleVetorial situacao;

    @Column(length = 1000)
    private String resultado;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
