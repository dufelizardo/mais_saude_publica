package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ocorrência com o fornecedor na execução (ADR-0214): encerrada com a resposta ou convertida em sanção (ADR-0212). */
@Entity
@Table(name = "TB_OCORRENCIA_FORNECEDOR", indexes = @Index(name = "ix_ocorrencia_fornecedor", columnList = "fornecedor_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaFornecedor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdemFornecimento ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.TipoOcorrenciaFornecedor tipo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDate registradaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaFornecedor situacao;

    @Column(length = 1000)
    private String resposta;

    private LocalDate encerradaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sancao_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SancaoFornecedor sancao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
