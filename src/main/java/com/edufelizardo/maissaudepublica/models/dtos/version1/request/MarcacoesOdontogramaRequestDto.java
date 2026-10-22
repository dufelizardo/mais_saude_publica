package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Marcações do odontograma feitas na avaliação odontológica (ADR-0135). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MarcacoesOdontogramaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem data, a da avaliação. */
    private LocalDate data;

    @NotEmpty(message = "Informe as marcações.")
    @Valid
    private List<Marcacao> marcacoes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Marcacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o dente (FDI).")
        private Integer dente;

        /** Nula: o dente inteiro. */
        private FaceDente face;

        @NotNull(message = "Informe a condição.")
        private UUID condicaoId;

        @Size(max = 500, message = "A observação vai até 500 caracteres.")
        private String observacao;
    }
}
