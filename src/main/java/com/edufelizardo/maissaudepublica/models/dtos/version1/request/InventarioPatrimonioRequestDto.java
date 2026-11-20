package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Abertura do inventário da unidade com a comissão de três profissionais (ADR-0217). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InventarioPatrimonioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    @NotNull(message = "Informe a comissão.")
    private List<String> membrosMatriculas = new ArrayList<>();
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
