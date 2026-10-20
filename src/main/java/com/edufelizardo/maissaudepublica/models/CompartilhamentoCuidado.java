package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cuidado compartilhado do caso com outra equipe (ADR-0122), tipicamente a eSF do paciente. Enquanto vigente, os
 * membros da equipe parceira têm vínculo e o conteúdo do caso: compartilhar o cuidado é compartilhar o PTS.
 */
@Entity
@Table(name = "TB_COMPARTILHAMENTO_CUIDADO", indexes = @Index(name = "ix_compartilhamento_cuidado_acomp", columnList = "acompanhamento_id, fim"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CompartilhamentoCuidado implements Serializable {
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

    @Column(nullable = false, length = 500, updatable = false)
    private String objetivo;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;
}
