package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Leitos autorizados de uma capacidade na unidade (ADR-0171). Só inclusão: o mais recente vale, e os anteriores são o histórico. */
@Entity
@Table(name = "TB_LIMITE_LEITOS", indexes = @Index(name = "ix_limite_leitos_unidade", columnList = "unidade_id, capacidade_id, registrado_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LimiteLeitos implements Serializable {
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
    @JoinColumn(name = "capacidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CapacidadeAssistencial capacidade;

    @Column(nullable = false, updatable = false)
    private int quantidade;

    @Column(nullable = false, length = 500, updatable = false)
    private String motivo;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
