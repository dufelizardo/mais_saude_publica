package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Classificação do caso (ADR-0124), como histórico: suspeito, provável, confirmado, descartado ou inconclusivo.
 */
@Entity
@Table(name = "TB_CLASSIFICACAO_CASO", indexes = @Index(name = "ix_classificacao_caso", columnList = "notificacao_id, classificada_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ClassificacaoCaso implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Notificacao notificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ClassificacaoCasoEpidemiologico classificacao;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private CriterioClassificacao criterio;

    @Column(length = 1000, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant classificadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", updatable = false)
    private Profissional responsavel;

    @Column(updatable = false)
    private String registradoPorCpf;
}
