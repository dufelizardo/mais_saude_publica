package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Dotação do Fundo Municipal de Saúde no exercício (ADR-0218). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DotacaoOrcamentariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o exercício.")
    private Integer exercicio;
    @NotBlank(message = "Informe o programa de trabalho.")
    @Pattern(regexp = "^\\d{2}\\.\\d{3}\\.\\d{4}\\.\\d{4}$", message = "O programa de trabalho é no formato 10.301.0010.2045.")
    private String programaTrabalho;
    @NotBlank(message = "Descreva a ação.")
    @Size(max = 300, message = "A descrição vai até 300 caracteres.")
    private String descricao;
    @NotBlank(message = "Informe a natureza da despesa.")
    @Pattern(regexp = "^\\d\\.\\d\\.\\d{2}\\.\\d{2}(\\.\\d{2})?$", message = "A natureza da despesa é no formato 3.3.90.30.")
    private String naturezaDespesa;
    @NotNull(message = "Informe a fonte.")
    private FonteRecursoSaude fonte;
    @Size(max = 20, message = "O código da fonte vai até 20 caracteres.")
    private String fonteCodigo;
    @NotNull(message = "Informe o valor da lei.")
    @DecimalMin(value = "0.01", message = "O valor é positivo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valorInicial;
}
