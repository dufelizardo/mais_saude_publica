package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As vagas e a permanência máxima do acolhimento noturno da unidade (ADR-0153). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfiguracaoAcolhimentoNoturnoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe as vagas.")
    @Min(value = 1, message = "A unidade tem pelo menos 1 vaga.")
    private Integer vagas;

    @NotNull(message = "Informe a permanência máxima.")
    @Min(value = 1, message = "A permanência máxima é de pelo menos 1 dia.")
    private Integer permanenciaMaximaDias;
}
