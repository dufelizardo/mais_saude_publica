package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Getter
@Setter
public class ProfissionalAtivoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean ativo;
    private LocalDate dataDesligamento;
}
