package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * A situação do encaminhamento intersetorial (ADR-0154): enviado, recebido pela instituição, concluído ou recusado por
 * ela (o retorno), ou cancelado por quem encaminhou. "Sem retorno" é derivado: em aberto depois do prazo esperado.
 */
public enum SituacaoEncaminhamentoIntersetorial {
    ENVIADO,
    RECEBIDO,
    CONCLUIDO,
    RECUSADO,
    CANCELADO;

    public boolean emAberto() {
        return this == ENVIADO || this == RECEBIDO;
    }
}
