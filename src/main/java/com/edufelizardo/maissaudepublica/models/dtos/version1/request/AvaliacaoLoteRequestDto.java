package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Avaliação do lote sob suspeita pela coordenação (ADR-0148): liberar ou inutilizar, com o parecer. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** LIBERAR ou INUTILIZAR. */
    @NotBlank
    private String decisao;
    @NotBlank
    @Size(max = 500)
    private String parecer;
}
