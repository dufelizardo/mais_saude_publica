package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Uma mudança de situação do acompanhamento, só de inclusão (ADR-0119). Sem situação anterior: a entrada. */
@Entity
@Table(name = "TB_EVENTO_ACOMPANHAMENTO", indexes = {
        @Index(name = "ix_evento_acompanhamento", columnList = "acompanhamento_id, ocorrido_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoAcompanhamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoProgramatico acompanhamento;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private SituacaoAcompanhamento situacaoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SituacaoAcompanhamento situacao;

    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(updatable = false)
    private String registradoPorCpf;
}
