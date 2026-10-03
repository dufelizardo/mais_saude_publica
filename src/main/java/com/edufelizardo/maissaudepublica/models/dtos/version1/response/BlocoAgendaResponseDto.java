package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.BlocoAgenda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Bloco recorrente da agenda (ADR-0091). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BlocoAgendaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID unidadeId;
    private String unidadeNome;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private int duracaoMinutos;
    private TipoAgendamento tipo;
    private LocalDate vigenteDesde;
    private LocalDate vigenteAte;
    /** Quantas vagas o bloco oferece por dia. */
    private int vagasPorDia;

    public static BlocoAgendaResponseDto fromBloco(BlocoAgenda b) {
        int vagas = (int) (Duration.between(b.getHoraInicio(), b.getHoraFim()).toMinutes() / b.getDuracaoMinutos());
        return new BlocoAgendaResponseDto(b.getUuid(), b.getProfissional().getMatricula(), b.getProfissional().getNome(),
                b.getUnidade().getUuid(), b.getUnidade().getNome(), b.getDiaSemana(), b.getHoraInicio(), b.getHoraFim(),
                b.getDuracaoMinutos(), b.getTipo(), b.getVigenteDesde(), b.getVigenteAte(), vagas);
    }
}
