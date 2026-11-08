package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DecisaoRegulacaoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRecursoUrgencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A regulação médica da ocorrência (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegulacaoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula do médico regulador.")
    private String profissionalMatricula;
    @NotNull(message = "Classifique a gravidade.")
    private PrioridadeRegulacao prioridade;
    @NotNull(message = "Informe a decisão.")
    private DecisaoRegulacaoUrgencia decisao;
    @NotBlank(message = "Descreva a conduta.")
    @Size(max = 2000, message = "A conduta vai até 2000 caracteres.")
    private String conduta;
    private TipoRecursoUrgencia tipoRecurso;
    @Size(max = 60, message = "A identificação do recurso vai até 60 caracteres.")
    private String recursoIdentificacao;
    private java.util.UUID destinoId;
    private Boolean vagaZero;
    @Size(max = 500, message = "A justificativa da vaga zero vai até 500 caracteres.")
    private String justificativaVagaZero;
}
