package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A contratação (ADR-0213): o processo com a modalidade, as solicitações cotadas reunidas e o resultado por item; a homologação gera os instrumentos. */
@Entity
@Table(name = "TB_CONTRATACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Contratacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 60)
    private String numeroProcesso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.ModalidadeContratacao modalidade;

    /** Na dispensa e na inexigibilidade. */
    @Column(length = 300)
    private String fundamentoLegal;

    /** Na adesão: o órgão gerenciador e o número da ata. */
    @Column(length = 200)
    private String ataOrigem;

    @Column(nullable = false, length = 1000)
    private String objeto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.SituacaoContratacao situacao;

    private LocalDate homologadaEm;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
