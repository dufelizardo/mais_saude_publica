package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Serviço da unidade (ADR-0165). O tipo e o início só valem na criação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServicoUnidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID tipoId;
    @NotBlank(message = "Informe o nome do serviço.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    private String responsavelMatricula;
    private Boolean vinteQuatroHoras;
    @Size(max = 200, message = "O horário vai até 200 caracteres.")
    private String horario;
    private List<UUID> setorIds;
    private LocalDate inicio;
}
