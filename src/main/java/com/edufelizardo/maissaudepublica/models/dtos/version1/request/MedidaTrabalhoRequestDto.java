package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaTrabalho;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A medida recomendada pela investigação do trabalho (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da medida.")
    private TipoMedidaTrabalho tipo;

    @NotBlank(message = "Descreva a medida.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o prazo.")
    private LocalDate prazo;
}
