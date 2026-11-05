package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Observação do animal agressor (ADR-0163): o animal, a data da agressão e, opcional, o atendimento antirrábico. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObservacaoAnimalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o animal.")
    private UUID animalId;
    private UUID notificacaoId;
    @NotNull(message = "Informe a data da agressão.")
    private LocalDate dataAgressao;
    private LocalDate inicio;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
