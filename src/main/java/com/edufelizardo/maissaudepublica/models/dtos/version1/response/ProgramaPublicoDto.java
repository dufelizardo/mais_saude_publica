package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Um programa ativo na consulta pública da landing page (ADR-0118): só o que serve ao cidadão — nome, categoria,
 * descrição, público e onde é oferecido. Sem paciente, sem coordenação, sem códigos de condição.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramaPublicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private String categoria;
    private String descricao;
    private String publico;
    private List<String> unidades;
}
