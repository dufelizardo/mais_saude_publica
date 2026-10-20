package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** Uma etapa da crise (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EtapaCriseRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a etapa.")
    private EtapaCriseSaudeMental etapa;

    @NotBlank(message = "Descreva a etapa.")
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;

    /** Sem data, agora. */
    private Instant ocorridaEm;
}
