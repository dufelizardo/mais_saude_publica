package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A comunicação da investigação do trabalho à vigilância sanitária (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoVisaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o que a vigilância sanitária precisa verificar.")
    @Size(max = 1500, message = "A descrição vai até 1500 caracteres.")
    private String descricao;
}
