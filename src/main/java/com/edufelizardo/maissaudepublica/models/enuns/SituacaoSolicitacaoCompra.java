package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação da solicitação de compra (ADR-0212): rascunho e devolvida se editam; enviada vai à análise; aprovada recebe as cotações; cotada tem o preço de referência e segue para a contratação; contratada, homologada (CP2, ADR-0213). */
public enum SituacaoSolicitacaoCompra {
    RASCUNHO, ENVIADA, DEVOLVIDA, APROVADA, REJEITADA, EM_COTACAO, COTADA, CONTRATADA, CANCELADA
}
