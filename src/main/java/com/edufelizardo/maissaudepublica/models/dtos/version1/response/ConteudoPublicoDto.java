package com.edufelizardo.maissaudepublica.models.dtos.version1.response;


import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Orientação de saúde no portal (ADR-0168). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConteudoPublicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String titulo;
    private String resumo;
    private String texto;
    private String tema;
    private String faixaEtaria;
    private int versao;
    private LocalDate validade;
}
