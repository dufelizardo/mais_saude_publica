package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoTurno;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Um turno da escala (ADR-0105), com os alertas de jornada e descanso de quem está nele. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TurnoEscalaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID equipeId;
    private String equipeNome;
    /** Nulos na vaga aberta. */
    private String profissionalMatricula;
    private String profissionalNome;
    private FuncaoEquipe funcao;
    private TipoTurno tipo;
    /** Dia em que o turno começa. */
    private LocalDate data;
    private LocalDateTime inicioEm;
    private LocalDateTime fimEm;
    /** Horas trabalhadas: a duração menos o intervalo. */
    private double horas;
    private int intervaloMinutos;
    private String descricao;
    private boolean vaga;
    /** Avisos que não impedem: jornada semanal acima da contratada, descanso menor que 11 horas, fora da equipe. */
    private List<String> alertas;
}
