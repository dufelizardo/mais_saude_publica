package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.OrigemAdscricao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Adscrição do paciente a uma equipe, com início e fim (ADR-0128): quem responde por ele na atenção primária. Uma
 * vigente por paciente. Pelo território, segue o domicílio (e a microárea dele); manual, tem motivo.
 */
@Entity
@Table(name = "TB_ADSCRICAO", indexes = {
        @Index(name = "ix_adscricao_paciente", columnList = "paciente_id, fim"),
        @Index(name = "ix_adscricao_equipe", columnList = "equipe_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Adscricao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Equipe equipe;

    /** Pelo território, a microárea do domicílio; manual, nula. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid", updatable = false)
    private Microarea microarea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrigemAdscricao origem;

    /** Por que a adscrição manual. */
    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
