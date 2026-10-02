package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoBloqueioAgenda;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Período em que a agenda não oferece vaga (ADR-0091): de um profissional (em qualquer unidade, ou numa só) ou da
 * unidade inteira ({@code profissional} nulo, por exemplo unidade fechada). Férias e afastamentos não ficam aqui:
 * vêm do RH na hora de montar a agenda.
 */
@Entity
@Table(name = "TB_BLOQUEIO_AGENDA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BloqueioAgenda implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", updatable = false)
    private UnidadeDeSaude unidade;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime inicio;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MotivoBloqueioAgenda motivo;

    @Column(length = 500, updatable = false)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    public BloqueioAgenda(Profissional profissional, UnidadeDeSaude unidade, LocalDateTime inicio, LocalDateTime fim,
                          MotivoBloqueioAgenda motivo, String descricao, Instant registradoEm, String registradoPorCpf) {
        this.profissional = profissional;
        this.unidade = unidade;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
        this.descricao = descricao;
        this.registradoEm = registradoEm;
        this.registradoPorCpf = registradoPorCpf;
    }

    /** Vale para este profissional nesta unidade e cobre algum instante de [de, ate). */
    public boolean cobre(UUID profissionalId, UUID unidadeId, LocalDateTime de, LocalDateTime ate) {
        boolean doProfissional = profissional == null || profissional.getUuid().equals(profissionalId);
        boolean daUnidade = unidade == null || unidade.getUuid().equals(unidadeId);
        return doProfissional && daUnidade && inicio.isBefore(ate) && fim.isAfter(de);
    }
}
