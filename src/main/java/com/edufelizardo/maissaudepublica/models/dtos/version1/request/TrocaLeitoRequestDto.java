package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
/** Troca de leito dentro da mesma unidade (ADR-0098). */
public class TrocaLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o leito de destino.")
    private UUID leitoId;

    @NotBlank(message = "Informe a matrícula de quem faz a troca.")
    private String profissionalMatricula;

    @NotBlank(message = "Informe o motivo da troca.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
