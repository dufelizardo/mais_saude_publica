package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioEstoque;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Inventário de materiais de uma unidade (ADR-0189): a abertura congela os lotes com o saldo do sistema e bloqueia a movimentação; o
 * fechamento, com todos contados, lança os ajustes no livro.
 */
@Entity
@Table(name = "TB_INVENTARIO_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InventarioEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoInventarioEstoque situacao;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant abertoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aberto_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional abertoPor;

    private Instant encerradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encerrado_por_id", referencedColumnName = "uuid")
    private Profissional encerradoPor;

    @Column(length = 1000)
    private String observacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
