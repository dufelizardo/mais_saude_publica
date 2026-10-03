package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Um turno de funcionamento da unidade num dia da semana (ADR-0101): abre e fecha no mesmo dia. Um dia pode ter mais de um
 * turno (manhã e tarde); dia sem turno é dia fechado. Substitui, para a agenda, o horário em texto livre.
 */
@Entity
@Table(name = "TB_HORARIO_UNIDADE", indexes = @Index(name = "ix_horario_unidade", columnList = "unidade_id, diaSemana"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HorarioUnidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek diaSemana;

    @Column(nullable = false)
    private LocalTime abre;

    @Column(nullable = false)
    private LocalTime fecha;
}
