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

/** Solicitação de compra, o documento de formalização da demanda (ADR-0212), com os itens. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoCompraRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    private UUID setorId;
    @NotBlank(message = "Informe a justificativa da necessidade.")
    @Size(max = 2000, message = "A justificativa vai até 2000 caracteres.")
    private String justificativa;
    private com.edufelizardo.maissaudepublica.models.enuns.PrioridadeCompra prioridade;
    @NotEmpty(message = "Informe ao menos um item.")
    @Size(max = 200, message = "A solicitação vai até 200 itens.")
    @Valid
    private List<Item> itens;

    /** Um item: material (do catálogo), medicamento ou serviço (descrito). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o tipo do item.")
        private com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra tipo;
        private UUID materialId;
        private UUID medicamentoId;
        @Size(max = 500, message = "A descrição do item vai até 500 caracteres.")
        private String descricao;
        @Size(max = 30, message = "A unidade de medida vai até 30 caracteres.")
        private String unidadeMedida;
        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade é ao menos 1.")
        private Integer quantidade;
    }
}
