package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Uma mudança de situação do veículo (ADR-0178): só inclusão. */
@Entity
@Table(name = "TB_SITUACAO_VEICULO", indexes = @Index(name = "ix_situacao_veiculo", columnList = "veiculo_id, inicio"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoVeiculoRegistro implements Serializable {
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private SituacaoVeiculo situacao;

    @Column(nullable = false, updatable = false)
    private Instant inicio;

    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(updatable = false)
    private String registradoPorCpf;
}
