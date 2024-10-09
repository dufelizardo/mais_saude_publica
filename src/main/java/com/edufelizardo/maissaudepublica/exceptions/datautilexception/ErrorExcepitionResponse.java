package com.edufelizardo.maissaudepublica.exceptions.datautilexception;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ErrorExcepitionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String message;
    private String details;

    public ErrorExcepitionResponse(String s, String message) {
        this.message = message;
        this.details = s;
    }
}
