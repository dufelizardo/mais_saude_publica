package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoExameAnimal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Coleta do exame do animal (ADR-0163). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameAnimalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o animal.")
    private UUID animalId;
    @NotNull(message = "Informe o exame.")
    private TipoExameAnimal tipo;
    private LocalDate dataColeta;
    private UUID observacaoId;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
