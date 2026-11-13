package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** O encaminhamento intersetorial pela investigação do trabalho (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a instituição.")
    private UUID instituicaoId;

    @NotBlank(message = "Informe o motivo.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;

    /** Sem data, 30 dias. */
    private LocalDate retornoEsperadoEm;
}
