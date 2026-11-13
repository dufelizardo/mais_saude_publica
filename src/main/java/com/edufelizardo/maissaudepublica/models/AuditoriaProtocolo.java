package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Auditoria de adesão a um protocolo (ADR-0205): cada item conforme, não conforme ou não se aplica; a adesão sai dos totais. */
@Entity
@Table(name = "TB_AUDITORIA_PROTOCOLO", indexes = @Index(name = "ix_auditoria_protocolo_unidade", columnList = "unidade_id, data_auditoria"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AuditoriaProtocolo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "versao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VersaoChecklistProtocolo versao;

    @Column(nullable = false, updatable = false)
    private LocalDate dataAuditoria;

    /** {"codigo": "CONFORME" | "NAO_CONFORME" | "NAO_SE_APLICA"} */
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String respostas;

    @Column(nullable = false, updatable = false)
    private int conformes;

    @Column(nullable = false, updatable = false)
    private int aplicaveis;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String auditorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
