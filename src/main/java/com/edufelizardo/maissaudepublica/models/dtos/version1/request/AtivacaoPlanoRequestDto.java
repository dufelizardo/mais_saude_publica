package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Ativar o plano na emergência (ADR-0157): a autoridade e a justificativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtivacaoPlanoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Escolha o plano.")
    private UUID planoId;
    @NotBlank(message = "Informe a autoridade que ativa.")
    @Size(max = 200, message = "A autoridade vai até 200 caracteres.")
    private String autoridade;
    @NotBlank(message = "Justifique a ativação.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
