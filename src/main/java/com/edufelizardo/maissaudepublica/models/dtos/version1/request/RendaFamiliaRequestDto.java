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

/** Faixa de renda da família (ADR-0127); sem valor, não informada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RendaFamiliaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private FaixaRendaFamiliar rendaFamiliar;
}
