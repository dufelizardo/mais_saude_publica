package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Pagamento da liquidação com a ordem bancária do SIAFIC (ADR-0219). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PagamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a data do pagamento.")
    private LocalDate data;
    @NotBlank(message = "Informe a ordem bancária.")
    @Size(max = 60, message = "A ordem bancária vai até 60 caracteres.")
    private String ordemBancaria;
    /** Obrigatória para pagar antes de outra liquidação vencida da mesma fonte (Lei 14.133/2021, art. 141). */
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativaForaDeOrdem;
}
