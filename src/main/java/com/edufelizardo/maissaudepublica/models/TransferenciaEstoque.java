package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaEstoque;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Transferência de materiais entre unidades em duas etapas (ADR-0189), como na Farmácia (ADRs 0059 e 0061): o envio tira dos lotes
 * da origem; o recebimento confere item a item e dá entrada no destino, com a divergência voltando à origem ou baixada como perda.
 */
@Entity
@Table(name = "TB_TRANSFERENCIA_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude destino;

    /** A requisição atendida; nula na transferência avulsa. */
    @Column(updatable = false)
    private UUID requisicaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoTransferenciaEstoque situacao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant enviadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional enviadoPor;

    private Instant recebidaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebido_por_id", referencedColumnName = "uuid")
    private Profissional recebidoPor;

    @Column(updatable = false)
    private String registradoPorCpf;
}
