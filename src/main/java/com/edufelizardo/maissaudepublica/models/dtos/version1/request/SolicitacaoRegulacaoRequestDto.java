package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Pedido de regulação feito pela unidade de origem (ADR-0087). */
@Data
@Getter
@Setter
public class SolicitacaoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID pacienteId;

    @NotNull
    private UUID procedimentoId;

    @NotNull
    private UUID unidadeSolicitanteId;

    @NotBlank
    private String profissionalMatricula;

    /** CID-10: letra, dois dígitos e, opcionalmente, a subcategoria (ex.: I10, E11.9, M545). */
    @NotBlank
    @Pattern(regexp = "^\\s*[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?\\s*$", message = "CID-10 inválido (ex.: I10, E11.9)")
    private String cid;

    @NotBlank
    @Size(min = 10, max = 4000)
    private String justificativa;

    @NotNull
    private PrioridadeRegulacao prioridade;
}
