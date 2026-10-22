package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Encerramento de um perfil ou de uma capacidade da unidade (ADR-0117): o registro fica no histórico. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem data, hoje. */
    private LocalDate fim;

    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivo;
}
