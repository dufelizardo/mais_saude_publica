package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
@Data
@Getter
@Setter
public class SuccessResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String message;
    private String details;

    public SuccessResponseDto(String message, String details) {
        this.message = message;
        this.details = details;
    }
}

