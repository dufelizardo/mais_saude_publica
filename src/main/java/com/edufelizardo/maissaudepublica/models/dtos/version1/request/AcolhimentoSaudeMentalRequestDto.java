package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAcolhimentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoInicialSaudeMental;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Acolhimento em saúde mental (ADR-0120). Com desfecho que acompanha, sem caso vigente, a equipe e o técnico de
 * referência são obrigatórios: o caso abre junto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcolhimentoSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe a unidade do acolhimento.")
    private UUID unidadeId;

    /** Opcional: o atendimento em que o acolhimento aconteceu. */
    private UUID atendimentoId;

    @NotBlank(message = "Descreva o motivo da procura.")
    @Size(max = 2000, message = "O motivo da procura vai até 2000 caracteres.")
    private String motivoProcura;

    @Size(max = 1000, message = "A necessidade imediata vai até 1000 caracteres.")
    private String necessidadeImediata;

    @Size(max = 1000, message = "A situação de vulnerabilidade vai até 1000 caracteres.")
    private String vulnerabilidade;

    @NotNull(message = "Informe o risco percebido.")
    private RiscoInicialSaudeMental riscoInicial;

    @NotNull(message = "Informe o desfecho do acolhimento.")
    private DesfechoAcolhimentoSaudeMental desfecho;

    @Size(max = 500, message = "O destino do encaminhamento vai até 500 caracteres.")
    private String encaminhamentoDestino;

    private UUID equipeId;

    private String tecnicoMatricula;
}
