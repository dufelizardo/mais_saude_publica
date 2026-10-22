package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Investigação da notificação (ADR-0124): uma por notificação, atualizada até o encerramento.
 */
@Entity
@Table(name = "TB_INVESTIGACAO_EPIDEMIOLOGICA", uniqueConstraints = @UniqueConstraint(name = "uk_investigacao_notificacao", columnNames = "notificacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InvestigacaoEpidemiologica implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Notificacao notificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigador_id", referencedColumnName = "uuid")
    private Profissional investigador;

    @Column(nullable = false, updatable = false)
    private Instant iniciadaEm;

    /** Entrevista, histórico, exposição e deslocamentos. */
    @Column(length = 2000)
    private String exposicao;

    @Column(length = 500)
    private String localProvavelInfeccao;

    @Column(length = 2000)
    private String medidas;

    /** Os pedidos de exame do paciente que a investigação usa (Laboratório, ADR-0093). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_INVESTIGACAO_EXAME", joinColumns = @JoinColumn(name = "investigacao_id"))
    @Column(name = "pedido_exame_id", nullable = false)
    private Set<UUID> examesIds = new HashSet<>();

    private String registradoPorCpf;
}
