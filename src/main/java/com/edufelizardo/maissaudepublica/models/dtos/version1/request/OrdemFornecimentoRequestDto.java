package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ordem de fornecimento contra a ata ou o contrato (ADR-0214). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrdemFornecimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade de entrega.")
    private UUID unidadeId;
    @Size(max = 60, message = "O empenho vai até 60 caracteres.")
    private String empenho;
    @Min(value = 1, message = "O prazo é de ao menos 1 dia.")
    @Max(value = 365, message = "O prazo vai até 365 dias.")
    private Integer prazoDias;
    @NotEmpty(message = "Informe ao menos um item.")
    @Valid
    private List<Item> itens;

    /** Um item da ata ou do contrato e a quantidade. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o item.")
        private UUID itemInstrumentoId;
        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade é ao menos 1.")
        private Integer quantidade;
    }
}
