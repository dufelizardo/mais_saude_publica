package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Livro de movimentação do estoque de um {@link Lote} (ADR-0057). Todo aumento ou redução de
 * {@code Lote.quantidade} passa por aqui, com quem, quando e por quê — {@code Lote.quantidade} é só
 * o saldo corrente, e {@code saldoApos} de cada lançamento permite reconstruir o histórico.
 *
 * <p>Imutável: não há edição nem exclusão pela API. Um lançamento errado se corrige com outro
 * lançamento de ajuste de inventário.
 */
@Entity
@Table(name = "TB_MOVIMENTACAO_FARMACIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentacaoFarmacia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Lote lote;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private TipoMovimentacaoFarmacia tipo;

    /** Variação do saldo: positiva em entradas, negativa em saídas; no ajuste, a diferença contada. */
    @NotNull
    @Column(updatable = false)
    private Integer quantidade;

    @NotNull
    @Column(updatable = false)
    private Integer saldoApos;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private MotivoPerda motivoPerda;

    @Column(length = 1000, updatable = false)
    private String justificativa;

    /** Responsável pela operação. Nulo só no saldo inicial e em entradas cadastradas sem responsável. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensacao_id", referencedColumnName = "uuid", updatable = false)
    private Dispensacao dispensacao;

    /** Hora do servidor, nunca informada pelo cliente. */
    @NotNull
    @Column(updatable = false)
    private Instant registradoEm;

    /** CPF do usuário autenticado quando o toggle de segurança está ligado (ADR-0055); nulo quando não. */
    @Column(updatable = false)
    private String registradoPorCpf;
}
