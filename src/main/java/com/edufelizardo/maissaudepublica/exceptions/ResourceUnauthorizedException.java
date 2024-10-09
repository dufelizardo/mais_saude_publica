package com.edufelizardo.maissaudepublica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ResourceUnauthorizedException extends RuntimeException {

    public ResourceUnauthorizedException(String message) {
        super(message);
    }

    public ResourceUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
