package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Território de um segmento (ADR-0125). O segmento não muda na edição. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TerritorioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o segmento.")
    private UUID segmentoId;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;

    @NotNull(message = "Informe a finalidade.")
    private FinalidadeTerritorio finalidade;

    /** Sem valor, ativo. */
    private Boolean ativo;
}
