package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FaixaRendaFamiliar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Família nova num domicílio: o responsável e, opcionalmente, os outros membros (ADR-0127). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FamiliaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o domicílio.")
    private UUID domicilioId;

    @NotNull(message = "Informe o responsável.")
    private UUID responsavelId;

    private FaixaRendaFamiliar rendaFamiliar;

    /** Sem valor, hoje. */
    private LocalDate inicio;

    @Valid
    private List<MembroFamiliaRequestDto> membros;
}
