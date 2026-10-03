package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Autorização com a vaga: unidade executante e data e hora (ADR-0087). */
@Data
@Getter
@Setter
public class AutorizacaoRegulacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private UUID unidadeExecutanteId;

    @NotNull
    private LocalDateTime dataHoraPrevista;

    @Size(max = 1000)
    private String observacao;

    /** Quem vai atender, se já se sabe: a autorização já cria o agendamento (ADR-0089). */
    private String profissionalExecutanteMatricula;
}
