package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Ativar a capacidade emergencial na emergência (ADR-0158). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtivacaoCapacidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Escolha a capacidade.")
    private UUID capacidadeId;
    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade é acima de zero.")
    private Integer quantidade;
    @NotBlank(message = "Justifique a ativação.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
