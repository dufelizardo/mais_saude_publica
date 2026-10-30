package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NaturezaEventoMassa;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoEventoMassa;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** Os dados do evento de massa (ADR-0160). O fim não vem antes do início, nem o público máximo abaixo do estimado (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoMassaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a natureza do evento.")
    private NaturezaEventoMassa natureza;
    @NotBlank(message = "Informe o local do evento.")
    @Size(max = 300, message = "O local vai até 300 caracteres.")
    private String local;
    @NotNull(message = "Informe o início previsto.")
    private Instant inicioPrevisto;
    @NotNull(message = "Informe o fim previsto.")
    private Instant fimPrevisto;
    @NotNull(message = "Informe o público estimado.")
    @Min(value = 1, message = "O público estimado é de pelo menos 1 pessoa.")
    private Integer publicoEstimado;
    @Min(value = 1, message = "O público máximo é de pelo menos 1 pessoa.")
    private Integer publicoMaximo;
    @Size(max = 200, message = "O organizador vai até 200 caracteres.")
    private String organizador;
    @Size(max = 200, message = "O contato vai até 200 caracteres.")
    private String contato;
    @NotNull(message = "Informe a classificação de risco.")
    private RiscoEventoMassa risco;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
