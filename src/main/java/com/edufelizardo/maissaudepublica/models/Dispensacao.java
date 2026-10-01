package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de dispensação de um {@link Lote} de medicamento a um {@link Paciente} — terceira
 * entidade do domínio Farmácia (ver MAPA-DE-DOMINIOS.md #9, ADR-0051). {@code consulta} é opcional:
 * nem toda dispensação nasce de uma consulta registrada no sistema (pode ser continuidade de
 * tratamento com receita em papel). <b>Sem setters de {@code lote}/{@code quantidade} usados fora
 * da criação</b> — esta entidade é create-only (ver ADR-0051): editar uma dispensação já ocorrida
 * exigiria reverter/reaplicar o efeito no estoque do lote, sem requisito concreto para isso.
 */
@Entity
@Table(name = "TB_DISPENSACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Dispensacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", referencedColumnName = "uuid", nullable = true)
    private Consulta consulta;

    @NotNull
    @Positive
    private Integer quantidade;

    @NotNull
    private LocalDateTime dataHora;

    public Dispensacao(Lote lote, Paciente paciente, Profissional profissional, Consulta consulta,
                        Integer quantidade, LocalDateTime dataHora) {
        this.lote = lote;
        this.paciente = paciente;
        this.profissional = profissional;
        this.consulta = consulta;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
    }
}
