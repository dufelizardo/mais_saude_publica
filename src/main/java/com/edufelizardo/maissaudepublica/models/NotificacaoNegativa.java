package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Notificação negativa (ADR-0124): a unidade declara que não houve caso de agravo compulsório na semana
 * epidemiológica.
 */
@Entity
@Table(name = "TB_NOTIFICACAO_NEGATIVA", uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_negativa", columnNames = {"unidade_id", "ano", "semana"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacaoNegativa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Column(nullable = false, updatable = false)
    private int ano;

    @Column(nullable = false, updatable = false)
    private int semana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", updatable = false)
    private Profissional responsavel;

    @Column(nullable = false, updatable = false)
    private Instant declaradaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
