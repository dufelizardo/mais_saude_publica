package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** A área vigente de uma equipe, com as microáreas e os ACS (ADR-0125); sem área, {@code area} nula. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TerritorioDaEquipeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID equipeId;
    private String territorioNome;
    private FinalidadeTerritorio finalidade;
    private ArvoreTerritorialDto.AreaNo area;
}
