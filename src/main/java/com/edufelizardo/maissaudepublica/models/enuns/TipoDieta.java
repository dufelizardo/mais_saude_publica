package com.edufelizardo.maissaudepublica.models.enuns;

/** Dieta prescrita na internação (ADR-0187). Zero não gera horário; enteral e parenteral seguem o intervalo; as orais, as refeições. */
public enum TipoDieta {
    ZERO,
    LIVRE,
    BRANDA,
    PASTOSA,
    LIQUIDA,
    LIQUIDA_PASTOSA,
    HIPOSSODICA,
    PARA_DIABETICO,
    ENTERAL,
    PARENTERAL;

    public boolean oral() {
        return this != ZERO && this != ENTERAL && this != PARENTERAL;
    }
}
