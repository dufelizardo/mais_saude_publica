package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoBuscaAtiva;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ação programática (ADR-0132, desenho na ADR-0109): atividade concreta prevista num programa, realizada pelos serviços —
 * a consulta programada, a aferição, a busca ativa do faltoso, o grupo de hipertensos. Individual (do acompanhamento do
 * paciente) ou coletiva (com os participantes). Realizada, guarda o atendimento que a cumpriu; quem confirma é o
 * profissional.
 */
@Entity
@Table(name = "TB_ACAO_PROGRAMATICA", indexes = {
        @Index(name = "ix_acao_programa", columnList = "programa_id, data_prevista"),
        @Index(name = "ix_acao_unidade", columnList = "unidade_id, data_prevista"),
        @Index(name = "ix_acao_paciente", columnList = "paciente_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoProgramatica implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** Do programa, ou da campanha (ADR-0133): um dos dois. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid", updatable = false)
    private Programa programa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", referencedColumnName = "uuid", updatable = false)
    private Campanha campanha;

    /** Ação da campanha: o local onde acontece. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_campanha_id", referencedColumnName = "uuid", updatable = false)
    private LocalCampanha localCampanha;

    /** Coletiva realizada: quantas pessoas foram atendidas (o andamento da meta da campanha). */
    private Integer quantidadeAtendida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoAcaoProgramatica tipo;

    /** Individual: o acompanhamento vigente do paciente no programa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", updatable = false)
    private AcompanhamentoProgramatico acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", updatable = false)
    private Paciente paciente;

    /** A unidade que realiza (a do acompanhamento, na individual): o escopo de acesso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid")
    private Equipe equipe;

    /** Coletiva: onde acontece. */
    @Column(length = 200)
    private String local;

    @Column(length = 1000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid")
    private Profissional responsavel;

    @Column(nullable = false)
    private LocalDate dataPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAcaoProgramatica situacao;

    private LocalDate realizadaEm;

    /** O atendimento que cumpriu a ação (do mesmo paciente). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid")
    private Atendimento atendimento;

    /** A marcação na Agenda feita a partir da ação. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", referencedColumnName = "uuid")
    private Agendamento agendamento;

    /** Não realizada ou cancelada: por quê. */
    @Column(length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    private ResultadoBuscaAtiva resultadoBusca;

    @Column(length = 1000)
    private String observacao;

    /** Coletiva: os participantes (acompanhados no programa). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_ACAO_PROGRAMATICA_PARTICIPANTE", joinColumns = @JoinColumn(name = "acao_id"),
            inverseJoinColumns = @JoinColumn(name = "paciente_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Paciente> participantes = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
