package com.edufelizardo.maissaudepublica.models.enuns;

/** Cada passo da internação eletiva agendada vira um evento imutável (ADR-0185). */
public enum TipoEventoInternacaoAgendada {
    AGENDAMENTO,
    RESERVA,
    REMARCACAO,
    ADMISSAO,
    FALTA,
    CANCELAMENTO
}
