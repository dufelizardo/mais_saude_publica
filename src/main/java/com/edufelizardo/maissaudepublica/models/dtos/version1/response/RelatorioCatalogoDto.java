package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um item do catálogo de relatórios (ADR-0167). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RelatorioCatalogoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private String categoria;
    private String descricao;
}
