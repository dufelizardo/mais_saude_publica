package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Corpo do DELETE des-habilitar/{cpf} (ver ADR-0017). Um único campo, opcional: informar uma
 * dataDesligamento desliga o profissional (e grava a data); não informar nada reabilita (e limpa
 * a data). Não existe mais um campo "ativo" separado — a presença da data já expressa a intenção.
 */
@Data
@Getter
@Setter
public class ProfissionalDesligamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate dataDesligamento;
}
