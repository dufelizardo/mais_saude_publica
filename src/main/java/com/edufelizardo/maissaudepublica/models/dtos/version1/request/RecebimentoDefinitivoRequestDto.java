package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O recebimento definitivo (art. 140, I, b; ADR-0214): o aceito de cada item entregue. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RecebimentoDefinitivoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Informe o aceite de cada item.")
    @Valid
    private List<Item> itens;

    /** O aceito de um item; o recusado pede o motivo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o item do recebimento.")
        private UUID itemRecebimentoId;
        @NotNull(message = "Informe a quantidade aceita.")
        @Min(value = 0, message = "A quantidade aceita não fica abaixo de zero.")
        private Integer quantidadeAceita;
        @Size(max = 500, message = "O motivo vai até 500 caracteres.")
        private String motivoRecusa;
    }
}
