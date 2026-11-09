package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Acionar o SAMU a partir da crise em saúde mental (ADR-0176, fecha a SM3). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcionamentoSamuRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a central de regulação.")
    private java.util.UUID centralId;
    @NotBlank(message = "Descreva o motivo do acionamento.")
    @Size(max = 2000, message = "O motivo vai até 2000 caracteres.")
    private String motivo;
    @Size(max = 300, message = "O endereço vai até 300 caracteres.")
    private String endereco;
    @Size(max = 20, message = "O telefone vai até 20 caracteres.")
    private String telefone;
}
