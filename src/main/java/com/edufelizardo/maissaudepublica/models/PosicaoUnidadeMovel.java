package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Passagem da unidade móvel por um ponto (ADR-0166), informada por quem opera; a última é a localização atual. Sem GPS. */
@Entity
@Table(name = "TB_POSICAO_UNIDADE_MOVEL", indexes = @Index(name = "ix_posicao_movel_unidade", columnList = "unidade_id, desde"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PosicaoUnidadeMovel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ponto_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PontoOperacional ponto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime desde;

    @Column(length = 300, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
