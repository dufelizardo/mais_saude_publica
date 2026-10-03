package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A agenda de um profissional numa unidade num período, dia a dia, com o resumo (ADR-0091). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AgendaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String profissionalMatricula;
    private String profissionalNome;
    private UUID unidadeId;
    private String unidadeNome;
    private LocalDate de;
    private LocalDate ate;
    /** Vagas da agenda no período, fora as bloqueadas. */
    private int vagasOfertadas;
    /** Vagas com marcação (não cancelada). */
    private int vagasOcupadas;
    /** Vagas ocupadas sobre ofertadas, de 0 a 100. */
    private int ocupacaoPercentual;
    private int marcacoes;
    private int encaixes;
    private int faltas;
    private List<DiaAgendaDto> dias;
    /** Situação da unidade quando não está em operação (ADR-0101), para a tela avisar. Nulo em operação. */
    private String avisoUnidade;
}
