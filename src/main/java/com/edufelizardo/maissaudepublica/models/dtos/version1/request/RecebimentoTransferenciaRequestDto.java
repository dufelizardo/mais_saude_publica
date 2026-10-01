package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaTransferencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Conferência de uma transferência no destino (ADR-0061). {@code quantidadeRecebida} é o que chegou;
 * se for menor do que o enviado, {@code motivoDivergencia} e {@code justificativaDivergencia} são
 * obrigatórios. Precisa ser registrada por um profissional diferente de quem enviou.
 */
@Data
@Getter
@Setter
public class RecebimentoTransferenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @PositiveOrZero
    private Integer quantidadeRecebida;

    @NotBlank
    private String profissionalMatricula;

    private MotivoDivergenciaTransferencia motivoDivergencia;

    @Size(max = 1000)
    private String justificativaDivergencia;
}
