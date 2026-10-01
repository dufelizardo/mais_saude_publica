package com.edufelizardo.maissaudepublica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Autenticado, mas sem a permissão ou fora do escopo da unidade (ADR-0067). */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceForbiddenException extends RuntimeException {

    public ResourceForbiddenException(String message) {
        super(message);
    }
}
