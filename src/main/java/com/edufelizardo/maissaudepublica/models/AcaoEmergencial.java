package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoEmergencial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Ação emergencial (ADR-0158): o quadro da coordenação. Pode nascer de uma ação prevista do plano ativo (E2) ou ser avulsa. */
@Entity
@Table(name = "TB_ACAO_EMERGENCIAL", indexes = @Index(name = "ix_acao_emergencial_emergencia", columnList = "emergencia_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoEmergencial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    /** A ação prevista do plano de onde veio, e a ativação do plano; nulas na ação avulsa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acao_plano_id", referencedColumnName = "uuid", updatable = false)
    private AcaoPlano acaoPlano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativacao_id", referencedColumnName = "uuid", updatable = false)
    private AtivacaoPlano ativacao;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 200)
    private String responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid")
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeAcaoEmergencial prioridade;

    private Instant prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAcaoEmergencial situacao;

    private Instant iniciadaEm;

    private Instant concluidaEm;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** Em aberto depois do prazo. */
    public boolean atrasada(Instant agora) {
        return prazo != null && prazo.isBefore(agora) && (situacao == SituacaoAcaoEmergencial.PENDENTE || situacao == SituacaoAcaoEmergencial.EM_EXECUCAO);
    }
}
