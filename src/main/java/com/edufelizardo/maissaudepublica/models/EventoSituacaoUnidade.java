package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Mudança de situação operacional da unidade, com motivo e quem mudou (ADR-0101). Só inclusão. */
@Entity
@Table(name = "TB_EVENTO_SITUACAO_UNIDADE", indexes = @Index(name = "ix_evento_situacao_unidade", columnList = "unidade_id, ocorridoEm"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoSituacaoUnidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SituacaoOperacional situacaoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SituacaoOperacional situacao;

    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(updatable = false)
    private LocalDate previsaoRetorno;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
