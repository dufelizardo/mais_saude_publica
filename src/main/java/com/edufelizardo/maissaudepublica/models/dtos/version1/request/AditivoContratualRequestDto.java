package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Termo aditivo (ADR-0213). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AditivoContratualRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo do aditivo.")
    private com.edufelizardo.maissaudepublica.models.enuns.TipoAditivoContratual tipo;
    /** Na prorrogação. */
    private LocalDate novoFim;
    /** No acréscimo, na supressão e no reequilíbrio. */
    private UUID itemId;
    @Min(value = 1, message = "A quantidade é ao menos 1.")
    private Integer quantidade;
    @DecimalMin(value = "0.0001", message = "O preço é maior que zero.")
    @Digits(integer = 10, fraction = 4, message = "O preço vai até 10 dígitos inteiros e 4 decimais.")
    private BigDecimal novoPreco;
    @NotBlank(message = "Informe a justificativa.")
    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
    @NotNull(message = "Informe a data de assinatura.")
    private LocalDate assinadoEm;
}
