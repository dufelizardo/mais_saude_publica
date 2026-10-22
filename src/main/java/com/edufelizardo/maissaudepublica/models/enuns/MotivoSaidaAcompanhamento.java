package com.edufelizardo.maissaudepublica.models.enuns;

/** Por que o paciente saiu do programa (ADR-0119). Alta conclui; os demais encerram. */
public enum MotivoSaidaAcompanhamento {
    ALTA,
    TRANSFERENCIA,
    MUDANCA_DE_MUNICIPIO,
    ABANDONO,
    OBITO,
    OUTRO
}
