package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Dimensões independentes de permissão (ADR-0054): administrar o sistema não dá acesso a dado de
 * saúde, e vice-versa.
 */
public enum DimensaoPermissao {
    /** Usuários, papéis, atribuições, estrutura organizacional. */
    ADMINISTRACAO_DO_SISTEMA,
    /** Operação administrativa e assistencial sem conteúdo clínico (cadastro, agenda, estoque, RH). */
    OPERACAO,
    /** Leitura ou registro de informação clínica do paciente. */
    ACESSO_AO_DADO_DE_SAUDE
}
