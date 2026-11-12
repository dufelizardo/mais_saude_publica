package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIncidente;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Incidente de segurança do paciente (RDC 36/2013; ADR-0203): notificado por qualquer profissional, anônimo ou identificado, e analisado pelo Núcleo de Segurança do Paciente. Cultura não punitiva: o notificador só aparece para o núcleo, e na anônima não é guardado. */
@Entity
@Table(name = "TB_INCIDENTE_SEGURANCA", indexes = @Index(name = "ix_incidente_seguranca_unidade", columnList = "unidade_id, ocorrido_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IncidenteSeguranca implements Serializable {
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
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @Column(nullable = false)
    private Instant ocorridoEm;

    @Column(nullable = false, length = 200)
    private String local;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIncidente tipo;

    @Enumerated(EnumType.STRING)
    @Column
    private GrauDano danoPercebido;

    @Column(nullable = false, length = 4000, updatable = false)
    private String descricao;

    @Column(length = 2000, updatable = false)
    private String acaoImediata;

    @Column(nullable = false, updatable = false)
    private boolean anonima;

    @Column(updatable = false)
    private String notificadoPorCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoIncidente situacao;

    @Enumerated(EnumType.STRING)
    @Column
    private NaturezaIncidente natureza;

    @Enumerated(EnumType.STRING)
    @Column
    private GrauDano dano;

    @Column(nullable = false)
    private boolean neverEvent;

    /** Os fatores contribuintes, separados por ponto e vírgula. */
    @Column(length = 300)
    private String fatores;

    @Column(length = 4000)
    private String causaRaiz;

    @Column(length = 2000)
    private String licoes;

    @Column(length = 1000)
    private String motivoDescarte;

    private Instant analisadoEm;

    private String analisadoPorCpf;

    @Column(length = 40)
    private String numeroNotivisa;

    private Instant notivisaEm;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
