package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Busca ativa do acompanhado faltoso ou atrasado (ADR-0132); sem responsável, o ACS da microárea. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuscaAtivaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String responsavelMatricula;

    /** Sem data, hoje. */
    private LocalDate dataPrevista;

    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;
}
