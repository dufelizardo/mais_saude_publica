package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GravidadeNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNaoConformidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** O registro da não conformidade (ADR-0204): a origem com o vínculo, a gravidade e o responsável pela matrícula. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NaoConformidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    private UUID setorId;
    @NotNull(message = "Informe a origem.")
    private OrigemNaoConformidade origem;
    private UUID incidenteId;
    private UUID irregularidadeId;
    @NotBlank(message = "Descreva a não conformidade.")
    @Size(max = 4000, message = "A descrição vai até 4000 caracteres.")
    private String descricao;
    @Size(max = 2000, message = "A correção imediata vai até 2000 caracteres.")
    private String correcaoImediata;
    @NotNull(message = "Informe a gravidade.")
    private GravidadeNaoConformidade gravidade;
    private LocalDate identificadaEm;
    @NotBlank(message = "Informe a matrícula do responsável.")
    private String responsavelMatricula;
}
