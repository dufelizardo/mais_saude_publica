package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Conclusão da denúncia sanitária (ADR-0199): procedente (com inspeção) ou improcedente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConclusaoDenunciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe se a denúncia foi procedente ou improcedente.")
    private SituacaoDenuncia situacao;
    @NotBlank(message = "Informe a conclusão.")
    @Size(max = 1000, message = "A conclusão vai até 1000 caracteres.")
    private String conclusao;
}
