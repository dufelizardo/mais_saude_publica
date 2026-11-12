package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Concessão da licença sanitária ou da dispensa (ADR-0199). A validade padrão é de um ano. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LicencaSanitariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate emitidaEm;
    private LocalDate validade;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
