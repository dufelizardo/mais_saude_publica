package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaTrabalho;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** A verificação da medida do trabalho (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VerificacaoMedidaTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** CUMPRIDA ou NAO_CUMPRIDA. */
    @NotNull(message = "Informe se a medida foi cumprida.")
    private SituacaoMedidaTrabalho situacao;

    /** Sem data, hoje. */
    private LocalDate verificadaEm;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
