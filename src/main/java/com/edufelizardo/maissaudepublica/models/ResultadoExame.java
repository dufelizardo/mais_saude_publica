package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Um resultado registrado para um exame do pedido (ADR-0093). Nunca é editado: registrar de novo antes da liberação,
 * ou retificar depois dela, grava outro resultado; o item aponta para o atual, e a retificação aponta para o que
 * corrigiu. A faixa de referência e a unidade são copiadas do catálogo no registro.
 */
@Entity
@Table(name = "TB_RESULTADO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ItemPedidoExame item;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal valorNumerico;

    @Column(length = 2000, updatable = false)
    private String valorTexto;

    @Column(length = 30, updatable = false)
    private String unidadeMedida;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal referenciaMinima;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal referenciaMaxima;

    @Column(length = 200, updatable = false)
    private String referenciaTexto;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private InterpretacaoResultado interpretacao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional analisadoPor;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private ResultadoExame retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    /** Liberação: quem responde tecnicamente pelo resultado. Gravada uma vez. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liberado_por_id", referencedColumnName = "uuid")
    private Profissional liberadoPor;

    private Instant liberadoEm;
}
