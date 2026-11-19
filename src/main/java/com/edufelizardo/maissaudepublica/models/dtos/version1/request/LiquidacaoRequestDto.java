package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Liquidação da despesa a partir do atesto ou avulsa (ADR-0219). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LiquidacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a origem.")
    private OrigemLiquidacao origem;
    /** O recebimento, a ordem de serviço ou a diária; vazio na avulsa. */
    private UUID origemId;
    @NotNull(message = "Informe o empenho.")
    private UUID empenhoId;
    @Size(max = 60, message = "A nota vai até 60 caracteres.")
    private String notaFiscal;
    private LocalDate notaEmitidaEm;
    @NotNull(message = "Informe o valor bruto.")
    @DecimalMin(value = "0.01", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valorBruto;
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal iss;
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal irrf;
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal inss;
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal outrasRetencoes;
    /** Sem informar, 30 dias. */
    private LocalDate vencimento;
    @Size(max = 500, message = "A justificativa vai até 500 caracteres.")
    private String justificativa;
    /** Na avulsa: a matrícula de quem atestou o serviço. */
    @Size(max = 30, message = "A matrícula vai até 30 caracteres.")
    private String atestadoPorMatricula;
}
