package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIncidente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A classificação do incidente pelo Núcleo de Segurança do Paciente (ICPS; ADR-0203). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ClassificacaoIncidenteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a natureza.")
    private NaturezaIncidente natureza;
    @NotNull(message = "Informe o grau de dano.")
    private GrauDano dano;
    /** Corrige o tipo informado na notificação. */
    private TipoIncidente tipo;
    private Boolean neverEvent;
}
