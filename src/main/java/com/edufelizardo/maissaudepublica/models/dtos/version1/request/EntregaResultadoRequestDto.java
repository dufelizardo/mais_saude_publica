package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FormaEntregaResultado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Entrega do resultado ao paciente (ADR-0181). Retirada por terceiro pede nome e documento. */
@Data
public class EntregaResultadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    /** A unidade onde o resultado é entregue. */
    @NotNull
    private UUID unidadeId;

    @NotNull
    private Boolean retiradoPeloPaciente;

    @Size(max = 150)
    private String terceiroNome;

    @Size(max = 30)
    private String terceiroDocumento;

    @Size(max = 50)
    private String terceiroParentesco;

    @NotNull
    private FormaEntregaResultado forma;
}
