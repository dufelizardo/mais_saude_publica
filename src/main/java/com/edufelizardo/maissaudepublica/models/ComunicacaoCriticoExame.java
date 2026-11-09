package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MeioComunicacaoCritico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Comunicação de valor crítico pelo laboratório (ADR-0181, RDC 786/2023): quem foi avisado, por qual meio, quando e por
 * quem. Ligada ao resultado comunicado; a retificação gera outro resultado, que pede nova comunicação se também for
 * crítico. Gravada uma vez.
 */
@Entity
@Table(name = "TB_COMUNICACAO_CRITICO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoCriticoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ItemPedidoExame item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ResultadoExame resultado;

    /** Quem recebeu a informação: o solicitante, o plantonista ou a enfermagem do setor. */
    @Column(nullable = false, length = 150, updatable = false)
    private String receptorNome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MeioComunicacaoCritico meio;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant comunicadoEm;

    /** Quem comunicou, pelo laboratório. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private String registradoPorCpf;
}
