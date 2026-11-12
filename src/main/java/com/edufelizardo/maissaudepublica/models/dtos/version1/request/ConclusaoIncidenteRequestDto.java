package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FatorContribuinte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** A conclusão da análise (ADR-0203): os fatores, a causa raiz (obrigatória no dano grave, no óbito e no never event) e as lições. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConclusaoIncidenteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Informe ao menos um fator contribuinte.")
    private List<FatorContribuinte> fatores;
    @Size(max = 4000, message = "A causa raiz vai até 4000 caracteres.")
    private String causaRaiz;
    @Size(max = 2000, message = "As lições vão até 2000 caracteres.")
    private String licoes;
}
