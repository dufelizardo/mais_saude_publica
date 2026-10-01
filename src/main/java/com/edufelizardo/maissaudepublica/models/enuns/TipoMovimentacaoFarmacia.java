package com.edufelizardo.maissaudepublica.models.enuns;

public enum TipoMovimentacaoFarmacia {
    /** Saldo que o lote já tinha quando o livro de movimentação passou a existir (ADR-0057). */
    SALDO_INICIAL,
    ENTRADA,
    DISPENSACAO,
    PERDA,
    AJUSTE_INVENTARIO,
    /** Saída do lote de origem numa transferência entre unidades (ADR-0059). */
    TRANSFERENCIA_SAIDA,
    /** Entrada no lote da unidade de destino, no recebimento conferido da transferência (ADR-0059, ADR-0061). */
    TRANSFERENCIA_ENTRADA,
    /** Devolução ao lote de origem de uma transferência cancelada antes do recebimento (ADR-0061). */
    TRANSFERENCIA_ESTORNO,
    /** Saldo de um lote duplicado da mesma remessa, levado ao lote que o incorpora (ADR-0060). */
    INCORPORACAO_SAIDA,
    /** Par da {@link #INCORPORACAO_SAIDA}, no lote que incorpora (ADR-0060). */
    INCORPORACAO_ENTRADA,
    /** Saída do lote para um medicamento administrado ao paciente pela enfermagem (ADR-0064). */
    ADMINISTRACAO,
    /** Devolução ao lote de uma administração retificada, antes da nova baixa (ADR-0064). */
    ADMINISTRACAO_ESTORNO
}
