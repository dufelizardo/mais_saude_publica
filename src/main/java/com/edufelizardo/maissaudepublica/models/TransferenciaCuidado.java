package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaCuidado;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Transferência do cuidado para outra unidade (ADR-0122): o "retorno à UBS" ou a ida a outro CAPS. A origem propõe com
 * o resumo de contrarreferência; o destino aceita (abre o caso lá, ligado a este) ou recusa.
 */
@Entity
@Table(name = "TB_TRANSFERENCIA_CUIDADO", indexes = @Index(name = "ix_transferencia_cuidado_destino", columnList = "unidade_destino_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaCuidado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidadeDestino;

    /** Resumo de contrarreferência para o destino. Dado de saúde. */
    @Column(nullable = false, length = 2000, updatable = false)
    private String resumo;

    @Column(nullable = false, updatable = false)
    private Instant propostaEm;

    @Column(updatable = false)
    private String propostaPorCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoTransferenciaCuidado situacao;

    private Instant decididaEm;

    @Column(length = 500)
    private String motivoDecisao;

    private String decididaPorCpf;

    /** O caso aberto no destino, no aceite. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caso_destino_id", referencedColumnName = "uuid")
    private AcompanhamentoSaudeMental casoDestino;
}
