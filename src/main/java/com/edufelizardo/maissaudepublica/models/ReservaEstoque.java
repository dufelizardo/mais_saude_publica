package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Material reservado para uma emergência numa unidade (ES3, ADR-0191). Enquanto ativa, as saídas comuns não tiram do reservado; o
 * consumo e a requisição da própria emergência abatem dele.
 */
@Entity
@Table(name = "TB_RESERVA_ESTOQUE", indexes = {@Index(name = "ix_reserva_estoque_unidade", columnList = "unidade_id, material_id, situacao"),
        @Index(name = "ix_reserva_estoque_emergencia", columnList = "emergencia_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReservaEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @Column(nullable = false, updatable = false)
    private Integer quantidade;

    /** O que já saiu da reserva pelo consumo ou pela requisição da emergência. */
    @Column(nullable = false)
    private Integer consumida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoReservaEstoque situacao;

    @Column(nullable = false, length = 500, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    private Instant liberadaEm;

    @Column(length = 500)
    private String motivoLiberacao;

    @Column(updatable = false)
    private String registradoPorCpf;

    private String liberadoPorCpf;

    /** O que ainda está reservado. */
    public int restante() {
        return situacao == SituacaoReservaEstoque.ATIVA ? quantidade - consumida : 0;
    }
}
