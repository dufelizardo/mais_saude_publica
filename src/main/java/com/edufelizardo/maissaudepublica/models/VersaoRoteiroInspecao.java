package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Versão do roteiro de inspeção (ADR-0199). Só inclusão: a inspeção guarda a versão que respondeu. */
@Entity
@Table(name = "TB_VERSAO_ROTEIRO_INSPECAO", uniqueConstraints = @UniqueConstraint(name = "uk_versao_roteiro_inspecao", columnNames = {"roteiro_id", "versao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VersaoRoteiroInspecao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roteiro_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoteiroInspecao roteiro;

    @Column(nullable = false, updatable = false)
    private int versao;

    /** {"itens": [{"codigo", "texto", "gravidade"}]} */
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String definicao;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
