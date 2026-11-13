package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoViagem;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Viagem do transporte eletivo (ADR-0190): o veículo, o condutor, a janela prevista, os embarques e a quilometragem. */
@Entity
@Table(name = "TB_VIAGEM_TRANSPORTE", indexes = @Index(name = "ix_viagem_transporte_saida", columnList = "saida_prevista"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ViagemTransporte implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condutor_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional condutor;

    @Column(nullable = false)
    private Instant saidaPrevista;

    @Column(nullable = false)
    private Instant retornoPrevisto;

    @Column(nullable = false, length = 500)
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoViagem situacao;

    private Integer kmSaida;
    private Integer kmChegada;
    private Instant saidaEm;
    private Instant chegadaEm;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(length = 500)
    private String observacao;

    private String registradoPorCpf;

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EmbarqueViagem> embarques = new ArrayList<>();

    public boolean cruza(Instant de, Instant ate) {
        return saidaPrevista.isBefore(ate) && retornoPrevisto.isAfter(de);
    }
}
