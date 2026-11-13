package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRequisicaoEstoque;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Operações do almoxarifado central (ADR-0189): requisição (unidade, central, prioridade, itens por material), atendimento (itens por
 * material), transferência avulsa (origem, destino, itens por lote), recebimento (itens da transferência com o recebido e a
 * divergência), inventário (unidade; contagem por item) e as decisões com motivo.
 */
@Data
public class OperacaoEstoqueRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula.")
    private String profissionalMatricula;

    private UUID unidadeId;

    private UUID centralId;

    private UUID destinoId;

    private PrioridadeRequisicaoEstoque prioridade;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;

    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;

    @Valid
    private List<Item> itens = new ArrayList<>();

    @Data
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID materialId;

        private UUID loteId;

        /** O item da transferência ou do inventário. */
        private UUID itemId;

        @Min(value = 0, message = "A quantidade não pode ser negativa.")
        private Integer quantidade;

        private MotivoDivergenciaEstoque motivoDivergencia;

        @Size(max = 500, message = "A observação do item vai até 500 caracteres.")
        private String observacao;
    }
}
