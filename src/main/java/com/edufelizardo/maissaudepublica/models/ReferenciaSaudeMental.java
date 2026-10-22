package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipe e técnico de referência de um caso em saúde mental (ADR-0120), como histórico: trocar a referência fecha a
 * anterior. A equipe é da unidade do caso e o técnico é membro vigente dela. Não é a lotação do profissional, que
 * continua no RH.
 */
@Entity
@Table(name = "TB_REFERENCIA_SAUDE_MENTAL", indexes = {
        @Index(name = "ix_referencia_saude_mental_acomp", columnList = "acompanhamento_id, fim"),
        @Index(name = "ix_referencia_saude_mental_equipe", columnList = "equipe_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReferenciaSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional tecnico;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    /** Por que a referência mudou; na abertura do caso, nulo. */
    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
