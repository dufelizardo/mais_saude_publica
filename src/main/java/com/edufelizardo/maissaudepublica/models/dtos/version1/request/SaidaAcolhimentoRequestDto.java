package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A saída do acolhimento noturno, com o motivo (ADR-0153). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SaidaAcolhimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate saida;

    @NotBlank(message = "Informe o motivo da saída.")
    @Size(max = 500)
    private String motivo;
}
