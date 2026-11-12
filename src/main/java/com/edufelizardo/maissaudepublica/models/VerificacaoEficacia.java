package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Verificação de eficácia do ciclo do plano (ADR-0204): eficaz encerra a não conformidade; não eficaz abre o ciclo seguinte. */
@Entity
@Table(name = "TB_VERIFICACAO_EFICACIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VerificacaoEficacia implements Serializable {
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

    @Column(nullable = false, updatable = false)
    private boolean eficaz;

    @Column(nullable = false, updatable = false)
    private LocalDate verificadaEm;

    @Column(length = 2000, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String verificadoPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
