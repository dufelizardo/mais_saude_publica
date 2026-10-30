package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEstruturaTemporaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEstruturaTemporaria;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Estrutura temporária de atendimento da emergência (ADR-0160): posto médico, tenda de hidratação, hospital de campanha.
 * É capacidade, não unidade: a unidade de referência responde por escala, estoque e leitos.
 */
@Entity
@Table(name = "TB_ESTRUTURA_TEMPORARIA", indexes = @Index(name = "ix_estrutura_temporaria_emergencia", columnList = "emergencia_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstruturaTemporaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEstruturaTemporaria tipo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, length = 300)
    private String local;

    private Double latitude;

    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_referencia_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidadeReferencia;

    @Column(nullable = false)
    private int leitos;

    private Integer atendimentosDia;

    @Column(nullable = false)
    private int ambulancias;

    @Column(length = 200)
    private String horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEstruturaTemporaria situacao;

    private Instant ativadaEm;

    private Instant desativadaEm;

    @Column(length = 500)
    private String motivoDesativacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
