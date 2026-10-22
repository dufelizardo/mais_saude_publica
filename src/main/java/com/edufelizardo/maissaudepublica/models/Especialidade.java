package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Especialidade do atendimento (ADR-0134, desenho na ADR-0111): clínica geral, enfermagem, odontologia, pediatria…
 * Catálogo cadastrável, para todas as especialidades. A marca de odontológica habilita os registros da Saúde Bucal no
 * atendimento (a avaliação odontológica, e depois o odontograma e o plano).
 */
@Entity
@Table(name = "TB_ESPECIALIDADE", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Especialidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Habilita os registros da Saúde Bucal no atendimento. */
    private boolean odontologica;

    private boolean ativa;
}
