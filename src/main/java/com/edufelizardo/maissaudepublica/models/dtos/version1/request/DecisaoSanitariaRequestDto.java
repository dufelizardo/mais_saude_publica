package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AgravanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.AtenuanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoDecisaoSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoPenalidadeSanitaria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** A decisão da autoridade julgadora (ADR-0200): o arquivamento ou as penalidades, com as atenuantes e as agravantes (arts. 7º e 8º). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DecisaoSanitariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o resultado.")
    private ResultadoDecisaoSanitaria resultado;
    @NotBlank(message = "Informe a fundamentação.")
    @Size(max = 4000, message = "A fundamentação vai até 4000 caracteres.")
    private String fundamentacao;
    private LocalDate data;
    private List<AtenuanteSanitaria> atenuantes;
    private List<AgravanteSanitaria> agravantes;
    @Valid
    private List<Penalidade> penalidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Penalidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotNull(message = "Informe o tipo da penalidade.")
        private TipoPenalidadeSanitaria tipo;
        @DecimalMin(value = "0.01", message = "A multa precisa de valor.")
        private BigDecimal valorMulta;
        private LocalDate suspensaoAte;
        @Size(max = 500, message = "A descrição vai até 500 caracteres.")
        private String descricao;
    }
}
