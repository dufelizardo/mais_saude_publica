package com.edufelizardo.maissaudepublica.models.enuns;

/** Por que um medicamento prescrito não foi administrado (ADR-0064). {@code OUTRO} exige observação. */
public enum MotivoNaoAdministracao {
    RECUSA_DO_PACIENTE,
    PACIENTE_AUSENTE,
    MEDICAMENTO_EM_FALTA,
    SUSPENSO_PELO_MEDICO,
    OUTRO
}
