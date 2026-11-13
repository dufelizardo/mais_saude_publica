package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** A comunicação obrigatória da violência notificada (ADR-0201): a instituição e as datas; o motivo é montado pelo sistema. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoViolenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a instituição.")
    private UUID instituicaoId;
    private LocalDate enviadoEm;
    /** 30 dias depois do envio, se vazio. */
    private LocalDate retornoEsperadoEm;
}
