package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um item do catálogo geral de procedimentos (ADR-0136); o código não muda. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcedimentoCatalogoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,39}$", message = "Use letras maiúsculas, números e sublinhado (ex.: CURATIVO).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;

    /** Sem especialidade, o procedimento é de uso geral. */
    private UUID especialidadeId;

    @Pattern(regexp = "^\\d{10}$", message = "O código SIGTAP tem 10 dígitos.")
    private String sigtap;

    /** Sem alcance, NENHUM. */
    private AlcanceProcedimento alcance;

    private UUID condicaoResultanteId;

    private Boolean exigeDentista;

    private Boolean ativo;
}
