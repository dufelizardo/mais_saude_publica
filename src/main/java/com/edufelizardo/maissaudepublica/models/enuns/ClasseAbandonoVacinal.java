package com.edufelizardo.maissaudepublica.models.enuns;

/** A taxa de abandono de uma vacina de várias doses, pelos parâmetros do PNI (ADR-0149): abaixo de 5%, de 5% a 10%, 10% ou mais. */
public enum ClasseAbandonoVacinal {
    BAIXA,
    MEDIA,
    ALTA;

    public static ClasseAbandonoVacinal de(double taxa) {
        return taxa < 5 ? BAIXA : taxa < 10 ? MEDIA : ALTA;
    }
}
