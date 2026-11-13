package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Agendamento de internação eletiva (ADR-0185). */
@Data
public class AgendamentoInternacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe a data prevista.")
    private LocalDate dataPrevista;

    @NotNull(message = "Informe o tipo de leito.")
    private TipoLeito tipoLeito;

    @NotBlank(message = "Informe a matrícula do médico responsável.")
    private String medicoMatricula;

    @NotBlank(message = "Informe o CID-10.")
    @Pattern(regexp = "^\\s*[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?\\s*$", message = "CID-10 inválido (ex.: K80.2).")
    private String cid;

    @NotBlank(message = "Informe o procedimento ou a cirurgia prevista.")
    @Size(max = 500, message = "O procedimento vai até 500 caracteres.")
    private String procedimento;

    @Min(1)
    @Max(90)
    private Integer previsaoDias;

    private UUID solicitacaoRegulacaoId;
}
