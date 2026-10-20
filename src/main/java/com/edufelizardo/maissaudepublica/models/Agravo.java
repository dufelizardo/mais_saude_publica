package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeNotificacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Agravo de notificação (ADR-0124, desenho na ADR-0110): catálogo como dado, sem tabela por doença. Diz os CID-10 que
 * cobre (prefixos: "A90" cobre A90 e A90.x), se é compulsório, a periodicidade, o prazo de encerramento, os critérios de
 * classificação aceitos e se é sensível. A ficha específica fica em {@link FichaAgravo}, versionada.
 */
@Entity
@Table(name = "TB_AGRAVO", uniqueConstraints = @UniqueConstraint(name = "uk_agravo_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Agravo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 60, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    /** Os CID-10 (ou prefixos) que o agravo cobre, para a sugestão de notificação. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_AGRAVO_CID", joinColumns = @JoinColumn(name = "agravo_id"))
    @Column(name = "cid", length = 10, nullable = false)
    private Set<String> cids = new HashSet<>();

    @Column(nullable = false)
    private boolean compulsorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodicidadeNotificacao periodicidade;

    @Column(nullable = false)
    private int prazoEncerramentoDias;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_AGRAVO_CRITERIO", joinColumns = @JoinColumn(name = "agravo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "criterio", nullable = false)
    private Set<CriterioClassificacao> criterios = new HashSet<>();

    /** HIV/aids, sífilis, violência: só a vigilância e quem notificou veem o detalhe. */
    @Column(nullable = false)
    private boolean sensivel;

    @Column(nullable = false)
    private boolean ativo;
}
