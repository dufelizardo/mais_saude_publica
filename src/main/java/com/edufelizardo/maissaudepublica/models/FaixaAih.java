package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Faixa de numeração de AIH recebida do gestor (ADR-0186): os 12 primeiros dígitos, do início ao fim, e o próximo a usar. O número
 * completo leva o dígito verificador (módulo 11). Sem faixas sobrepostas.
 */
@Entity
@Table(name = "TB_FAIXA_AIH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FaixaAih implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, updatable = false)
    private Integer ano;

    @Column(nullable = false, updatable = false)
    private Long inicio;

    @Column(nullable = false, updatable = false)
    private Long fim;

    @Column(nullable = false)
    private Long proximo;

    private boolean ativa;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;

    public boolean esgotada() {
        return proximo > fim;
    }

    public long restantes() {
        return Math.max(0, fim - proximo + 1);
    }
}
