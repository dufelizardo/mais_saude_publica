package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
@Data
@Getter
@Setter
public class UnidadeDeSaudeAtivoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean ativo;
}
