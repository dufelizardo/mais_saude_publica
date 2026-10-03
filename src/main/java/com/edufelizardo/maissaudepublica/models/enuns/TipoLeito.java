package com.edufelizardo.maissaudepublica.models.enuns;

/** Tipo do leito (ADR-0098), uma versão simplificada da classificação de leitos do CNES. */
public enum TipoLeito {
    CLINICO,
    CIRURGICO,
    PEDIATRICO,
    OBSTETRICO,
    UTI_ADULTO,
    UTI_PEDIATRICA,
    UTI_NEONATAL,
    /** Leito de observação, como o da UPA. */
    OBSERVACAO,
    ISOLAMENTO
}
