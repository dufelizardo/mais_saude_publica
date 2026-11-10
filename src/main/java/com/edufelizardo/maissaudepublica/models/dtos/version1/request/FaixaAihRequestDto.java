package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Faixa de numeração de AIH recebida do gestor (ADR-0186): o ano e os 12 primeiros dígitos do início e do fim. */
@Data
public class FaixaAihRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o ano.")
    @Min(value = 2000, message = "Ano inválido.")
    @Max(value = 2100, message = "Ano inválido.")
    private Integer ano;

    @NotNull(message = "Informe o início da faixa.")
    @Min(value = 1, message = "O início vai de 1 a 999999999999.")
    @Max(value = 999_999_999_999L, message = "O início vai de 1 a 999999999999 (12 dígitos, sem o verificador).")
    private Long inicio;

    @NotNull(message = "Informe o fim da faixa.")
    @Min(value = 1, message = "O fim vai de 1 a 999999999999.")
    @Max(value = 999_999_999_999L, message = "O fim vai de 1 a 999999999999 (12 dígitos, sem o verificador).")
    private Long fim;
}
