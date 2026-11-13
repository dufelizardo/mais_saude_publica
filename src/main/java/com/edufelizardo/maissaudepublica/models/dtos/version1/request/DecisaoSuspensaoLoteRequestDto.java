package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A decisão da rede sobre o lote suspenso: liberar ou inutilizar, com o parecer (ADR-0206). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DecisaoSuspensaoLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a decisão: LIBERAR ou INUTILIZAR.")
    private String decisao;
    @NotBlank(message = "Informe o parecer.")
    @Size(max = 1000, message = "O parecer vai até 1000 caracteres.")
    private String parecer;
}
