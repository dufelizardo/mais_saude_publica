package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** O checklist de protocolo (ADR-0205): o código e o nome no cadastro; os itens em cada versão. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ChecklistProtocoloRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 40, message = "O código vai até 40 caracteres.")
    private String codigo;
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @NotEmpty(message = "Informe ao menos um item.")
    @Valid
    private List<Item> itens;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotBlank(message = "Informe o código do item.")
        @Size(max = 40, message = "O código do item vai até 40 caracteres.")
        private String codigo;
        @NotBlank(message = "Informe o texto do item.")
        @Size(max = 500, message = "O texto do item vai até 500 caracteres.")
        private String texto;
    }
}
