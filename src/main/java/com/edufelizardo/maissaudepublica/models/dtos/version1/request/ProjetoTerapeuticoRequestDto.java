package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Elaboração (versaoBase nula) ou revisão do PTS (ADR-0121). {@code versaoBase} é a versão vigente que a
 * revisão parte; se outra revisão entrou antes, 409. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjetoTerapeuticoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** A versão vigente de onde a revisão parte; nula na primeira elaboração. */
    private Integer versaoBase;

    @NotBlank(message = "Descreva as necessidades.")
    @Size(max = 4000, message = "As necessidades vão até 4000 caracteres.")
    private String necessidades;

    @NotEmpty(message = "Inclua pelo menos um objetivo.")
    private List<@Valid ObjetivoTerapeuticoRequestDto> objetivos;

    private List<@Valid IntervencaoPrevistaRequestDto> intervencoesPrevistas;

    /** Sem periodicidade, trimestral. */
    private PeriodicidadeAcompanhamento periodicidadeRevisao;

    /** Sem data, hoje mais a periodicidade. */
    private LocalDate revisarEm;

    /** Sem valor, não pactuado. */
    private Boolean pactuadoComPessoa;

    @Size(max = 1000, message = "A observação da pactuação vai até 1000 caracteres.")
    private String pactuacaoObservacao;

    /** Obrigatória na revisão. */
    @Size(max = 2000, message = "A síntese da revisão vai até 2000 caracteres.")
    private String sinteseRevisao;
}
