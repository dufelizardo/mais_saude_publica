package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Pedido de exames feito na unidade de origem (ADR-0093). */
@Data
@Getter
@Setter
public class PedidoExameRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID pacienteId;

    private UUID atendimentoId;

    @NotNull
    private UUID unidadeSolicitanteId;

    @NotBlank
    private String profissionalMatricula;

    @NotEmpty
    private List<UUID> exameIds;

    @NotBlank
    @Size(max = 2000)
    private String indicacaoClinica;

    @Pattern(regexp = "^\\s*([A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?)?\\s*$", message = "CID-10 inválido (ex.: I10, E11.9)")
    private String cid;

    @NotNull
    private PrioridadeExame prioridade;
}
