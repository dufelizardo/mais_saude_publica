package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Edição de lote: só corrige o que foi digitado errado na entrada (número impresso na embalagem e
 * validade). Quantidade muda apenas pelo livro de movimentação, e medicamento/unidade não mudam —
 * trocar a unidade de um lote seria uma transferência sem registro (ADR-0057).
 */
@Data
@Getter
@Setter
public class LoteAtualizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String numeroLote;

    @NotNull
    private LocalDate validade;
}
