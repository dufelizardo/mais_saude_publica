package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Conclui a pesquisa de preços (ADR-0212): a justificativa de cada item que ficou com menos de 3 cotações. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConclusaoPesquisaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private List<Justificativa> justificativas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Justificativa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o item.")
        private UUID itemId;
        @NotBlank(message = "Informe a justificativa.")
        @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
        private String justificativa;
    }
}
