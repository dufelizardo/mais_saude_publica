package com.edufelizardo.maissaudepublica.models.enuns;

/** Situação da solicitação de transporte eletivo (ADR-0190). */
public enum SituacaoSolicitacaoTransporte {
    SOLICITADA,
    /** TFD à espera da comissão. */
    AGUARDANDO_AUTORIZACAO,
    AUTORIZADA,
    /** Está numa viagem programada ou em andamento. */
    PROGRAMADA,
    REALIZADA,
    FALTOU,
    NEGADA,
    CANCELADA;

    /** Na fila, pronta para entrar numa viagem. */
    public boolean naFila() {
        return this == SOLICITADA || this == AUTORIZADA;
    }

    public boolean cancelavel() {
        return this == SOLICITADA || this == AGUARDANDO_AUTORIZACAO || this == AUTORIZADA;
    }
}
