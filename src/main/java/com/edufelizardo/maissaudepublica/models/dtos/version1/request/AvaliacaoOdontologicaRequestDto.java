package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Avaliação odontológica de um atendimento de odontologia (ADR-0134). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoOdontologicaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o atendimento.")
    private UUID atendimentoId;

    @NotBlank(message = "Informe a matrícula do cirurgião-dentista.")
    private String profissionalMatricula;

    /** Sem data, agora. */
    private LocalDateTime dataHora;

    @Size(max = 1000, message = "A queixa vai até 1000 caracteres.")
    private String queixaPrincipal;

    @Size(max = 4000, message = "A anamnese vai até 4000 caracteres.")
    private String anamnese;

    @Size(max = 4000, message = "O exame clínico vai até 4000 caracteres.")
    private String exameClinico;

    @Size(max = 2000, message = "O diagnóstico vai até 2000 caracteres.")
    private String diagnostico;

    @NotNull(message = "Informe o risco em saúde bucal.")
    private RiscoSaudeBucal risco;

    @NotNull(message = "Informe a necessidade de tratamento.")
    private NecessidadeTratamentoOdontologico necessidadeTratamento;

    @Size(max = 2000, message = "O plano inicial vai até 2000 caracteres.")
    private String planoInicial;

    @Size(max = 2000, message = "As observações vão até 2000 caracteres.")
    private String observacoes;

    /** Na retificação (ADR-0062): por que a versão anterior é corrigida. */
    @Size(max = 500, message = "O motivo vai até 500 caracteres.")
    private String motivoRetificacao;
}
