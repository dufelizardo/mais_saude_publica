package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Encerra o bloco: ele vale até {@code vigenteAte}, inclusive (ADR-0091). */
@Data
@Getter
@Setter
public class EncerramentoBlocoAgendaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private LocalDate vigenteAte;
}
