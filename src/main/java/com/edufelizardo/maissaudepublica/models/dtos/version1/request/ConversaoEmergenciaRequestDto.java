package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Converter o evento em emergência (ADR-0156): o nome, o nível inicial e a justificativa; o território é opcional. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConversaoEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome da emergência.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @NotNull(message = "Informe o nível inicial.")
    private UUID nivelId;
    @NotBlank(message = "Justifique a ativação.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
    private String responsavelMatricula;
    private String geometria;
    private List<UUID> microareaIds;
}
