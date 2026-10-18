package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Agregado da rede (ADR-0119): quantos pacientes estão em algum programa. Não identifica ninguém. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResumoAcompanhamentosDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long pacientes;
    private long acompanhamentos;
    private long faltosos;
}
