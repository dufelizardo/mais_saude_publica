package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AtividadeSaudeBucalColetiva;
import com.edufelizardo.maissaudepublica.models.enuns.LocalSaudeBucalColetiva;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** O detalhe odontológico da ação coletiva (ADR-0140). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoSaudeBucalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Informe as atividades de saúde bucal.")
    private List<AtividadeSaudeBucalColetiva> atividades;

    @NotNull(message = "Informe o local: ESCOLA, CRECHE, UBS, COMUNIDADE, EMPRESA, INSTITUICAO_LONGA_PERMANENCIA ou OUTRO.")
    private LocalSaudeBucalColetiva localTipo;

    @Size(max = 200, message = "O nome da instituição vai até 200 caracteres.")
    private String instituicao;

    @Size(max = 300, message = "O público-alvo vai até 300 caracteres.")
    private String publicoAlvo;

    @PositiveOrZero(message = "Os kits distribuídos não podem ser negativos.")
    private Integer kitsDistribuidos;
}
