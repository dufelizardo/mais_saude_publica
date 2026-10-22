package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ACS de uma microárea, com início e fim (ADR-0125). Liga a microárea à participação do ACS na equipe responsável pela
 * área ({@link MembroEquipe}, função ACS): um ACS vigente por microárea e uma microárea vigente por ACS.
 */
@Entity
@Table(name = "TB_ATRIBUICAO_MICROAREA", indexes = {
        @Index(name = "ix_atribuicao_microarea", columnList = "microarea_id, fim"),
        @Index(name = "ix_atribuicao_membro", columnList = "membro_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtribuicaoMicroarea implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Microarea microarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private MembroEquipe membro;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    /** Por que terminou: troca, saída da equipe, redivisão. */
    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
