package com.edufelizardo.maissaudepublica.models.enuns;

/** Ciclo de uma transferência entre unidades (ADR-0061). */
public enum StatusTransferenciaFarmacia {
    /** Saiu do lote de origem e ainda não foi conferida no destino. */
    EM_TRANSITO,
    /** Conferida no destino com a quantidade enviada. */
    RECEBIDA,
    /** Conferida no destino com menos do que foi enviado; a diferença fica registrada com motivo. */
    RECEBIDA_COM_DIVERGENCIA,
    /** Cancelada antes do recebimento; o saldo voltou ao lote de origem. */
    CANCELADA
}
