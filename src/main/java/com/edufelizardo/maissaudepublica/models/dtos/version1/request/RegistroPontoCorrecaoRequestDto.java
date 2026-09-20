package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroPonto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class RegistroPontoCorrecaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private LocalDateTime dataHoraProposta;

    @NotNull
    private TipoRegistroPonto tipoProposto;

    @NotBlank
    private String justificativa;
}
