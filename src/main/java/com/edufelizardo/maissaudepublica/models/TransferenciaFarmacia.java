package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaTransferencia;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Transferência de estoque de um {@link Lote} para outra {@link UnidadeDeSaude}, em duas etapas
 * (ADR-0059, revista pela ADR-0061):
 *
 * <ol>
 *   <li><b>Envio</b> — lança {@code TRANSFERENCIA_SAIDA} no lote de origem; a transferência fica
 *   {@code EM_TRANSITO}.</li>
 *   <li><b>Recebimento</b> — outro profissional confere no destino e lança
 *   {@code TRANSFERENCIA_ENTRADA} com a quantidade que chegou, no lote da mesma remessa na unidade de
 *   destino (ADR-0060). Se chegou menos, a diferença fica registrada aqui com motivo e justificativa
 *   ({@code RECEBIDA_COM_DIVERGENCIA}).</li>
 *   <li>Ou <b>cancelamento</b> antes do recebimento — {@code TRANSFERENCIA_ESTORNO} devolve o saldo à
 *   origem.</li>
 * </ol>
 *
 * <p>Os dados de cada etapa são gravados uma vez e não mudam depois; não há edição nem exclusão pela API.
 */
@Entity
@Table(name = "TB_TRANSFERENCIA_FARMACIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaFarmacia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    private StatusTransferenciaFarmacia status;

    // ── Envio ──────────────────────────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Lote loteOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_destino_id", referencedColumnName = "uuid", updatable = false)
    private UnidadeDeSaude unidadeDestino;

    /** Quantidade enviada. */
    @NotNull
    @Positive
    @Column(updatable = false)
    private Integer quantidade;

    /** Quem enviou. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(length = 1000, updatable = false)
    private String observacao;

    /** Hora do envio, do servidor. */
    @NotNull
    @Column(updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    // ── Recebimento ────────────────────────────────────────────────────────────────────────────────

    /** Lote da mesma remessa na unidade de destino; nulo enquanto em trânsito, se cancelada ou se nada chegou. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_destino_id", referencedColumnName = "uuid")
    private Lote loteDestino;

    private Integer quantidadeRecebida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_recebimento_id", referencedColumnName = "uuid")
    private Profissional profissionalRecebimento;

    @Enumerated(EnumType.STRING)
    private MotivoDivergenciaTransferencia motivoDivergencia;

    @Column(length = 1000)
    private String justificativaDivergencia;

    private Instant recebidoEm;

    private String recebidoPorCpf;

    // ── Cancelamento ───────────────────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_cancelamento_id", referencedColumnName = "uuid")
    private Profissional profissionalCancelamento;

    @Column(length = 1000)
    private String motivoCancelamento;

    private Instant canceladoEm;

    private String canceladoPorCpf;

    /** Envio: nasce em trânsito, sem nada do recebimento nem do cancelamento. */
    public TransferenciaFarmacia(Lote loteOrigem, UnidadeDeSaude unidadeDestino, Integer quantidade,
                                 Profissional profissional, String observacao, Instant registradoEm,
                                 String registradoPorCpf) {
        this.status = StatusTransferenciaFarmacia.EM_TRANSITO;
        this.loteOrigem = loteOrigem;
        this.unidadeDestino = unidadeDestino;
        this.quantidade = quantidade;
        this.profissional = profissional;
        this.observacao = observacao;
        this.registradoEm = registradoEm;
        this.registradoPorCpf = registradoPorCpf;
    }

    /** Diferença entre enviado e recebido; nula enquanto não recebida. */
    public Integer getQuantidadeDivergente() {
        return quantidadeRecebida == null ? null : quantidade - quantidadeRecebida;
    }
}
