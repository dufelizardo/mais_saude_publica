package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Agendamento de uma solicitação autorizada na unidade executante (ADR-0089). Sem data, vale a da vaga. */
@Data
@Getter
@Setter
public class AgendamentoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotBlank
    private String profissionalExecutanteMatricula;

    private LocalDateTime dataHora;
}
