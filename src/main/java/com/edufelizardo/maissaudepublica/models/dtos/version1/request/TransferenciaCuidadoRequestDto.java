package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Proposta de transferência do cuidado (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaCuidadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade de destino.")
    private UUID unidadeDestinoId;

    @NotBlank(message = "Escreva o resumo de contrarreferência.")
    @Size(max = 2000, message = "O resumo vai até 2000 caracteres.")
    private String resumo;
}
