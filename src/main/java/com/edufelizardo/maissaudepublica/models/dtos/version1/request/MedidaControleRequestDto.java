package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaControle;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaControle;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Medida de controle (ADR-0131); concluir pede o resultado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaControleRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da medida.")
    private TipoMedidaControle tipo;

    @NotBlank(message = "Descreva a medida.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    /** Sem matrícula, quem registra (se é profissional). */
    private String responsavelMatricula;

    private LocalDate prazo;

    /** Na edição; na criação, planejada. */
    private SituacaoMedidaControle situacao;

    @Size(max = 2000, message = "O resultado vai até 2000 caracteres.")
    private String resultado;
}
