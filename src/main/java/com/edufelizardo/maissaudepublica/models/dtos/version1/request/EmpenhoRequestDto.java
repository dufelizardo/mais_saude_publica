package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Empenho lançado no SIAFIC e registrado na plataforma (ADR-0218). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EmpenhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o número do empenho.")
    @Size(max = 30, message = "O número vai até 30 caracteres.")
    private String numero;
    @NotNull(message = "Informe o tipo.")
    private TipoEmpenho tipo;
    @NotNull(message = "Informe a dotação.")
    private UUID dotacaoId;
    @NotNull(message = "Informe o fornecedor.")
    private UUID fornecedorId;
    /** A ata ou o contrato, se houver. */
    private UUID instrumentoId;
    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "O valor é positivo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valor;
    @NotNull(message = "Informe a data.")
    private LocalDate data;
    @Size(max = 500, message = "O histórico vai até 500 caracteres.")
    private String historico;
}
