package com.edufelizardo.maissaudepublica.models.enuns;

/** Por onde a denúncia sanitária chegou (ADR-0199). */
public enum CanalDenuncia {
    TELEFONE, PRESENCIAL, OUVIDORIA, INTERNET, OUTRO,
    /** Aberta pela própria vigilância, pela investigação do trabalho (ADR-0208). */
    VIGILANCIA
}
