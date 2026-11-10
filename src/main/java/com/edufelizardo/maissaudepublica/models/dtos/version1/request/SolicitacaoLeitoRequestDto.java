package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Pedido de leito à central de regulação de leitos (ADR-0184). */
@Data
public class SolicitacaoLeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade de origem.")
    private UUID unidadeOrigemId;

    private UUID atendimentoId;

    /** A internação na origem, quando o paciente já está internado e precisa de outro nível. */
    private UUID internacaoOrigemId;

    private UUID ocorrenciaUrgenciaId;

    @NotBlank(message = "Informe a matrícula de quem pede.")
    private String profissionalMatricula;

    @NotBlank(message = "Informe o CID-10.")
    @Pattern(regexp = "^\\s*[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?\\s*$", message = "CID-10 inválido (ex.: J18.9).")
    private String cid;

    @NotBlank(message = "Descreva o quadro clínico.")
    @Size(max = 2000, message = "O quadro clínico vai até 2000 caracteres.")
    private String quadroClinico;

    @NotNull(message = "Informe o tipo de leito.")
    private TipoLeito tipoLeito;

    @NotNull(message = "Informe a prioridade.")
    private PrioridadeRegulacao prioridade;
}
