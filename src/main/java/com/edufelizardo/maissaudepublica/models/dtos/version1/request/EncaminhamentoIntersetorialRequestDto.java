package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Encaminhamento a uma instituição externa (ADR-0154). Sem {@code enviadoEm}, hoje. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoIntersetorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a instituição.")
    private UUID instituicaoId;

    @NotBlank(message = "Informe o motivo do encaminhamento.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;

    private LocalDate enviadoEm;

    @NotNull(message = "Informe até quando se espera o retorno da instituição.")
    private LocalDate retornoEsperadoEm;
}
