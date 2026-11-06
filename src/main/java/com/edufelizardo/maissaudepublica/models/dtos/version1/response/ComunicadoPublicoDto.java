package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeComunicado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Aviso no portal (ADR-0168): sem dado de pessoa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicadoPublicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String titulo;
    private String mensagem;
    private PrioridadeComunicado prioridade;
    private String unidadeNome;
    private LocalDate inicio;
    private LocalDate fim;
}
