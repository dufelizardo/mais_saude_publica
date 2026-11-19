package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Conferência de um bem do inventário (ADR-0217). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConferenciaInventarioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o resultado.")
    private ResultadoConferencia resultado;
    /** Obrigatória quando o bem foi visto (encontrado ou em outro local). */
    private ConservacaoBem conservacao;
    /** No outro local: o setor onde estava (da unidade do inventário ou da unidade informada). */
    private UUID setorId;
    /** No outro local: a unidade onde estava, se não for a do inventário. */
    private UUID unidadeId;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
