package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Vacinação antirrábica animal do posto no dia (ADR-0163): ao menos um animal (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacinacaoAntirrabicaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    @NotNull(message = "Informe a microárea.")
    private UUID microareaId;
    @NotBlank(message = "Informe o posto.")
    @Size(max = 200, message = "O posto vai até 200 caracteres.")
    private String posto;
    @Min(value = 0, message = "Os animais não ficam abaixo de zero.")
    private Integer caes;
    @Min(value = 0, message = "Os animais não ficam abaixo de zero.")
    private Integer gatos;
    @NotBlank(message = "Informe o responsável.")
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
}
