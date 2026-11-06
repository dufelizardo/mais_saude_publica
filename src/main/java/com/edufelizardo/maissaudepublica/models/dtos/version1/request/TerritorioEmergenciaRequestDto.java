package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** O território afetado (ADR-0156): o polígono e as microáreas; sem a lista, as que o polígono atinge. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TerritorioEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String geometria;
    private List<UUID> microareaIds;
}
