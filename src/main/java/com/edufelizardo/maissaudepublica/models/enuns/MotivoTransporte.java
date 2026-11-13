package com.edufelizardo.maissaudepublica.models.enuns;

/** Para que o paciente vai (ADR-0190). Hemodiálise e terapia seriada aceitam a recorrência; o TFD passa pela comissão. */
public enum MotivoTransporte {
    CONSULTA,
    EXAME,
    HEMODIALISE,
    /** Quimioterapia, radioterapia, fisioterapia e outras sessões seguidas. */
    TERAPIA_SERIADA,
    ALTA_HOSPITALAR,
    /** Tratamento fora do domicílio (Portaria SAS 55/1999). */
    TFD,
    OUTRO;

    public boolean recorrente() {
        return this == HEMODIALISE || this == TERAPIA_SERIADA;
    }
}
