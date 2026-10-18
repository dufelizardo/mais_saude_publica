package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * O acompanhamento de um paciente num programa de saúde (ADR-0119, desenho na ADR-0109). "Acompanhamento" e não
 * "inscrição", porque nem todo programa tem inscrição formal. É histórico: voltar ao programa abre um registro novo, e
 * cada mudança de situação fica em {@link EventoAcompanhamento}. Estar num programa é dado de saúde (LGPD, art. 11).
 */
@Entity
@Table(name = "TB_ACOMPANHAMENTO_PROGRAMATICO", indexes = {
        @Index(name = "ix_acompanhamento_paciente", columnList = "paciente_id, fim"),
        @Index(name = "ix_acompanhamento_programa", columnList = "programa_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoProgramatico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Programa programa;

    /** Unidade responsável: precisa oferecer o programa (participação vigente). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid", updatable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAcompanhamento situacao;

    @Enumerated(EnumType.STRING)
    private MotivoSaidaAcompanhamento motivoSaida;

    @Column(length = 500)
    private String observacaoSaida;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
