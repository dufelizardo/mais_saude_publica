package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Cadastro de câmara de refrigeração (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CamaraRefrigeracaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID unidadeId;
    @NotBlank
    @Size(max = 80)
    private String nome;
    private BigDecimal temperaturaMinima;
    private BigDecimal temperaturaMaxima;
    /** Na edição: desativar tira a câmara das pendências de leitura. */
    private Boolean ativa;
}
