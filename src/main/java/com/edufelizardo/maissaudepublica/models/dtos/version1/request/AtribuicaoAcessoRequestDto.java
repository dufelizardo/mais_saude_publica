package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Concede um papel a um usuário num escopo (ADR-0066). Sem unidade = rede inteira; período opcional. */
@Data
@Getter
@Setter
public class AtribuicaoAcessoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID usuarioId;

    @NotNull
    private UUID papelId;

    private UUID unidadeId;

    private LocalDate inicio;

    private LocalDate fim;
}
