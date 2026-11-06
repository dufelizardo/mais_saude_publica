package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoObservacaoAnimal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** O desfecho da observação do animal agressor (ADR-0163). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DesfechoObservacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o desfecho.")
    private DesfechoObservacaoAnimal desfecho;
    private LocalDate data;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
