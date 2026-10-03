package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Conclusão de um alerta da auditoria (ADR-0096): procedente ou improcedente, com parecer. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnaliseAlertaAuditoriaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a conclusão: PROCEDENTE ou IMPROCEDENTE.")
    private StatusAlertaAuditoria conclusao;

    @NotBlank(message = "Escreva o parecer.")
    @Size(min = 10, max = 2000, message = "O parecer precisa ter de 10 a 2000 caracteres.")
    private String parecer;
}
