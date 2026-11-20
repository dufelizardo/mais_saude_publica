package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O recebimento provisório de uma entrega (art. 140, I; ADR-0214). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecebimentoCompraRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o número da nota fiscal.")
    @Size(max = 60, message = "A nota fiscal vai até 60 caracteres.")
    private String notaFiscal;
    @NotNull(message = "Informe a data da nota fiscal.")
    private LocalDate notaEmitidaEm;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
    @NotEmpty(message = "Informe ao menos um item entregue.")
    @Valid
    private List<Item> itens;

    /** Um item entregue, com o lote e a validade quando o produto pede. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o item da ordem.")
        private UUID itemOrdemId;
        @NotNull(message = "Informe a quantidade entregue.")
        @Min(value = 1, message = "A quantidade é ao menos 1.")
        private Integer quantidade;
        @Size(max = 60, message = "O lote vai até 60 caracteres.")
        private String numeroLote;
        private LocalDate validade;
    }
}
