package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Aceite do bem pela unidade de destino (ADR-0215), com o novo responsável. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AceiteTransferenciaBemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Opcional; da mesma unidade do bem. */
    private UUID setorId;
    @NotBlank(message = "Informe a matrícula do responsável.")
    @Size(max = 30, message = "A matrícula vai até 30 caracteres.")
    private String responsavelMatricula;
}
