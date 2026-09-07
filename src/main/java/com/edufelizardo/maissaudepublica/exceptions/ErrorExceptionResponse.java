package com.edufelizardo.maissaudepublica.exceptions;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ErrorExceptionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String message;
    private String details;

    public ErrorExceptionResponse(String s, String message) {
        this.message = message;
        this.details = s;
    }
}
