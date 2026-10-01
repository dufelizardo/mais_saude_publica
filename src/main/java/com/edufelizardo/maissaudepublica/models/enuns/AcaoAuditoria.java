package com.edufelizardo.maissaudepublica.models.enuns;

/** O que aconteceu, num evento de auditoria (ADR-0070). */
public enum AcaoAuditoria {
    LOGIN,
    TROCA_DE_SENHA,
    /** Pedido do link e troca de senha por ele, sem login (ADR-0081). */
    RECUPERACAO_DE_SENHA,
    /** Leitura de dado de saúde ou pessoal (rotas marcadas com {@code @AuditarLeitura}). */
    LEITURA,
    CRIACAO,
    ALTERACAO,
    RETIFICACAO,
    REVOGACAO,
    /** Acesso ao prontuário sem vínculo assistencial, declarado com motivo (ADR-0076). */
    ACESSO_JUSTIFICADO,
    /** Exportação da própria trilha (CSV), com os filtros usados (ADR-0082). */
    EXPORTACAO,
    EXCLUSAO
}
