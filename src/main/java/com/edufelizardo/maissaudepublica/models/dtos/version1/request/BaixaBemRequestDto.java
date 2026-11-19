package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Pedido de baixa do bem (ADR-0217). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BaixaBemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o motivo.")
    private MotivoBaixaBem motivo;
    @NotBlank(message = "Informe o processo administrativo.")
    @Size(max = 60, message = "O processo vai até 60 caracteres.")
    private String processo;
    @Size(max = 60, message = "O boletim vai até 60 caracteres.")
    private String boletimOcorrencia;
    @NotNull(message = "Informe a data da baixa.")
    private LocalDate dataBaixa;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
