package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** A referência estrutural (ADR-0197): sem início, vale a partir de hoje. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReferenciaRedeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a origem (unidade ou esfera).")
    private UUID origemId;
    @NotNull(message = "Informe o procedimento.")
    private UUID procedimentoId;
    @NotNull(message = "Informe a executante.")
    private UUID executanteId;
    private LocalDate inicio;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
