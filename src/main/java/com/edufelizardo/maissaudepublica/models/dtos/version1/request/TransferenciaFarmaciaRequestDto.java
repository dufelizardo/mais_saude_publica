package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Transferência de parte do saldo de um lote para outra unidade (ADR-0059). O lote de destino não é
 * informado: é o da mesma remessa na unidade de destino, criado quando ainda não existe.
 */
@Data
@Getter
@Setter
public class TransferenciaFarmaciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID loteOrigemId;

    @NotNull
    private UUID unidadeDestinoId;

    @NotNull
    @Positive
    private Integer quantidade;

    @NotBlank
    private String profissionalMatricula;

    @Size(max = 1000)
    private String observacao;
}
