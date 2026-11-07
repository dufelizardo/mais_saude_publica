package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Passagem da unidade móvel por um ponto (ADR-0166). Sem data, agora. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PosicaoUnidadeMovelRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o ponto.")
    private UUID pontoId;
    private LocalDateTime desde;
    @Size(max = 300, message = "A observação vai até 300 caracteres.")
    private String observacao;
}
