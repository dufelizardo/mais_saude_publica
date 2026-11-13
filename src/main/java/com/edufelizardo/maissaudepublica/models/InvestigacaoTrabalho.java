package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NexoTrabalho;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A investigação do ambiente de trabalho (ADR-0208), 1:1 com a notificação que tem o bloco do trabalho: a visita ao local, os achados,
 * o nexo concluído com a justificativa e a comunicação à vigilância sanitária.
 */
@Entity
@Table(name = "TB_INVESTIGACAO_TRABALHO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InvestigacaoTrabalho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", nullable = false, updatable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Notificacao notificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigador_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional investigador;

    private LocalDate visitaEm;

    @Column(length = 500)
    private String localVisitado;

    @Column(length = 2000)
    private String achados;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NexoTrabalho nexo;

    @Column(length = 1000)
    private String justificativaNexo;

    private Instant nexoConcluidoEm;

    private String nexoConcluidoPorCpf;

    /** A denúncia aberta na vigilância sanitária por esta investigação. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denuncia_id", referencedColumnName = "uuid", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DenunciaSanitaria denuncia;

    @Column(nullable = false, updatable = false)
    private Instant iniciadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    private Instant atualizadoEm;

    private String atualizadoPorCpf;
}
