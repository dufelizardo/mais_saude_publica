package com.edufelizardo.maissaudepublica.models.enuns;

/** O que a busca ativa encontrou (ADR-0132); "mudou de endereço" sugere encerrar o acompanhamento. */
public enum ResultadoBuscaAtiva {
    ENCONTRADO_AGENDADO,
    ENCONTRADO_RECUSOU,
    NAO_ENCONTRADO,
    MUDOU_DE_ENDERECO
}
