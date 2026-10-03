package com.edufelizardo.maissaudepublica.models.enuns;

/** O padrão que gerou um alerta da auditoria (ADR-0096). */
public enum TipoAlertaAuditoria {
    /** Muitas recusas (403) do mesmo usuário em pouco tempo. */
    RECUSAS_SEGUIDAS,
    /** Muitos logins recusados do mesmo CPF (ou da mesma origem) em pouco tempo. */
    LOGIN_RECUSADO,
    /** Leitura de dados de muitos pacientes diferentes pelo mesmo usuário em pouco tempo. */
    LEITURA_EM_MASSA,
    /** Leituras de dado de saúde de madrugada, fora de unidade que funciona 24 horas. */
    LEITURA_FORA_DO_HORARIO,
    /** Prontuário aberto sem vínculo, por acesso justificado: a supervisão revisa ("quebra de vidro"). */
    ACESSO_JUSTIFICADO
}
