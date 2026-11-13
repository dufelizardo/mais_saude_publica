package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma ação do plano 5W2H (ADR-0204): o quê (descricao), por quê (justificativa), quem, onde, quando, como e quanto custa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoQualidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o quê.")
    @Size(max = 500, message = "O quê vai até 500 caracteres.")
    private String descricao;
    @NotBlank(message = "Informe por quê.")
    @Size(max = 500, message = "O porquê vai até 500 caracteres.")
    private String justificativa;
    @NotBlank(message = "Informe a matrícula de quem faz.")
    private String responsavelMatricula;
    @Size(max = 200, message = "O onde vai até 200 caracteres.")
    private String onde;
    @NotNull(message = "Informe o prazo.")
    private LocalDate prazo;
    @Size(max = 1000, message = "O como vai até 1000 caracteres.")
    private String como;
    @DecimalMin(value = "0", message = "O custo não fica abaixo de zero.")
    private BigDecimal custo;
}
