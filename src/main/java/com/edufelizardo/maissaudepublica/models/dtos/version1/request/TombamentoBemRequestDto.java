package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Tombamento do bem comprado (ADR-0215): a classe, a identificação, o setor e o responsável. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TombamentoBemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a classe do bem.")
    private ClasseBem classe;
    @Size(max = 80, message = "A marca vai até 80 caracteres.")
    private String marca;
    @Size(max = 80, message = "O modelo vai até 80 caracteres.")
    private String modelo;
    @Size(max = 80, message = "O número de série vai até 80 caracteres.")
    private String numeroSerie;
    /** Opcional; da mesma unidade do bem. */
    private UUID setorId;
    @NotBlank(message = "Informe a matrícula do responsável.")
    @Size(max = 30, message = "A matrícula vai até 30 caracteres.")
    private String responsavelMatricula;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
