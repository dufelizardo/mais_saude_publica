package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Livro de movimentação do lote de imunobiológico (ADR-0148, como o da Farmácia na ADR-0057): imutável, em doses, com o
 * saldo depois de cada lançamento. Erro se corrige com outro lançamento (ajuste ou estorno).
 */
@Entity
@Table(name = "TB_MOVIMENTACAO_IMUNOBIOLOGICO", indexes = @Index(name = "ix_movimentacao_imuno_lote", columnList = "lote_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentacaoImunobiologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LoteImunobiologico lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoMovimentacaoImunobiologico tipo;

    /** Positiva na entrada e no estorno; negativa na saída. */
    @Column(nullable = false, updatable = false)
    private int doses;

    @Column(nullable = false, updatable = false)
    private int saldoApos;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private MotivoPerdaImunobiologico motivoPerda;

    @Column(length = 500, updatable = false)
    private String justificativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_vacinacao_id", referencedColumnName = "uuid", updatable = false)
    private RegistroVacinacao registroVacinacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frasco_id", referencedColumnName = "uuid", updatable = false)
    private FrascoAberto frasco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
