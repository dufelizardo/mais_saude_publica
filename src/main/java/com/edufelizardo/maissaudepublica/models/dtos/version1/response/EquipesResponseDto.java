package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import java.util.List;
import java.util.Map;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** As equipes do escopo, com os indicadores do topo da tela (ADR-0103). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EquipesResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long equipes;
    private long ativas;
    private Map<TipoEquipe, Long> porTipo;
    /** Profissionais diferentes com participação vigente em alguma equipe ativa. */
    private long profissionaisVinculados;
    private long incompletas;
    private List<EquipeResumoDto> lista;
}
