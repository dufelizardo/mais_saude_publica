package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Os acolhimentos em saúde mental de um paciente (ADR-0120), com a base do acesso, como no prontuário. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcolhimentosSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private AcessoProntuarioDto acesso;
    private List<AcolhimentoSaudeMentalDto> lista;
}
