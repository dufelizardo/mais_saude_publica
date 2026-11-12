package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoQualidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** O andamento da ação pelo responsável ou pela gestão (ADR-0204): em andamento ou concluída com a evidência. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AndamentoAcaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a situação.")
    private SituacaoAcaoQualidade situacao;
    @Size(max = 2000, message = "A evidência vai até 2000 caracteres.")
    private String evidencia;
    private LocalDate concluidaEm;
}
