package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Saída do paciente do programa (ADR-0119): alta conclui; os outros motivos encerram. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncerramentoAcompanhamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o motivo da saída.")
    private MotivoSaidaAcompanhamento motivoSaida;

    @NotBlank(message = "Descreva a saída.")
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String observacao;

    /** Sem data, hoje. */
    private LocalDate fim;
}
