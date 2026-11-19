package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Diária ou ajuda de custo do TFD (ADR-0219). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DiariaTfdRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a solicitação de TFD.")
    private UUID solicitacaoId;
    @NotNull(message = "Informe o beneficiário.")
    private BeneficiarioDiariaTfd beneficiario;
    /** O credor já cadastrado; ou, no lugar dele, o CPF do beneficiário ({@code credorCpf}). */
    private UUID credorId;
    /** O CPF do beneficiário: o credor pessoa física é encontrado ou cadastrado com o nome dele. */
    @Size(max = 14, message = "O CPF vai até 14 caracteres.")
    private String credorCpf;
    @NotNull(message = "Informe os dias.")
    @Min(value = 1, message = "Os dias vão de 1 a 30.")
    @Max(value = 30, message = "Os dias vão de 1 a 30.")
    private Integer dias;
    @NotNull(message = "Informe o valor da diária.")
    @DecimalMin(value = "0.01", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valorDiaria;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
