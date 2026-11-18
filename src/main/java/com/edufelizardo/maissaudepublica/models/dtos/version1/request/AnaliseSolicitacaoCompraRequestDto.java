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

/** A análise da solicitação de compra (ADR-0212): aprovar, devolver ou rejeitar (os dois últimos com o motivo). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnaliseSolicitacaoCompraRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a decisão.")
    private com.edufelizardo.maissaudepublica.models.enuns.DecisaoSolicitacaoCompra decisao;
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;
}
