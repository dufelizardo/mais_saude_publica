package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoItemPlanoTratamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Item do plano de tratamento (ADR-0137): o procedimento previsto do catálogo, no dente e nas faces, e a situação. O
 * procedimento que o executa aponta para ele: realizado, o item fica concluído.
 */
@Entity
@Table(name = "TB_ITEM_PLANO_TRATAMENTO", indexes = @Index(name = "ix_item_plano_tratamento_plano", columnList = "plano_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemPlanoTratamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PlanoTratamento plano;

    /** A sessão sugerida, para ordenar. */
    private int ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_catalogo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ProcedimentoCatalogo procedimentoPrevisto;

    /** Notação FDI; nulo quando o item do catálogo não leva dente. */
    @Column(updatable = false)
    private Integer dente;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_ITEM_PLANO_TRATAMENTO_FACE", joinColumns = @JoinColumn(name = "item_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "face", nullable = false)
    private Set<FaceDente> faces = new LinkedHashSet<>();

    @Column(length = 500, updatable = false)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoItemPlanoTratamento situacao;

    /** O procedimento que executa o item (agendado ou realizado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid")
    private Procedimento procedimento;

    /** A solicitação de regulação do encaminhamento, quando houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_regulacao_id", referencedColumnName = "uuid")
    private SolicitacaoRegulacao solicitacaoRegulacao;

    @Column(length = 500)
    private String motivoSituacao;

    private Instant situacaoAlteradaEm;

    private String situacaoAlteradaPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(updatable = false)
    private String criadoPorCpf;
}
