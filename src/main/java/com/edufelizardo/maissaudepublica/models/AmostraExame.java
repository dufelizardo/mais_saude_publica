package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Amostra coletada (ADR-0093): uma por material na coleta, com o código da etiqueta. A coleta diz também qual
 * laboratório vai analisar — é por ele que se decide quem analisa e libera. Amostra rejeitada não se apaga: os
 * exames dela voltam para recoleta e ela fica com o motivo.
 */
@Entity
@Table(name = "TB_AMOSTRA_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AmostraExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PedidoExame pedido;

    @NotNull
    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MaterialExame material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_coleta_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidadeColeta;

    /** Onde a amostra é analisada; por padrão, a própria unidade da coleta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude laboratorio;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime coletadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coletada_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional coletadaPor;

    @Column(updatable = false)
    private String registradoPorCpf;

    @Enumerated(EnumType.STRING)
    private MotivoRejeicaoAmostra motivoRejeicao;

    @Column(length = 500)
    private String observacaoRejeicao;

    private Instant rejeitadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejeitada_por_id", referencedColumnName = "uuid")
    private Profissional rejeitadaPor;

    public boolean isRejeitada() {
        return rejeitadaEm != null;
    }
}
