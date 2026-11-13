package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoRecursoSanitario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O julgamento do recurso (ADR-0200); no parcialmente provido, as penalidades que ficam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class JulgamentoRecursoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o resultado.")
    private ResultadoRecursoSanitario resultado;
    @NotBlank(message = "Informe a fundamentação.")
    @Size(max = 4000, message = "A fundamentação vai até 4000 caracteres.")
    private String fundamentacao;
    private LocalDate data;
    private List<UUID> penalidadesMantidas;
}
