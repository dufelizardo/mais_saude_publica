package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Participação de um profissional numa equipe, com início e fim (ADR-0103): a composição é histórico, não campo que se
 * sobrescreve. Sem fim, está vigente.
 */
@Entity
@Table(name = "TB_MEMBRO_EQUIPE", indexes = {
        @Index(name = "ix_membro_equipe_equipe", columnList = "equipe_id, fim"),
        @Index(name = "ix_membro_equipe_profissional", columnList = "profissional_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MembroEquipe implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FuncaoEquipe funcao;

    /** Microárea do agente comunitário. */
    @Column(length = 20, updatable = false)
    private String microarea;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoSaida;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
