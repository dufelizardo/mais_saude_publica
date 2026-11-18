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

/** Certidão de regularidade do fornecedor (ADR-0212). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CertidaoFornecedorRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da certidão.")
    private com.edufelizardo.maissaudepublica.models.enuns.TipoCertidaoFornecedor tipo;
    @Size(max = 60, message = "O número vai até 60 caracteres.")
    private String numero;
    @NotNull(message = "Informe a data de emissão.")
    private LocalDate emitidaEm;
    @NotNull(message = "Informe a validade.")
    private LocalDate validaAte;
}
