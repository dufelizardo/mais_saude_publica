package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Transferência de estoque de um {@link Lote} para outra {@link UnidadeDeSaude} (ADR-0059). O saldo
 * não muda aqui: a transferência gera dois lançamentos no livro de movimentação
 * ({@code TRANSFERENCIA_SAIDA} na origem e {@code TRANSFERENCIA_ENTRADA} no destino), ligados a ela.
 * O lote de destino é o da mesma remessa (medicamento, número e validade) na unidade de destino,
 * criado com saldo zero quando ainda não existe.
 *
 * <p>Imutável, como os lançamentos do livro: não há edição nem exclusão pela API.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_origem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Lote loteOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Lote loteDestino;

    @NotNull
    @Positive
    @Column(updatable = false)
    private Integer quantidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(length = 1000, updatable = false)
    private String observacao;

    /** Hora do servidor, nunca informada pelo cliente. */
    @NotNull
    @Column(updatable = false)
    private Instant registradoEm;

    /** CPF do usuário autenticado quando o toggle de segurança está ligado (ADR-0055); nulo quando não. */
    @Column(updatable = false)
    private String registradoPorCpf;
}
