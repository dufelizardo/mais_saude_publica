package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ocupação das agendas por especialidade no período (ADR-0144), com a montagem de vagas da Agenda. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcupacaoAgendaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private UUID unidadeId;
    private String abrangencia;
    private long ofertadas;
    private long ocupadas;
    /** Nulo sem vaga ofertada. */
    private Integer percentual;
    private List<Linha> especialidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Linha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String especialidade;
        private long ofertadas;
        private long ocupadas;
        private Integer percentual;
    }
}
