package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoTurno;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Cadastro e edição de turno (ADR-0105). O fim antes do início (ou igual, no plantão de 24 horas) cai no dia seguinte. Sem
 * profissional, o turno é uma vaga aberta e precisa da função. Na edição, a unidade e o profissional ficam: a troca de
 * profissional é pela designação.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TurnoEscalaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    private UUID equipeId;

    /** Vazio: vaga aberta. */
    private String profissionalMatricula;

    private FuncaoEquipe funcao;

    @NotNull(message = "Informe o tipo do turno.")
    private TipoTurno tipo;

    @NotNull(message = "Informe a data.")
    private LocalDate data;

    @NotNull(message = "Informe o início.")
    private LocalTime inicio;

    @NotNull(message = "Informe o fim.")
    private LocalTime fim;

    @Size(max = 120, message = "A descrição vai até 120 caracteres.")
    private String descricao;

    /** Intervalo de repouso e alimentação, em minutos; não conta na jornada. Sem valor, sem intervalo. */
    @Min(value = 0, message = "O intervalo não pode ser negativo.")
    @Max(value = 120, message = "O intervalo vai até 2 horas (CLT, art. 71).")
    private Integer intervaloMinutos;
}
