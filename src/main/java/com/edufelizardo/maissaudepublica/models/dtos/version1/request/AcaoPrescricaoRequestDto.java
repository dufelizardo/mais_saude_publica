package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AceitacaoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoChecagem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Ações sobre a prescrição hospitalar (ADR-0187): suspender item (motivo), reaprazar horário (novo horário e motivo), checar
 * (resultado, aceitação da dieta, motivo de não administração, lote e quantidade, hora) e retificar a checagem (motivo da retificação).
 */
@Data
public class AcaoPrescricaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula.")
    private String profissionalMatricula;

    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;

    private Instant previstoPara;

    private ResultadoChecagem resultado;

    private AceitacaoDieta aceitacao;

    private MotivoNaoAdministracao motivoNaoAdministracao;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;

    private UUID loteId;

    @Min(value = 1, message = "A quantidade é de pelo menos 1.")
    private Integer quantidade;

    private Instant realizadoEm;

    @Size(max = 1000, message = "O motivo da retificação vai até 1000 caracteres.")
    private String motivoRetificacao;
}
