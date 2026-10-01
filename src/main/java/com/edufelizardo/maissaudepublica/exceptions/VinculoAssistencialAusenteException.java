package com.edufelizardo.maissaudepublica.exceptions;

/**
 * 403 do prontuário sem vínculo assistencial (ADR-0076). Tem código próprio em {@code details}
 * ({@value #CODIGO}) para a tela oferecer o acesso justificado em vez de um "sem permissão" genérico.
 */
public class VinculoAssistencialAusenteException extends ResourceForbiddenException {

    public static final String CODIGO = "VINCULO_ASSISTENCIAL_AUSENTE";

    public VinculoAssistencialAusenteException(String message) {
        super(message);
    }
}
