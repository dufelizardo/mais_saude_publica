package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Versão do checklist de protocolo (ADR-0205). Só inclusão: a auditoria guarda a versão que respondeu. */
@Entity
@Table(name = "TB_VERSAO_CHECKLIST_PROTOCOLO", uniqueConstraints = @UniqueConstraint(name = "uk_versao_checklist_protocolo", columnNames = {"checklist_id", "versao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VersaoChecklistProtocolo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChecklistProtocolo checklist;

    @Column(nullable = false, updatable = false)
    private int versao;

    /** {"itens": [{"codigo", "texto"}]} */
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String definicao;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
