package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCriseSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Registro de uma crise (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoCriseRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe o tipo da crise.")
    private TipoCriseSaudeMental tipo;

    @NotBlank(message = "Descreva a crise.")
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;

    @Size(max = 200, message = "O local vai até 200 caracteres.")
    private String localOcorrencia;

    /** Sem data, agora. */
    private Instant identificadaEm;
}
