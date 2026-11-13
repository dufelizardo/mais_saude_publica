package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Realização de um exame de imagem (ADR-0182): onde, quando, por qual técnico, em qual equipamento e com ou sem contraste.
 * Uma por exame. Gravada uma vez.
 */
@Entity
@Table(name = "TB_REALIZACAO_EXAME_IMAGEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RealizacaoExameImagem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false, unique = true)
    private ItemPedidoExame item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant realizadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional tecnico;

    /** O aparelho, em texto ("Tomógrafo da sala 2"): não há cadastro de aparelhos no sistema. */
    @Column(length = 150, updatable = false)
    private String equipamento;

    @Column(nullable = false, updatable = false)
    private boolean contraste;

    @Column(length = 200, updatable = false)
    private String contrasteDescricao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
