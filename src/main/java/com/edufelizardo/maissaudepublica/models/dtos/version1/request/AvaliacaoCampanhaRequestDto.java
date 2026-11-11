package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAtingido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A avaliação da campanha de comunicação encerrada (ADR-0196). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoCampanhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Diga se o público-alvo foi atingido.")
    private PublicoAtingido publicoAtingido;
    @Size(max = 2000, message = "Os resultados vão até 2000 caracteres.")
    private String resultados;
    @NotBlank(message = "Informe as conclusões.")
    @Size(max = 2000, message = "As conclusões vão até 2000 caracteres.")
    private String conclusoes;
    @Size(max = 2000, message = "As recomendações vão até 2000 caracteres.")
    private String recomendacoes;
}
