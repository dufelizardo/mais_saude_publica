package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Prescrição hospitalar diária (ADR-0187): vale 24 horas a partir do início. A nova da mesma internação encerra a anterior no seu
 * início; a renovada copia os itens da anterior. A alta encerra a vigente.
 */
@Entity
@Table(name = "TB_PRESCRICAO_HOSPITALAR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PrescricaoHospitalar implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Internacao internacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional medico;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant inicio;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant fim;

    /** Quando outra prescrição ou a alta encerrou esta antes do fim. */
    private Instant encerradaEm;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private UUID renovadaDe;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;

    /** O fim efetivo: o encerramento, se veio antes do fim. */
    public Instant fimEfetivo() {
        return encerradaEm != null && encerradaEm.isBefore(fim) ? encerradaEm : fim;
    }

    public boolean vigenteEm(Instant instante) {
        return !instante.isBefore(inicio) && instante.isBefore(fimEfetivo());
    }
}
