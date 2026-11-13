package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoQualidade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ação do plano 5W2H da não conformidade (ADR-0204): o quê, por quê, quem, onde, quando, como e quanto custa, no ciclo do plano. */
@Entity
@Table(name = "TB_ACAO_QUALIDADE", indexes = @Index(name = "ix_acao_qualidade_nc", columnList = "nao_conformidade_id, ciclo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoQualidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NaoConformidade naoConformidade;

    @Column(nullable = false, updatable = false)
    private int ciclo;

    @Column(name = "o_que", nullable = false, length = 500)
    private String oQue;

    @Column(name = "por_que", nullable = false, length = 500)
    private String porQue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional responsavel;

    @Column(length = 200)
    private String onde;

    @Column(nullable = false)
    private LocalDate prazo;

    @Column(length = 1000)
    private String como;

    @Column(precision = 14, scale = 2)
    private BigDecimal custo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAcaoQualidade situacao;

    @Column(length = 2000)
    private String evidencia;

    private LocalDate concluidaEm;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
