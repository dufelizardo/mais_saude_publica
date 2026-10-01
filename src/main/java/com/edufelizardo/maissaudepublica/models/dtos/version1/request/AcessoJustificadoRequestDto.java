package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoAcessoJustificado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Acesso ao prontuário sem vínculo assistencial (ADR-0076). O mínimo do texto é conferido no serviço (configurável). */
@Data
@Getter
@Setter
public class AcessoJustificadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o motivo")
    private MotivoAcessoJustificado motivo;

    @NotBlank(message = "Escreva a justificativa")
    @Size(max = 1000, message = "A justificativa pode ter até 1000 caracteres")
    private String justificativa;
}
