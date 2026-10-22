package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Classificação do caso (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ClassificacaoCasoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a classificação.")
    private ClassificacaoCasoEpidemiologico classificacao;

    /** Obrigatório em confirmado e descartado. */
    private CriterioClassificacao criterio;

    @Size(max = 1000, message = "A justificativa vai até 1000 caracteres.")
    private String justificativa;
}
