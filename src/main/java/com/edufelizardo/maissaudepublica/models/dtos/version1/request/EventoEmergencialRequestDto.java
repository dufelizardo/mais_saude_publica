package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FonteEventoEmergencial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Registro do evento emergencial (ADR-0156). Sem {@code ocorridoEm}, agora. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoEmergencialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private UUID tipoId;
    private Instant ocorridoEm;
    @Size(max = 300, message = "O local vai até 300 caracteres.")
    private String local;
    private String geometria;
    @NotNull(message = "Informe a fonte.")
    private FonteEventoEmergencial fonte;
    @NotBlank(message = "Descreva o evento.")
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;
    private UUID surtoId;
}
