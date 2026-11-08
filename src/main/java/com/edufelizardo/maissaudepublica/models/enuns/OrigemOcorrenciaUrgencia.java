package com.edufelizardo.maissaudepublica.models.enuns;

/** De onde veio a ocorrência de urgência (ADR-0176). */
public enum OrigemOcorrenciaUrgencia {
    /** Ligação para a central: quem ligou e o telefone de retorno. */
    CHAMADO_192,
    /** Pedido de uma UPA, hospital ou UBS: transferência ou remoção. */
    UNIDADE_DE_SAUDE,
    /** Acionamento a partir do evento de crise em saúde mental (ADR-0123). */
    CRISE_SAUDE_MENTAL
}
