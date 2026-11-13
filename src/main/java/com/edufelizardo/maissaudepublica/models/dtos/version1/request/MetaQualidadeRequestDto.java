package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorQualidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A meta de um indicador de qualidade na unidade (ADR-0205); na adesão, o protocolo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MetaQualidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    @NotNull(message = "Informe o indicador.")
    private IndicadorQualidade indicador;
    private UUID checklistId;
    @NotNull(message = "Informe a meta.")
    @DecimalMin(value = "0", message = "A meta não fica abaixo de zero.")
    private BigDecimal valor;
    private LocalDate vigenteDesde;
}
