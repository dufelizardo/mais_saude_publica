package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Plano de tratamento odontológico (ADR-0137, desenho na ADR-0111): o tratamento acontece em várias consultas, com
 * itens previstos que os procedimentos executam. Um aberto por paciente; concluído é o "tratamento concluído".
 * O plano é vivo (ganha itens e muda de situação), então não usa retificação: cada mudança guarda quem e quando.
 */
@Entity
@Table(name = "TB_PLANO_TRATAMENTO", indexes = @Index(name = "ix_plano_tratamento_paciente", columnList = "paciente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoTratamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    /** O atendimento em que o plano foi aberto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Atendimento atendimento;

    /** A avaliação odontológica de origem, quando houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", referencedColumnName = "uuid", updatable = false)
    private AvaliacaoOdontologica avaliacao;

    /** O cirurgião-dentista responsável. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(length = 2000)
    private String diagnostico;

    @Column(length = 1000)
    private String objetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NecessidadeTratamentoOdontologico prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoPlanoTratamento situacao;

    @Column(nullable = false, updatable = false)
    private Instant abertoEm;

    @Column(updatable = false)
    private String abertoPorCpf;

    private Instant encerradoEm;

    private String encerradoPorCpf;

    @Column(length = 500)
    private String motivoEncerramento;
}
