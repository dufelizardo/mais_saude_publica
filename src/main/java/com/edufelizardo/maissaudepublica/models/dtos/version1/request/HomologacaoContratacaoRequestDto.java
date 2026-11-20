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

/** Homologa a contratação (ADR-0213): um instrumento para cada fornecedor vencedor. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HomologacaoContratacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private List<Instrumento> instrumentos;

    /** A ata ou o contrato de um fornecedor vencedor. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Instrumento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o fornecedor.")
        private UUID fornecedorId;
        @NotNull(message = "Informe o tipo: ata ou contrato.")
        private com.edufelizardo.maissaudepublica.models.enuns.TipoInstrumentoContratual tipo;
        @NotBlank(message = "Informe o número.")
        @Size(max = 60, message = "O número vai até 60 caracteres.")
        private String numero;
        @NotNull(message = "Informe o início da vigência.")
        private LocalDate inicio;
        @NotNull(message = "Informe o fim da vigência.")
        private LocalDate fim;
        private Boolean servicoContinuo;
        @NotBlank(message = "Informe a matrícula do gestor.")
        private String gestorMatricula;
        @NotBlank(message = "Informe a matrícula do fiscal.")
        private String fiscalMatricula;
    }
}
