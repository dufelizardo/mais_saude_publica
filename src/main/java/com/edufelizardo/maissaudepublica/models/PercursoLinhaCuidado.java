package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * O paciente na linha de cuidado (ADR-0152): aberto por profissional, com a data de início (a última menstruação, o
 * nascimento, a suspeita), a unidade e a equipe responsáveis. A situação das etapas é calculada; o percurso guarda só
 * a abertura e o encerramento.
 */
@Entity
@Table(name = "TB_PERCURSO_LINHA_CUIDADO", indexes = @Index(name = "ix_percurso_linha_cuidado_paciente", columnList = "paciente_id, linha_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PercursoLinhaCuidado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LinhaDeCuidado linha;

    @Column(nullable = false)
    private LocalDate inicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid")
    private Equipe equipe;

    /** O acompanhamento no programa da linha, quando houver (ADR-0119). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid")
    private AcompanhamentoProgramatico acompanhamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoPercursoLinha situacao;

    private LocalDate encerradoEm;

    @Column(length = 500)
    private String motivoEncerramento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
